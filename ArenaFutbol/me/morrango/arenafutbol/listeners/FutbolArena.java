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
 *	  powered by KickStarter
 *
 */

package me.morrango.arenafutbol.listeners;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.matches.MatchMessageEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.teams.Team;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;


public class FutbolArena extends Arena{
	public FutbolArena(){}
	public Plugin plugin = Bukkit.getPluginManager().getPlugin("ArenaFutbol");
	public HashMap<Entity, Player> kickedBy = new HashMap<Entity, Player>();
	public HashMap<Entity, Match> kickedBalls = new HashMap<Entity, Match>();
	public HashMap<Match, Entity> cleanUpList = new HashMap<Match, Entity>();
	public Set<Team> canKick = new HashSet<Team>();
	
	@Override
	public void onStart(){
		Location loc = getSpawnLoc(2);
		World world = loc.getWorld();
		int ball = plugin.getConfig().getInt("ball");
		ItemStack is = new ItemStack(ball);
		Location center = fixCenter(world, loc);
		world.dropItem(center, is);
		List<Team> teamsList = match.getArena().getTeams();
		for (Team t: teamsList) {
			canKick.add(t);
		}
	}
	
	@Override
	public void onVictory(MatchResult result){
		removeBalls(getMatch());
		removeTeams(getMatch());
	}
	
	@Override
	public void onCancel(){
		removeBalls(getMatch());
		removeTeams(getMatch());
	}
	
	@MatchEventHandler
	public void matchMessages(MatchMessageEvent event){
		MatchState state = event.getState();
		List<Team> teamsList = match.getArena().getTeams();
		Team teamOne = teamsList.get(0);
		Team teamTwo = teamsList.get(1);
		if (state.equals(MatchState.ONMATCHINTERVAL)) {
			event.setMatchMessage("");
			match.sendMessage(ChatColor.YELLOW + "The current score is " +
					scoreMessage(teamOne, teamTwo));
		}
		if (state.equals(MatchState.ONMATCHTIMEEXPIRED)) {
			event.setMatchMessage("");
			match.sendMessage(ChatColor.YELLOW + "The Final score is " +
					scoreMessage(teamOne, teamTwo));	
		}
	}
	
	@MatchEventHandler
	public void onPlayerAnimation(PlayerAnimationEvent event){
		Player player = event.getPlayer();
		ArenaPlayer arenaPlayer = getAP(event.getPlayer());
		Team kickersTeam = getTeam(arenaPlayer);
		List<Entity> ent = player.getNearbyEntities(1,1,1);
		for (Entity entity : ent) {
			if(entity instanceof Item && canKick.contains(kickersTeam)) {
				List<Team> teamsList = match.getArena().getTeams();
				Location location = player.getLocation();
				World world = player.getWorld();
				Vector direction = player.getEyeLocation().getDirection();
				entity.setVelocity(adjustVector(direction));
				world.playEffect(location, Effect.STEP_SOUND, 10);
				kickedBy.put(entity, player);
				kickedBalls.put(entity, getMatch());
				cleanUpList.put(getMatch(), entity);
				for (Team t: teamsList) {
					if (!canKick.contains(t)) {
						canKick.add(t);
					}
				}
			}
		}
	}

	@MatchEventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event){
		if (event.isCancelled()) {
			return;
		}else {
			event.setCancelled(true);
		}
	}
	
	@MatchEventHandler(needsPlayer=false)
	public void onEntityInteract(EntityInteractEvent event){
		Entity ent = event.getEntity();
		if (ent instanceof Item && kickedBalls.containsKey(ent)) {
			World world = ent.getWorld();
			Location loc = event.getEntity().getLocation();
			// Team goals are set by the blocks below the pressure plates
			Block block = loc.getBlock().getRelative(BlockFace.DOWN);
			int blockData = block.getData();
			Match thisMatch = kickedBalls.get(ent);
			ArenaPlayer scoringPlayer = getAP(kickedBy.get(ent));
			Map<Integer, Location> spawnLocs = thisMatch.getArena().getSpawnLocs();
			List<Team> teamsList = thisMatch.getArena().getTeams();
			Team teamOne = teamsList.get(0);
			Team teamTwo = teamsList.get(1);
			// Add kill and send message
			teamsList.get(blockData).addKill(scoringPlayer);
			canKick.remove(teamsList.get(blockData));
			world.createExplosion(loc, -1); // TODO maybe change to a sound and effect. Also add to config.yml
			thisMatch.sendMessage(ChatColor.GRAY + scoringPlayer.getName() +
				ChatColor.YELLOW + " has scored a Goal!!! "); 
			thisMatch.sendMessage(scoreMessage(teamOne, teamTwo));
			// Send ball to center
			Vector stop = new Vector();
			Location center = fixCenter(world, spawnLocs.get(2));
			ent.setVelocity(stop);
			ent.teleport(center, TeleportCause.PLUGIN);
			// Return players to team spawn
			Set<Player> setOne = teamOne.getBukkitPlayers();
			Set<Player> setTwo = teamTwo.getBukkitPlayers();
			for (Player player : setOne) {player.teleport(match.getArena().getSpawnLoc(0));}
			for (Player player : setTwo) {player.teleport(match.getArena().getSpawnLoc(1));}
		}
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
	
	public Vector adjustVector(Vector vector) {
		double y = plugin.getConfig().getDouble("y");
		double maxY = plugin.getConfig().getDouble("max-y");
		double Y = vector.getY();
		if ((Y + y) <= maxY) {
			Vector adjustedVector = vector.setY(Y + y);
			return adjustedVector;
		}else {
			Vector adjustedVector = vector.setY(maxY);
			return adjustedVector;
		}
	}
	
	public String scoreMessage(Team teamOne, Team teamTwo) {
		String scoreMessage = (ChatColor.GRAY + teamOne.getName() + ": " +
			ChatColor.GOLD + teamOne.getNKills() + " " +
			ChatColor.GRAY + teamTwo.getName() + ": " +
			ChatColor.GOLD + teamTwo.getNKills());
		return scoreMessage;
	}
	
	public void removeBalls(Match match) {
		Entity ball = cleanUpList.get(match);
		if (ball != null) {
			kickedBy.remove(ball);
			kickedBalls.remove(ball);
			ball.remove();
		}
	}
	
	public void removeTeams(Match match) {
		List<Team> teamsList = match.getArena().getTeams();
		for (Team t : teamsList) {
			if (canKick.contains(t)) {
				canKick.remove(t);
			}
		}
	}
}