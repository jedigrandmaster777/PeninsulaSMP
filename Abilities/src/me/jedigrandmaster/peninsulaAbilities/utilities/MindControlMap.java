package me.jedigrandmaster.peninsulaAbilities.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;

public class MindControlMap {
	private HashMap<Player, HashSet<Creature>> players = new HashMap<Player, HashSet<Creature>>();
	private HashMap<Creature, Player> entities = new HashMap<Creature, Player>();
	
	public MindControlMap setPlayerOfEntity(Creature c, Player p) {
		if(this.entities.get(c) != null) {
			Player pl = entities.get(c);
			if(pl != null) {
				this.players.get(pl).remove(c);
			}
		}
		
		if(this.players.get(p) == null) this.players.put(p, new HashSet<Creature>());
		
		this.entities.put(c, p);
		this.players.get(p).add(c);
		
		return this;
	}
	public MindControlMap removeEntity(Creature c) {
		if(this.entities.get(c) != null) {
			Player pl = this.entities.get(c);
			if(pl != null) {
				this.players.get(pl).remove(c);
			}
			
			this.entities.remove(c);
		}
		
		return this;
	}
	/*
	public MindControlMap createPlayer(Player p) {
		this.players.put(p, new HashSet<Creature>());
		
		return this;
	}
	*/
	public MindControlMap deletePlayer(Player p) {
		ArrayList<Creature> a = new ArrayList<Creature>(this.getListOfPlayer(p));
		
		for(Creature c : a) {
			this.entities.remove(c);
		}
		
		players.remove(p);
		
		return this;
	}
	
	public Player getPlayerOfEntity(Creature c) {
		return this.entities.get(c);
	}
	
	public Set<Creature> getListOfPlayer(Player p){
		return this.players.get(p);
	}
	
	public void log(Player p) {
		p.sendMessage("Player map: "+this.players);
		p.sendMessage("Entity map: "+this.entities);
	}
	
}
