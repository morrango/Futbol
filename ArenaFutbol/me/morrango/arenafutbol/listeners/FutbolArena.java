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
import java.util.List;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.matches.MatchMessageEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.teams.Team;
import me.morrango.arenafutbol.ArenaFutbol;

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
import org.bukkit.util.Vector;


public class FutbolArena extends Arena{
	@SuppressWarnings("unused")
	private ArenaFutbol plugin;

	public FutbolArena(){}
	public HashMap<Entity, Player> kickedBy = new HashMap<Entity, Player>();
	public HashMap<Entity, Match> balls = new HashMap<Entity, Match>();
	
	
	@MatchEventHandler
	public void matchMessages(MatchMessageEvent event){
		MatchState state = event.getState();
		List<Team> teamsList = match.getArena().getTeams();
		Team teamOne = teamsList.get(0);
		Team teamTwo = teamsList.get(1);
		String score = (ChatColor.GRAY + teamOne.getName() + ": " + ChatColor.GOLD + teamOne.getNKills() + " " +
				ChatColor.GRAY + teamTwo.getName() + ": " + ChatColor.GOLD + teamTwo.getNKills());
		if (state.equals(MatchState.ONMATCHINTERVAL)) {
			event.setMatchMessage("");
			match.sendMessage(ChatColor.YELLOW + "The current score is " + score);
		}
		if (state.equals(MatchState.ONMATCHTIMEEXPIRED)) {
			event.setMatchMessage("");
			match.sendMessage(ChatColor.YELLOW + "The Final score is " + score);	
		}
	}
	
	@MatchEventHandler
	public void onPlayerAnimation(PlayerAnimationEvent event){
		Match match = getMatch();
		Player player = event.getPlayer();
		Location location = player.getLocation();
		World world = player.getWorld();
		List<Entity> ent = player.getNearbyEntities(1,1,1);
		for (Entity entity : ent) {
			if(entity instanceof Item) {
				Vector direction = player.getEyeLocation().getDirection();
				double y = direction.getY();
				Vector adjustedVector = direction.setY(y + 0.25);
				entity.setVelocity(adjustedVector);
				world.playEffect(location, Effect.STEP_SOUND, 10);
				kickedBy.put(entity, player);
				balls.put(entity, match);
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
		World world = ent.getWorld();
		Location loc = event.getEntity().getLocation();
		// Team goals are set by the blocks below the pressure plates
		Block block = loc.getBlock().getRelative(BlockFace.DOWN);
		int blockData = block.getData();
		if (ent instanceof Item) {
			Match thisMatch = balls.get(ent);
			Player kickedByPlayer = kickedBy.get(ent);
			Map<Integer, Location> spawnLocs = thisMatch.getArena().getSpawnLocs();
			List<Team> teamsList = thisMatch.getArena().getTeams();
			Team teamOne = teamsList.get(0);
			Team teamTwo = teamsList.get(1);
			ArenaPlayer scoringPlayer = BattleArena.toArenaPlayer(kickedByPlayer);
			// Add kill and send message
			teamsList.get(blockData).addKill(scoringPlayer);
			world.createExplosion(loc, -1); // TODO maybe change to a sound and effect. Also add to config.yml
			thisMatch.sendMessage(ChatColor.GRAY + scoringPlayer.getName() + ChatColor.YELLOW + " has scored a Goal!!! "); 
			thisMatch.sendMessage(ChatColor.GRAY + teamOne.getName() + ": " + ChatColor.GOLD + teamOne.getNKills() + " " +
				ChatColor.GRAY + teamTwo.getName() + ": " + ChatColor.GOLD + teamTwo.getNKills());
			// Send ball to center
			Vector stop = new Vector(0, 0, 0);
			ent.setVelocity(stop);
			ent.teleport(spawnLocs.get(2), TeleportCause.PLUGIN);
			ent.setVelocity(stop);
			// Return players to team spawn
			Set<Player> setOne = teamOne.getBukkitPlayers();
			Set<Player> setTwo = teamTwo.getBukkitPlayers();
			for (Player player : setOne) {player.teleport(match.getArena().getSpawnLoc(0));}
			for (Player player : setTwo) {player.teleport(match.getArena().getSpawnLoc(1));}
		}
	}	
	
	
}