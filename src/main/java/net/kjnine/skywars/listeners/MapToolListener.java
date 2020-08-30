package net.kjnine.skywars.listeners;

import java.util.HashSet;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;
import org.bukkit.material.Wool;
import org.inventivetalent.itembuilder.ItemBuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.kjnine.skywars.SWMapTool;
import net.md_5.bungee.api.ChatColor;

public class MapToolListener implements Listener {

	private SWMapTool pl;
	
	public MapToolListener(SWMapTool pl) {
		this.pl = pl;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(!pl.toolModeData.containsKey(e.getPlayer().getUniqueId())) return;
		e.setCancelled(true);
		Player p = e.getPlayer();
		if(p.getItemInHand() == null || !p.getItemInHand().getType().equals(Material.INK_SACK)) return;
		
		DyeColor col = ((Dye)p.getItemInHand().getData()).getColor();
		if(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			Block target = e.getClickedBlock();
			if(e.getAction().equals(Action.RIGHT_CLICK_AIR)) target = p.getTargetBlock(null, 50);
			Location ref = target.getWorld().getBlockAt(0, 50, 0).getLocation();
			if(col.equals(DyeColor.LIME)) {
				if(!pl.curIslandData.containsKey(p.getUniqueId())) {
					JsonObject island = new JsonObject();
					JsonObject bloc = new JsonObject();
					bloc.addProperty("x", target.getX() - ref.getBlockX());
					bloc.addProperty("y", target.getY() - ref.getBlockY());
					bloc.addProperty("z", target.getZ() - ref.getBlockZ());
					island.add("spawnPoint", bloc);
					pl.curIslandData.put(p.getUniqueId(), island);
					p.sendMessage(ChatColor.GREEN + "Started Island with Spawn " + bloc.toString());
					pl.spawnsReplaced.put(target, target.getType());
					target.setType(Material.WOOL);
					Wool data = new Wool(col);
					target.getState().setRawData(data.getData());
					target.setData(data.getData());
					if(!pl.blocksReplaced.containsKey(p.getUniqueId())) pl.blocksReplaced.put(p.getUniqueId(), new HashSet<>());
					pl.blocksReplaced.get(p.getUniqueId()).add(target);

					ItemStack im = new ItemBuilder(Material.INK_SACK)
							.withData(pl.dye(DyeColor.GRAY))
							.buildMeta()
							.withDisplayName(ChatColor.GRAY + "(Use Blue Dye to Save Island)")
							.item().build();
					im.setDurability(pl.dye(DyeColor.GRAY).getData());
					p.getInventory().setItem(1, im.clone());
					im = new ItemBuilder(Material.INK_SACK)
							.withData(pl.dye(DyeColor.ORANGE))
							.buildMeta()
							.withDisplayName(ChatColor.GOLD + "Island Chest Marker")
							.item().build();
					im.setDurability(pl.dye(DyeColor.ORANGE).getData());
					p.getInventory().setItem(2, im.clone());
					im = new ItemBuilder(Material.INK_SACK)
							.withData(pl.dye(DyeColor.PURPLE))
							.buildMeta()
							.withDisplayName(ChatColor.DARK_PURPLE + "Island BrewingStand Marker")
							.item().build();
					im.setDurability(pl.dye(DyeColor.PURPLE).getData());
					p.getInventory().setItem(3, im.clone());
					im = new ItemBuilder(Material.INK_SACK)
							.withData(pl.dye(DyeColor.LIGHT_BLUE))
							.buildMeta()
							.withDisplayName(ChatColor.AQUA + "Save Island")
							.item().build();
					im.setDurability(pl.dye(DyeColor.LIGHT_BLUE).getData());
					p.getInventory().setItem(4, im.clone());
					p.updateInventory();
				}
			} else if(col.equals(DyeColor.GRAY)) {
				if(pl.curIslandData.containsKey(p.getUniqueId())) {
					JsonObject island = pl.curIslandData.get(p.getUniqueId());
					JsonObject bloc = new JsonObject();
					bloc.addProperty("x", target.getX() - ref.getBlockX());
					bloc.addProperty("y", target.getY() - ref.getBlockY());
					bloc.addProperty("z", target.getZ() - ref.getBlockZ());
					JsonObject spawnPoint = island.get("spawnPoint").getAsJsonObject();
					Location spawn = ref.clone().add( 
							spawnPoint.get("x").getAsInt(), 
							spawnPoint.get("y").getAsInt(), 
							spawnPoint.get("z").getAsInt());
					island.remove("spawnPoint");
					island.add("spawnPoint", bloc);
					p.sendMessage(ChatColor.GREEN + "Updated Island Spawn " + bloc.toString());
					Block b = ref.getWorld().getBlockAt(spawn);
					b.setType(pl.spawnsReplaced.getOrDefault(b, Material.GRASS));
					pl.spawnsReplaced.remove(b);
					if(pl.blocksReplaced.containsKey(p.getUniqueId())) pl.blocksReplaced.get(p.getUniqueId()).remove(b);
					pl.spawnsReplaced.put(target, target.getType());
					target.setType(Material.WOOL);
					Wool data = new Wool(DyeColor.LIME);
					target.getState().setRawData(data.getData());
					target.setData(data.getData());
					if(!pl.blocksReplaced.containsKey(p.getUniqueId())) pl.blocksReplaced.put(p.getUniqueId(), new HashSet<>());
					pl.blocksReplaced.get(p.getUniqueId()).add(target);
				}
			} else if(col.equals(DyeColor.LIGHT_BLUE)) {
				if(pl.curIslandData.containsKey(p.getUniqueId())) {
					JsonObject jo = pl.toolModeData.get(p.getUniqueId());
					if(!jo.has("islands")) jo.add("islands", new JsonArray());
					JsonArray islands = jo.get("islands").getAsJsonArray();
					JsonObject curIsland = pl.curIslandData.get(p.getUniqueId());
					islands.add(curIsland);
					pl.curIslandData.remove(p.getUniqueId());
					p.sendMessage(ChatColor.GREEN + "Saved Island[#" + (islands.size() - 1) + "]");
					ItemStack im = new ItemBuilder(Material.INK_SACK)
							.withData(pl.dye(DyeColor.LIME))
							.buildMeta()
							.withDisplayName(ChatColor.GREEN + "Begin Island (Spawnpoint)")
							.item().build();
					im.setDurability(pl.dye(DyeColor.LIME).getData());
					p.getInventory().setItem(1, im);
					p.getInventory().setItem(2, null);
					p.getInventory().setItem(3, null);
					p.getInventory().setItem(4, null);
					p.updateInventory();
				}
			} else if(target.getType().equals(Material.CHEST)) {
				if(col.equals(DyeColor.YELLOW)) {
					JsonObject mainMap = pl.toolModeData.get(p.getUniqueId());
					if(!mainMap.has("center")) mainMap.add("center", new JsonObject());
					JsonObject center = mainMap.get("center").getAsJsonObject();
					if(!center.has("chests")) center.add("chests", new JsonArray());
					JsonArray chests = center.get("chests").getAsJsonArray();
					JsonObject bloc = new JsonObject();
					bloc.addProperty("x", target.getX() - ref.getBlockX());
					bloc.addProperty("y", target.getY() - ref.getBlockY());
					bloc.addProperty("z", target.getZ() - ref.getBlockZ());
					chests.add(bloc);
					p.sendMessage(ChatColor.GREEN + "Added Center Chest " + bloc.toString());
					target.setType(Material.WOOL);
					Wool data = new Wool(col);
					target.getState().setRawData(data.getData());
					target.setData(data.getData());
					if(!pl.blocksReplaced.containsKey(p.getUniqueId())) pl.blocksReplaced.put(p.getUniqueId(), new HashSet<>());
					pl.blocksReplaced.get(p.getUniqueId()).add(target);
				} else if(col.equals(DyeColor.ORANGE)) {
					if(pl.curIslandData.containsKey(p.getUniqueId())) {
						JsonObject island = pl.curIslandData.get(p.getUniqueId());
						if(!island.has("chests")) island.add("chests", new JsonArray());
						JsonArray chests = island.get("chests").getAsJsonArray();
						JsonObject bloc = new JsonObject();
						bloc.addProperty("x", target.getX() - ref.getBlockX());
						bloc.addProperty("y", target.getY() - ref.getBlockY());
						bloc.addProperty("z", target.getZ() - ref.getBlockZ());
						chests.add(bloc);
						p.sendMessage(ChatColor.GREEN + "Added Island Chest " + bloc.toString());
						target.setType(Material.WOOL);
						Wool data = new Wool(col);
						target.getState().setRawData(data.getData());
						target.setData(data.getData());
						if(!pl.blocksReplaced.containsKey(p.getUniqueId())) pl.blocksReplaced.put(p.getUniqueId(), new HashSet<>());
						pl.blocksReplaced.get(p.getUniqueId()).add(target);
					}
				}
			} else if(target.getType().equals(Material.BREWING_STAND)) {
				if(col.equals(DyeColor.MAGENTA)) {
					JsonObject mainMap = pl.toolModeData.get(p.getUniqueId());
					if(!mainMap.has("center")) mainMap.add("center", new JsonObject());
					JsonObject center = mainMap.get("center").getAsJsonObject();
					if(!center.has("brewingStands")) center.add("brewingStands", new JsonArray());
					JsonArray chests = center.get("brewingStands").getAsJsonArray();
					JsonObject bloc = new JsonObject();
					bloc.addProperty("x", target.getX() - ref.getBlockX());
					bloc.addProperty("y", target.getY() - ref.getBlockY());
					bloc.addProperty("z", target.getZ() - ref.getBlockZ());
					chests.add(bloc);
					p.sendMessage(ChatColor.GREEN + "Added Center BrewingStand " + bloc.toString());
					target.setType(Material.WOOL);
					Wool data = new Wool(col);
					target.getState().setRawData(data.getData());
					target.setData(data.getData());
					if(!pl.blocksReplaced.containsKey(p.getUniqueId())) pl.blocksReplaced.put(p.getUniqueId(), new HashSet<>());
					pl.blocksReplaced.get(p.getUniqueId()).add(target);
				} else if(col.equals(DyeColor.PURPLE)) {
					if(pl.curIslandData.containsKey(p.getUniqueId())) {
						JsonObject island = pl.curIslandData.get(p.getUniqueId());
						if(!island.has("brewingStands")) island.add("brewingStands", new JsonArray());
						JsonArray chests = island.get("brewingStands").getAsJsonArray();
						JsonObject bloc = new JsonObject();
						bloc.addProperty("x", target.getX() - ref.getBlockX());
						bloc.addProperty("y", target.getY() - ref.getBlockY());
						bloc.addProperty("z", target.getZ() - ref.getBlockZ());
						chests.add(bloc);
						p.sendMessage(ChatColor.GREEN + "Added Island BrewingStand " + bloc.toString());
						target.setType(Material.WOOL);
						Wool data = new Wool(col);
						target.getState().setRawData(data.getData());
						target.setData(data.getData());
						if(!pl.blocksReplaced.containsKey(p.getUniqueId())) pl.blocksReplaced.put(p.getUniqueId(), new HashSet<>());
						pl.blocksReplaced.get(p.getUniqueId()).add(target);
					}
				}
			}
		}
	}
	
	
	@EventHandler
	public void onInv(InventoryClickEvent e) {
		if(pl.toolModeData.containsKey(e.getWhoClicked().getUniqueId())) e.setCancelled(true);
	}
	
}
