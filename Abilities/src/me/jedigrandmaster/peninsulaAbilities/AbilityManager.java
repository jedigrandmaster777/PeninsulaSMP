package me.jedigrandmaster.peninsulaAbilities;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.OfflinePlayer;

public class AbilityManager {
	private HashMap<String, Set<String>> abilities;
	
	public AbilityManager(PeninsulaAbilities plugin) {
		try {
			FileInputStream fileIn = new FileInputStream("plugins/abilities/abilities.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			this.abilities = (HashMap<String, Set<String>>) in.readObject();
			
			in.close();
			fileIn.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			this.abilities = new HashMap<String, Set<String>>();
		}
	}
	
	public void save() {
		try {
			FileOutputStream fileOut = new FileOutputStream("plugins/abilities/abilities.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this.abilities);
			out.close();
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public void addAbility(OfflinePlayer player, String ability) {
		String uuid = player.getUniqueId().toString();
		Set<String> set = this.abilities.get(uuid);
		if(set == null) {
			set = new HashSet<String>();
			this.abilities.put(uuid, set);
		}
		
		set.add(ability);
	}
	
	public void removeAbility(OfflinePlayer player, String ability) {
		String uuid = player.getUniqueId().toString();
		Set<String> set = this.abilities.get(uuid);
		if(set == null) {
			return;
		}
		
		set.remove(ability);
	}
	
	public Set<String> getAbilities(OfflinePlayer player) {
		Set<String> set = this.abilities.get(player.getUniqueId().toString());
		if(set == null) return new HashSet<String>(); //maybe change
		return set;
	}
}
