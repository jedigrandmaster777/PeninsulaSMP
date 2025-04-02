package me.jedigrandmaster.peninsulaAbilities.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class ItemManager {
	private Predicate<ItemStack> pred;
	private HashMap<Player, List<ItemStack>> keep;
	
	public ItemManager(Predicate<ItemStack> pred) {
		this.pred = pred;
		this.keep = new HashMap<Player, List<ItemStack>>();
	}
	
	public void onDeath(PlayerDeathEvent e) {
		Player player = e.getEntity();
		
		List<ItemStack> drops = e.getDrops();
		ArrayList<ItemStack> keep = new ArrayList<ItemStack>();
		
		for(int i = 0; i < drops.size(); i++) {
			ItemStack drop = drops.get(i);
			if(!this.pred.test(drop)) continue;
			
			drops.remove(i);
			keep.add(drop);
			i--;
		}
		
		if(keep.size() > 0) this.keep.put(player, keep);
	}
	
	public void onRespawn(PlayerRespawnEvent e) {
		Player player = e.getPlayer();
		List<ItemStack> list = this.keep.get(player);
		if(list == null) return;
		
		for(ItemStack item : list) {
			player.getInventory().addItem(item);
		}
		
		this.keep.remove(player);
	}
}
