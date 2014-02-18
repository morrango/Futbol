/*
 *    ArenaFutbol - by Morrango
 *    http://
 *
 *    This file is part of ArenaFutbol.
 *
 *    ArenaFutbol is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    ArenaFutbol is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with ArenaFutbol.  If not, see <http://www.gnu.org/licenses/>.
 *    
 *	  powered by: 
 *    KickStarter
 *    BattleArena
 *
 */

package me.morrango.arenafutbol.listeners;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.matches.MatchMessageEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.scoreboard.ArenaScoreboard;
import mc.alk.arena.objects.teams.ArenaTeam;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;


public class FutbolArena extends Arena{
	public FutbolArena(){}
	public Plugin plugin = Bukkit.getPluginManager().getPlugin("ArenaFutbol");
	public HashMap<Entity, Player> kickedBy = new HashMap<Entity, Player>();
	public HashMap<Entity, Match> kickedBalls = new HashMap<Entity, Match>();
	public HashMap<Match, Entity> cleanUpList = new HashMap<Match, Entity>();
	private HashMap<ArenaTeam, Integer> ballTimers = new HashMap<ArenaTeam, Integer>();
	public Set<ArenaTeam> canKick = new HashSet<ArenaTeam>();
	Integer id;
	//ArenaObjective scores;

	@Override
	public void onOpen(){
//        scores = new ArenaObjective("goal", "totalKillCount");
//        scores.setDisplayName(ChatColor.GOLD + "Goals");
		  ArenaScoreboard scoreboard = match.getScoreboard();
		  scoreboard.clear();
//        scores.setDisplaySlot(ArenaDisplaySlot.SIDEBAR);
//        scores.setDisplayPlayers(false);
//        scoreboard.setPoints(scores, match.getTeams().get(0), 0);
//        scoreboard.setPoints(scores, match.getTeams().get(1), 0);
//        scoreboard.addObjective(scores);
	}
	
	@Override
	public void onStart(){
		List<ArenaTeam> teamsList = match.getArena().getTeams();
		 ArenaScoreboard scoreboard = match.getScoreboard();
		Location loc = getSpawn(2, false);
		World world = loc.getWorld();
		//int ballID = plugin.getConfig().getInt("ball");
		ItemStack is = new ItemStack(Material.MAGMA_CREAM);
		ItemMeta meta = is.getItemMeta();
		meta.setDisplayName("Ball");
		is.setItemMeta(meta);
		Location center = fixCenter(world, loc);
		world.dropItem(center, is);
		for (ArenaTeam t: teamsList) {
			canKick.add(t);
			scoreboard.addTeam(t);
		}
	}
	
	@Override
	public void onVictory(MatchResult result){
		removeBalls(getMatch());
		removeArenaTeams(getMatch());
		cancelTimer();
		List<ArenaTeam> teamsList = match.getArena().getTeams();
		int teamOne = teamsList.get(0).getNKills();
		int teamTwo = teamsList.get(1).getNKills();
		if (teamOne == teamTwo) {
			match.setDraw();
		}
		if (teamOne > teamTwo) {
			match.setWinner(teamsList.get(0));
		}else {
			match.setWinner(teamsList.get(1));
		}
	}
	
	@Override
	public void onCancel(){
		removeBalls(getMatch());
		removeArenaTeams(getMatch());
	}
	
	@ArenaEventHandler
	public void matchMessages(MatchMessageEvent event){
		MatchState state = event.getState();
		if (!state.equals(MatchState.ONMATCHINTERVAL)) {
			event.setMatchMessage("");
		}
	}
	
	@ArenaEventHandler
	public void onPlayerAnimation(PlayerAnimationEvent event){
		Player player = event.getPlayer();
		ArenaPlayer arenaPlayer = getAP(player);
		ArenaTeam kickersTeam = getTeam(arenaPlayer);
		List<Entity> ent = player.getNearbyEntities(1,1,1);
		for (Entity entity : ent) {
			if(entity instanceof Item && canKick.contains(kickersTeam)) {
				List<ArenaTeam> teamsList = match.getArena().getTeams();
				Location location = player.getLocation();
				World world = player.getWorld();
				entity.setVelocity(kickVector(player));
				world.playEffect(location, Effect.STEP_SOUND, 10);
				kickedBy.put(entity, player);
				kickedBalls.put(entity, getMatch());
				cleanUpList.put(getMatch(), entity);
				for (ArenaTeam t: teamsList) {
					if (!canKick.contains(t)) {
						canKick.add(t);
						cancelBallTimer(t);
					}
				}
			}
		}
	}

