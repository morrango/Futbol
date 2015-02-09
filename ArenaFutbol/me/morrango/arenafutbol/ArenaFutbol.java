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
 */

package me.morrango.arenafutbol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Logger;

import mc.alk.arena.BattleArena;
import me.morrango.arenafutbol.commands.CommandExecutor_ArenaFutbol;
import me.morrango.arenafutbol.listeners.FutbolArena;
import me.morrango.arenafutbol.tasks.Task_PlayEffect;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class ArenaFutbol extends JavaPlugin {
	private Logger log;
	private PluginDescriptionFile description;
	private String prefix;
	public static ArenaFutbol plugin;
	public static HashSet<Entity> balls = new HashSet<Entity>();
	public HashMap<UUID, Vector> vectors = new HashMap<UUID, Vector>();
	private boolean particles = false;

	@Override
	public void onEnable() {
		plugin = this;
		log = Logger.getLogger("Minecraft");
		description = getDescription();
		prefix = "[" + description.getName() + "] ";
		log("loading " + description.getFullName());

		if (this.getConfig().getBoolean("particles")) {
			this.particles = true;
		}
		else {
			this.particles = false;
		}

		this.loadConfig();
		log(this.getServer().getVersion());
		BattleArena.registerCompetition(this, "Futbol", "fb",
				FutbolArena.class, new CommandExecutor_ArenaFutbol());
		getServer().getScheduler().scheduleSyncRepeatingTask(this,
				new Task_PlayEffect(this), 1L, 1L);

		// try {
		// Metrics metrics = new Metrics(this);
		// metrics.start();
		// } catch (IOException e) {
		// // Failed to submit the stats :-(
		// }

	}

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelAllTasks();
		log("disabled " + description.getFullName());
	}

	public void log(String message) {
		log.info(prefix + message);
	}

	public void loadConfig() {
		this.getConfig().addDefault("particles", false);
		this.getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		saveConfig();
	}

	public void doBallPhysics() {
		for (Entity ball : ArenaFutbol.balls) {
			UUID uuid = ball.getUniqueId();
			Vector velocity = ball.getVelocity();
			if (this.vectors.containsKey(uuid)) {
				velocity = (Vector) this.vectors.get(uuid);
			}
			Vector newVector = ball.getVelocity();
			if (newVector.getX() == 0.0D) {
				newVector.setX(-velocity.getX() * 0.9D);
			} else if (Math.abs(velocity.getX() - newVector.getX()) < 0.15D) {
				newVector.setX(velocity.getX() * 0.975D);
			}
			if ((newVector.getY() == 0.0D) && (velocity.getY() < -0.1D)) {
				newVector.setY(-velocity.getY() * 0.9D);
			}
			if (newVector.getZ() == 0.0D) {
				newVector.setZ(-velocity.getZ() * 0.9D);
			} else if (Math.abs(velocity.getZ() - newVector.getZ()) < 0.15D) {
				newVector.setZ(velocity.getZ() * 0.975D);
			}
			ball.setVelocity(newVector);
			this.vectors.put(uuid, newVector);
			if (particles) {
				showEffect(ball);
			}
		}
	}

	public void showEffect(Entity entity) {
		Location location = entity.getLocation();
		World world = entity.getWorld();
		world.playEffect(location, Effect.INSTANT_SPELL, 0, 128);
	}

}
