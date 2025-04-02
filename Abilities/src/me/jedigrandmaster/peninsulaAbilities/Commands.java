package me.jedigrandmaster.peninsulaAbilities;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class Commands implements CommandExecutor, TabCompleter {
	private AbilityManager manager;
	private ArrayList<String> abilityNames;
	
	public Commands(AbilityManager manager, ArrayList<String> abilityNames) {
		this.manager = manager;
		this.abilityNames = abilityNames;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("addability")) {
			if(!sender.hasPermission("peninsula.addability")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to do this");
				return true;
			}
			
			if(args.length < 2) return false;
			OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
			this.manager.addAbility(player, args[1]);
			
			sender.sendMessage(ChatColor.GREEN + "Ability added");
			
			return true;
		}
		if(command.getName().equalsIgnoreCase("removeability")) {
			if(!sender.hasPermission("peninsula.removeability")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to do this");
				return true;
			}
			
			if(args.length < 2) return false;
			OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
			this.manager.removeAbility(player, args[1]);
			
			sender.sendMessage(ChatColor.GREEN + "Ability removed");
			
			return true;
		}
		
		
		return false;
	}
	
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if(command.getName().equalsIgnoreCase("addability")) {
			if(args.length == 2) return this.abilityNames;
		}
		if(command.getName().equalsIgnoreCase("removeability")) {
			if(args.length < 2) return null;
			OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
			if(this.manager.getAbilities(target) == null) return null;
			return new ArrayList<String>(this.manager.getAbilities(target));
		}
		return null;
	}
	
}
