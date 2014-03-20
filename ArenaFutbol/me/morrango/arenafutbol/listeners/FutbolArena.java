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
import java.util.Random;
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
import mc.alk.arena.objects.spawns.SpawnLocation;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.scoreboardapi.api.SEntry;
import mc.alk.scoreboardapi.api.SObjective;
import mc.alk.scoreboardapi.api.SScoreboard;
import mc.alk.scoreboardapi.scoreboard.SAPIDisplaySlot;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class FutbolArena extends Arena {
	public FutbolArena() {
	}

	public Plugin plugin = Bukkit.getPluginManager().getPlugin("ArenaFutbol");
	public HashMap<Entity, Player> kickedBy = new HashMap<Entity, Player>();
	public HashMap<Entity, Match> kickedBalls = new HashMap<Entity, Match>();
	public HashMap<Match, Entity> cleanUpList = new HashMap<Match, Entity>();
	private HashMap<ArenaTeam, Integer> ballTimers = new HashMap<ArenaTeam, Integer>();
	public Set<ArenaTeam> canKick = new HashSet<ArenaTeam>();
	private Random random = new Random();


	public void createFireWork(Location loc, Color teamColor) {
		if (loc != null && teamColor != null) {
			World w = loc.getWorld();
			Entity firework = w.spawnEntity(
					new Location(w, loc.getX() + random.nextGaussian() * 3, loc
							.getY() - 2, loc.getZ() + random.nextGaussian() * 3),
					EntityType.FIREWORK);
			Firework fw = (Firework) firework;
			FireworkMeta meta = fw.getFireworkMeta();
			Builder builder = FireworkEffect.builder();

			builder.withColor(teamColor);

			switch (random.nextInt(4)) {
			case 0:
				builder.with(FireworkEffect.Type.BALL_LARGE);
				break;
			case 1:
				builder.with(FireworkEffect.Type.BALL_LARGE);
				break;
			case 2:
				builder.with(FireworkEffect.Type.BALL_LARGE);
				break;
			case 3:
				builder.with(FireworkEffect.Type.BALL_LARGE);
				break;
			default:
				builder.with(FireworkEffect.Type.CREEPER);
				break;
			}
			builder.trail(false);
			meta.addEffect(builder.build());
			meta.setPower(0);
			fw.setFireworkMeta(meta);

		}
	}

	@Override
	public void onOpen() {
		Set<ArenaPlayer> set = match.getPlayers();
		 List<ArenaTeam> teamsList = match.getArena().getTeams();
		 String teamOne = teamsList.get(0).getDisplayName();
		 String teamTwo = teamsList.get(1).getDisplayName();
		// Create the scoreboard
		SScoreboard scoreboard = getMatch().getScoreboard();
		SObjective objective = scoreboard.registerNewObjective(
				"futbolObjective", "totalKillCount", "&6Time", SAPIDisplaySlot.SIDEBAR);
		
		Iterable<SObjective> objectives = scoreboard.getObjectives();
		for (SObjective sObjective : objectives) {
			sObjective.setDisplayPlayers(false);
			sObjective.setDisplayTeams(false);
		}

		 SEntry team1 = scoreboard.createEntry(teamOne, "&4" + teamOne);
		 SEntry team2 = scoreboard.createEntry(teamTwo, "&4" + teamTwo);
//		 Add the entries to the objective
		 objective.addEntry(team1, 0);
		 objective.addEntry(team2, 0);
		for (ArenaPlayer arenaPlayer : set) {
			scoreboard.setScoreboard((Player) arenaPlayer);
		}
	}

	@Override
	public void onStart() {
		List<ArenaTeam> teamsList = match.getArena().getTeams();
		SpawnLocation loc = getSpawn(2, false);
		Location location = loc.getLocation();
		World world = location.getWorld();
		ItemStack is = plugin.getConfig().getItemStack("ball");
		Location center = fixCenter(world, location);
		world.dropItem(center, is);
		for (ArenaTeam t : teamsList) {
			canKick.add(t);
		}
	}

	@Override
	public void onVictory(MatchResult result) {
		removeBalls(getMatch());
		removeArenaTeams(getMatch());
		List<ArenaTeam> teamsList = match.getArena().getTeams();
		int teamOne = teamsList.get(0).getNKills();
		int teamTwo = teamsList.get(1).getNKills();
		if (teamOne == teamTwo) {
			match.setDraw();
		}
		if (teamOne > teamTwo) {
			match.setWinner(teamsList.get(0));
		} else {
			match.setWinner(teamsList.get(1));
		}
	}

	@Override
	public void onCancel() {
		removeBalls(getMatch());
		removeArenaTeams(getMatch());
	}

	@ArenaEventHandler
	public void matchMessages(MatchMessageEvent event) {
		MatchState state = event.getState();
		if (!state.equals(MatchState.ONMATCHINTERVAL)) {
			event.setMatchMessage("");
		}
	}

	@ArenaEventHandler
	public void onPlayerAnimation(PlayerAnimationEvent event) {
		Player player = event.getPlayer();
		ArenaPlayer arenaPlayer = getAP(player);
		ArenaTeam kickersTeam = getTeam(arenaPlayer);
		List<Entity> ent = player.getNearbyEntities(1, 1, 1);
		for (Entity entity : ent) {
			if (entity instanceof Item && canKick.contains(kickersTeam)) {
				List<ArenaTeam> teamsList = match.getArena().getTeams();
				Location location = player.getLocation();
				World world = player.getWorld();
				entity.setVelocity(kickVector(player));
				world.playEffect(location, Effect.STEP_SOUND, 10);
				kickedBy.put(entity, player);
				kickedBalls.put(entity, getMatch());
				cleanUpList.put(getMatch(), entity);
				for (ArenaTeam t : teamsList) {
					if (!canKick.contains(t)) {
						canKick.add(t);
						cancelBallTimer(t);
					}
				}
			}
		}
	}

	@ArenaEventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if (event.isCancelled()) {
			return;
		}
		event.setCancelled(true);
	}

	@ArenaEventHandler(priority = EventPriority.HIGHEST, needsPlayer = false)
	public void onGoalScored(EntityInteractEvent event) {
		SObjective objective = getMatch().getScoreboard().getObjective(
				"futbolObjective");
		Entity ent = event.getEntity();
		if (ent instanceof Item && kickedBalls.containsKey(ent)
				&& kickedBy.get(ent) != null) {
			World world = ent.getWorld();
			Location loc = event.getEntity().getLocation();
			Location center = fixCenter(world,
					match.getArena().getSpawn(2, false).getLocation());
			// ArenaTeam goals are set by the blocks below the pressure plates
			Block block = loc.getBlock().getRelative(BlockFace.DOWN);
			Material material = block.getType();
			event.setCancelled(true);
			Match thisMatch = kickedBalls.get(ent);
			List<ArenaTeam> teamsList = thisMatch.getArena().getTeams();
			ArenaTeam teamOne = teamsList.get(0);
			ArenaTeam teamTwo = teamsList.get(1);
			ArenaTeam scoringTeam = null;
			if (material.equals(Material.STONE)) {
				scoringTeam = teamsList.get(0);
				objective.setPoints(teamOne.getDisplayName(), teamOne.getNKills());
				createFireWork(center, Color.RED);
			}
			if (material.equals(Material.COBBLESTONE)) {
				scoringTeam = teamsList.get(1);
				createFireWork(center, Color.BLUE);
			}
			ArenaPlayer scoringPlayer = getAP(kickedBy.get(ent));
			// Add kill and send message
			scoringTeam.addKill(scoringPlayer);
			objective.setPoints(scoringTeam.getDisplayName(), scoringTeam.getNKills());
			canKick.remove(scoringTeam);
			startBallTimer(scoringTeam);
			kickedBy.put(ent, null);
			// Send ball to center
			Vector stop = new Vector(0, 0, 0);
			ent.setVelocity(stop);
			ent.teleport(center, TeleportCause.PLUGIN);
			// Return players to team spawn
			Set<Player> setOne = teamOne.getBukkitPlayers();
			Set<Player> setTwo = teamTwo.getBukkitPlayers();
			tpArenaTeams(setOne, setTwo, thisMatch);
		}
	}

	@ArenaEventHandler(needsPlayer = false)
	public void onItemDespawn(ItemDespawnEvent event) {
		if (kickedBalls.containsKey(event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@ArenaEventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		event.setFoodLevel(20);
	}

	private void startBallTimer(final ArenaTeam team) {
		cancelBallTimer(team);
		int ballTimer = plugin.getConfig().getInt("balltimer");
		BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin,
				new Runnable() {
					@Override
					public void run() {
						canKick.add(team);
					}
				}, (ballTimer * 20) + 60);
		ballTimers.put(team, task.getTaskId());
	}

	public void tpArenaTeams(final Set<Player> setOne,
			final Set<Player> setTwo, final Match match) {
		for (Player player : setOne) {
			player.teleport(match.getArena().getSpawn(0, false).getLocation());
		}
		for (Player player : setTwo) {
			player.teleport(match.getArena().getSpawn(1, false).getLocation());
		}
	}

	private void cancelBallTimer(ArenaTeam team) {
		Integer timerid = ballTimers.get(team);
		if (timerid != null) {
			Bukkit.getScheduler().cancelTask(timerid);
		}
	}

	public ArenaPlayer getAP(Player player) {
		ArenaPlayer ap = BattleArena.toArenaPlayer(player);
		return ap;
	}

	public Location fixCenter(World world, Location origin) {
		Location center = new Location(world, origin.getX(),
				origin.getY() + 1.0, origin.getZ());
		return center;
	}

	public Vector kickVector(Player player) {
		float configAdjPitch = -(float) plugin.getConfig().getInt("pitch");
		float configMaxPitch = -(float) plugin.getConfig().getInt("maxpitch");
		double configPower = plugin.getConfig().getDouble("power");
		Location loc = player.getEyeLocation();
		float pitch = loc.getPitch();
		pitch = pitch + configAdjPitch;
		// Bukkit.broadcastMessage("adj" + configAdjPitch + "max " +
		// configMaxPitch);
		if (pitch > 0) {
			pitch = 0.0f;
		}
		if (pitch < configMaxPitch) {
			pitch = 0.0f + configMaxPitch;
		}
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

}