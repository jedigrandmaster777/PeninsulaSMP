package me.jedigrandmaster.peninsulaAbilities;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.jedigrandmaster.peninsulaAbilities.abilities.AlienAbility;
import me.jedigrandmaster.peninsulaAbilities.abilities.ArcherAbility;
import me.jedigrandmaster.peninsulaAbilities.abilities.AxeAbility;
import me.jedigrandmaster.peninsulaAbilities.abilities.CreeperAbility;
import me.jedigrandmaster.peninsulaAbilities.abilities.DragonAbility;
import me.jedigrandmaster.peninsulaAbilities.abilities.FishAbility;
import me.jedigrandmaster.peninsulaAbilities.abilities.ForceAbility;
import me.jedigrandmaster.peninsulaAbilities.abilities.JesusAbility;
import me.jedigrandmaster.peninsulaAbilities.abilities.LizardAbility;
import me.jedigrandmaster.peninsulaAbilities.abilities.MothAbility;
import me.jedigrandmaster.peninsulaAbilities.abilities.NetheranAbility;
import me.jedigrandmaster.peninsulaAbilities.abilities.WelsAbility;


public class PeninsulaAbilities extends JavaPlugin {
	private AbilityManager manager;
	
	private void registerAbility(ArrayList<String> names, Ability ability) {
		ability.setManager(this.manager);
		Bukkit.getPluginManager().registerEvents(ability, this);
		names.add(ability.getName());
	}
	
	@Override
	public void onEnable() {
		ArrayList<String> names = new ArrayList<String>();
		this.manager = new AbilityManager(this);
		
		registerAbility(names, new FishAbility(this));
		registerAbility(names, new DragonAbility(this));
		registerAbility(names, new ForceAbility(this));
		registerAbility(names, new NetheranAbility(this));
		registerAbility(names, new AlienAbility(this));
		registerAbility(names, new AxeAbility(this));
		registerAbility(names, new WelsAbility(this));
		registerAbility(names, new MothAbility(this));
		registerAbility(names, new LizardAbility(this));
		registerAbility(names, new ArcherAbility(this));
		registerAbility(names, new CreeperAbility(this));
		registerAbility(names, new JesusAbility(this));
		
		Commands commands = new Commands(this.manager, names);
		
		PluginCommand add = this.getCommand("addability");
		add.setExecutor(commands);
		add.setTabCompleter(commands);
		
		PluginCommand remove = this.getCommand("removeability");
		remove.setExecutor(commands);
		remove.setTabCompleter(commands);
		
		PluginCommand setData = this.getCommand("updateitem");
		setData.setExecutor(new CreateItemCommand(this));
	}
	
	@Override
	public void onDisable() {
		this.manager.save();
	}
}
