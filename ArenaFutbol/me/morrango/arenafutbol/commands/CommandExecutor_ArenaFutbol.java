package me.morrango.arenafutbol.commands;

import me.morrango.arenafutbol.ArenaFutbol;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class CommandExecutor_ArenaFutbol implements CommandExecutor {
	private ArenaFutbol plugin;

	public CommandExecutor_ArenaFutbol(ArenaFutbol plugin){
		this.plugin = plugin;
	}

	@EventHandler
	public boolean onCommand(CommandSender sender, Command command,String label, String[] args) {
		if (command.getName().equalsIgnoreCase("arenafutbol")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Y U NO PLAYER??!111");
				return true;
			}
			if (sender.isOp()) {
				if (args.length == 2) {
					if (args[0].equalsIgnoreCase("ball")) {
						int newInt = Integer.parseInt(args[1]);
						plugin.getConfig().set("ball", newInt);
						plugin.saveConfig();
						sender.sendMessage(ChatColor.GREEN + "Ball set to Item ID " + newInt);
					}
					if (args[0].equalsIgnoreCase("balltimer")) {
						int prsInt = Integer.parseInt(args[1]);
						int newInt = prsInt*20;
						plugin.getConfig().set("balltimer", newInt);
						plugin.saveConfig();
						sender.sendMessage(ChatColor.GREEN + "Ball posession timer set to " + prsInt + " Seconds");
					}
					if (args[0].equalsIgnoreCase("sety")) {
						try {
						double newDouble = Double.parseDouble(args[1]);
						if (newDouble > 1.0) {return false;}
						plugin.getConfig().set("y", newDouble);
						plugin.saveConfig();
						sender.sendMessage(ChatColor.GREEN + "Y Axis adjustment set to  " + newDouble);
						}catch(NumberFormatException ex) {
							return false;
						}
					}
					
					if (args[0].equalsIgnoreCase("maxy")) {
						try {
							double newDouble = Double.parseDouble(args[1]);
							if (newDouble > 1.0) {return false;}
							plugin.getConfig().set("maxy", newDouble);
							plugin.saveConfig();
							sender.sendMessage(ChatColor.GREEN + "Maximum Y axis set to " + newDouble);
						}catch(NumberFormatException ex) {
							return false;
						}
					}
					return true;
				}
			}
	}
	return false;
	}
}