	@ArenaEventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event){
		if (event.isCancelled()) {
			return;
		}
		event.setCancelled(true);
	}
	
	@ArenaEventHandler(priority=EventPriority.HIGHEST, needsPlayer=false)
	public void onEntityInteract(EntityInteractEvent event){
		Entity ent = event.getEntity();
		if (ent instanceof Item && kickedBalls.containsKey(ent) && kickedBy.get(ent) != null) {
			World world = ent.getWorld();
			Location loc = event.getEntity().getLocation();
			// ArenaTeam goals are set by the blocks below the pressure plates
			Block block = loc.getBlock().getRelative(BlockFace.DOWN);
			Material material = block.getType();
			event.setCancelled(true);
			Match thisMatch = kickedBalls.get(ent);
			//Map<Integer, Location> spawnLocs = thisMatch.getArena().getSpawnLocs();
			List<ArenaTeam> teamsList = thisMatch.getArena().getTeams();
			ArenaTeam teamOne = teamsList.get(0);
			ArenaTeam teamTwo = teamsList.get(1);
			ArenaTeam scoringTeam;
			if (material.equals(Material.SMOOTH_BRICK)) {
				scoringTeam = teamsList.get(0);
			}else {
				scoringTeam = teamsList.get(1);
			}
			
			ArenaPlayer scoringPlayer = getAP(kickedBy.get(ent));
			// Add kill and send message
			scoringTeam.addKill(scoringPlayer);
		//	match.getScoreboard().(scores, scoringTeam, scoringTeam.getNKills());
			canKick.remove(scoringTeam);
			startBallTimer(scoringTeam);
			kickedBy.put(ent, null);
			world.createExplosion(loc, -1); // TODO maybe change to a sound and effect. Also add to config.yml
			// Send ball to center
			Vector stop = new Vector(0, 0, 0);
			Location center = fixCenter(world, match.getArena().getSpawn(2, false));
			ent.setVelocity(stop);
			ent.teleport(center, TeleportCause.PLUGIN);
			// Return players to team spawn
			Set<Player> setOne = teamOne.getBukkitPlayers();
			Set<Player> setTwo = teamTwo.getBukkitPlayers();
			tpArenaTeams(setOne, setTwo, thisMatch);
		}
	}
	
	@ArenaEventHandler(needsPlayer=false)
	public void onItemDespawn(ItemDespawnEvent event){
		if (kickedBalls.containsKey(event.getEntity())) {
			event.setCancelled(true);
		}
	}
	
	@ArenaEventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event){
		event.setFoodLevel(20);
	}
	
	private void startBallTimer(final ArenaTeam team) {
		cancelBallTimer(team);
		int ballTimer = plugin.getConfig().getInt("balltimer");
		BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin,
		new Runnable(){
				@Override
				public void run() {
					canKick.add(team);
				}
			}, ballTimer + 60);
		ballTimers.put(team, task.getTaskId());
	}
	
	public void tpArenaTeams(final Set<Player> setOne,final Set<Player> setTwo, final Match match) {
				for (Player player : setOne) {player.teleport(match.getArena().getSpawn(0, false));}
				for (Player player : setTwo) {player.teleport(match.getArena().getSpawn(1, false));}
	}

	private void cancelBallTimer(ArenaTeam team){
		Integer timerid = ballTimers.get(team);
		if (id != null) {Bukkit.getScheduler().cancelTask(timerid);}
	}
	
	public ArenaPlayer getAP(Player player) {
		ArenaPlayer ap = BattleArena.toArenaPlayer(player);
		return ap;
	}
	
	public Location fixCenter(World world, Location origin) {
		Location center = new Location(world,
				origin.getX(),
				origin.getY() + 1.0,
				origin.getZ());
		return center;		
	}
	
	public Vector kickVector(Player player) {
		float configAdjPitch = -(float)plugin.getConfig().getInt("pitch");
		float configMaxPitch = -(float)plugin.getConfig().getInt("maxpitch");
		double configPower = plugin.getConfig().getDouble("power");
		Location loc = player.getEyeLocation();
		float pitch = loc.getPitch();
		pitch = pitch + configAdjPitch;
		//Bukkit.broadcastMessage("adj" + configAdjPitch + "max " + configMaxPitch);
		if (pitch > 0) {pitch = 0.0f;}
		if (pitch < configMaxPitch) {pitch = 0.0f + configMaxPitch;}
		loc.setPitch(pitch);
		Vector vector = loc.getDirection();
		vector = vector.multiply(configPower);
		return vector;
	}
	
	public void removeBalls(Match match) {
		Entity ball = cleanUpList.get(match);
		if (ball != null) {
			kickedBalls.remove(ball);
			kickedBy.remove(ball);
			ball.remove();
		}
	}
	
	public void removeArenaTeams(Match match) {
		List<ArenaTeam> teamsList = match.getArena().getTeams();
		for (ArenaTeam t : teamsList) {
			if (canKick.contains(t)) {
				canKick.remove(t);
			}
		}
	}
	
	public void cancelTimer(){
		if (id != null){
			Bukkit.getScheduler().cancelTask(id);
			id = null;
		}
	}
	
	
}