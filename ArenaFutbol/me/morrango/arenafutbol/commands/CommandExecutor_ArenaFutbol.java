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

public class CommandExecutor_ArenaFutbol extends CustomCommandExecutor {


	@MCCommand(cmds = { "ball" }, inGame = true, admin = true)
	public boolean ball(CommandSender sender, int itemId) {
		ArenaFutbol.plugin.getConfig().set("ball", itemId);
		ArenaFutbol.plugin.saveConfig();
		sender.sendMessage(ChatColor.GREEN + "Ball set to Item ID " + itemId);
		return true;
	}

	@MCCommand(cmds = { "balltimer" }, inGame = true, admin = true)
	public boolean balltimer(CommandSender sender, int timer) {
		int newInt = timer * 20;
		ArenaFutbol.plugin.getConfig().set("balltimer", newInt);
		ArenaFutbol.plugin.saveConfig();
		sender.sendMessage(ChatColor.GREEN + "Ball posession timer set to "
				+ timer + " Seconds");
		return true;
	}

	@MCCommand(cmds = { "pitch" }, inGame = true, admin = true)
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

	@MCCommand(cmds = { "maxpitch" }, inGame = true, admin = true)
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

	@MCCommand(cmds = { "power" }, inGame = true, admin = true)
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

	// @EventHandler
	// public boolean onCommand(CommandSender sender, Command command,String
	// label, String[] args) {
	// if (command.getName().equalsIgnoreCase("arenafutbol")) {
	// if (!(sender instanceof Player)) {
	// sender.sendMessage("Y U NO PLAYER??!111");
	// return true;
	// }
	// if (sender.isOp()) {
	// if (args.length == 2) {
	// if (args[0].equalsIgnoreCase("ball")) {
	// int newInt = Integer.parseInt(args[1]);
	// plugin.getConfig().set("ball", newInt);
	// plugin.saveConfig();
	// sender.sendMessage(ChatColor.GREEN + "Ball set to Item ID " + newInt);
	// return true;
	// }
	// if (args[0].equalsIgnoreCase("balltimer")) {
	// int prsInt = Integer.parseInt(args[1]);
	// int newInt = prsInt*20;
	// plugin.getConfig().set("balltimer", newInt);
	// plugin.saveConfig();
	// sender.sendMessage(ChatColor.GREEN + "Ball posession timer set to " +
	// prsInt + " Seconds");
	// return true;
	// }
	// if (args[0].equalsIgnoreCase("pitch")) {
	// int prsInt = Integer.parseInt(args[1]);
	// if (prsInt > 90 ||prsInt < 0) {return false;}
	// plugin.getConfig().set("pitch", prsInt);
	// plugin.saveConfig();
	// sender.sendMessage(ChatColor.GREEN + "Pitch adjustment set to " + prsInt
	// + " degrees");
	// return true;
	// }
	// if (args[0].equalsIgnoreCase("maxPitch")) {
	// int prsInt = Integer.parseInt(args[1]);
	// if (prsInt > 90 ||prsInt < 0) {return false;}
	// plugin.getConfig().set("maxpitch", prsInt);
	// plugin.saveConfig();
	// sender.sendMessage(ChatColor.GREEN + "Maximum Pitch set to " + prsInt +
	// " degrees");
	// return true;
	// }
	// if (args[0].equalsIgnoreCase("power")) {
	// try {
	// double newDouble = Double.parseDouble(args[1]);
	// if (newDouble > 2.0) {return false;}
	// plugin.getConfig().set("power", newDouble);
	// plugin.saveConfig();
	// sender.sendMessage(ChatColor.GREEN + "Power adjustment set to " +
	// (int)(newDouble*100) + "%");
	// }catch(NumberFormatException ex) {
	// return false;
	// }
	// return true;
	// }
	// return false;
	// }
	// }
	// }
	// return false;
	// }
}
