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

package me.morrango.arenafutbol.commands;

import mc.alk.arena.executors.CustomCommandExecutor;
import mc.alk.arena.executors.MCCommand;
import me.morrango.arenafutbol.ArenaFutbol;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandExecutor_ArenaFutbol extends CustomCommandExecutor {


	@MCCommand(cmds = { "ball" }, op = true, admin = true)
	public boolean ball(CommandSender sender) {
		Player player = (Player)sender;
		ItemStack itemInHand = player.getItemInHand();
		ArenaFutbol.plugin.getConfig().set("ball", itemInHand);
		ArenaFutbol.plugin.saveConfig();
		sender.sendMessage(ChatColor.GREEN + "Ball set to " + itemInHand);
		return true;
	}

	@MCCommand(cmds = { "balltimer" }, op = true, admin = true)
	public boolean balltimer(CommandSender sender, int timer) {
		int newInt = timer;
		ArenaFutbol.plugin.getConfig().set("balltimer", newInt);
		ArenaFutbol.plugin.saveConfig();
		sender.sendMessage(ChatColor.GREEN + "Ball posession timer set to "
				+ timer + " Seconds");
		return true;
	}

	@MCCommand(cmds = { "pitch" }, op = true, admin = true)
	public boolean pitch(CommandSender sender, int pitch) {
		if (pitch > 90 || pitch < 0) {
			return false;
		}
		ArenaFutbol.plugin.getConfig().set("pitch", pitch);
		ArenaFutbol.plugin.saveConfig();
		sender.sendMessage(ChatColor.GREEN + "Pitch adjustment set to " + pitch
				+ " degrees");
		return true;
	}

	@MCCommand(cmds = { "maxpitch" }, op = true, admin = true)
	public boolean maxpitch(CommandSender sender, int pitch) {
		if (pitch > 90 || pitch < 0) {
			return false;
		}
		ArenaFutbol.plugin.getConfig().set("maxpitch", pitch);
		ArenaFutbol.plugin.saveConfig();
		sender.sendMessage(ChatColor.GREEN + "Maximum Pitch set to " + pitch
				+ " degrees");
		return true;
	}

	@MCCommand(cmds = { "power" }, op = true, admin = true)
	public boolean power(CommandSender sender, double newDouble) {
		if (newDouble > 2.0) {
			return false;
		}
		ArenaFutbol.plugin.getConfig().set("power", newDouble);
		ArenaFutbol.plugin.saveConfig();
		sender.sendMessage(ChatColor.GREEN + "Power adjustment set to "
				+ (int) (newDouble * 100) + "%");
		return true;

	}
	
	@MCCommand(cmds = { "particles" }, op = true, admin = true)
	public boolean particles(CommandSender sender, boolean particles) {
		ArenaFutbol.plugin.getConfig().set("particles", particles);
		ArenaFutbol.plugin.saveConfig();
		sender.sendMessage(ChatColor.GREEN + "Particles set to "
				+ particles + " changes may not take effect until server reload/restart.");
		return true;

	}
}
