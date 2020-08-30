package net.kjnine.skywars;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;
import org.bukkit.material.Wool;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.itembuilder.ItemBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.kjnine.skywars.listeners.MapToolListener;
import net.md_5.bungee.api.ChatColor;

public class SWMapTool extends JavaPlugin {

	public Map<UUID, JsonObject> toolModeData = new HashMap<>();
	public Map<UUID, JsonObject> curIslandData = new HashMap<>();
	private Map<UUID, String> toolModeMapNames = new HashMap<>();
	public Map<UUID, Set<Block>> blocksReplaced = new HashMap<>();
	public Map<Block, Material> spawnsReplaced = new HashMap<>();
	public Map<UUID, Block> lastTarget = new HashMap<>();
	
	private File outFolder;
	
	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		return new EmptyWorldGenerator();
	}
	
	@Override
	public void onEnable() {
		initCommands();
		outFolder = new File(getDataFolder(), "map-saves");
		outFolder.mkdirs();
		getServer().getPluginManager().registerEvents(new MapToolListener(this), this);
		BlockVisualizer vis = new BlockVisualizer(this);
		vis.runTaskTimer(this, 200L, 1L);
	}

	
	private void initCommands() {
		try {
			Field commandMapField = getServer().getClass().getDeclaredField("commandMap");
			commandMapField.setAccessible(true);
			SimpleCommandMap commandMap = (SimpleCommandMap) ((commandMapField.get(getServer())));
			Command cmd = new Command("swmaptool", "Skywars Map Setup command", "Usage: /swmaptool [mapname]", new ArrayList<String>()) {
				@SuppressWarnings("deprecation")
				@Override
				public boolean execute(CommandSender sender, String commandLabel, String[] args) {
					if(!(sender instanceof Player)) return true;
					Player p = (Player) sender;
					if(toolModeData.containsKey(p.getUniqueId())) {
						String outName = toolModeMapNames.getOrDefault(p.getUniqueId(), p.getName());
						File outFile = new File(outFolder, outName + ".json");
						int i = 0;
						while(outFile.exists()) outFile = new File(outFolder, outName + (++i) + ".json");
						try {
							outFile.createNewFile();
							try (BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))) {
								JsonObject jo = toolModeData.get(p.getUniqueId());
								if(curIslandData.containsKey(p.getUniqueId())) {
									if(!jo.has("islands")) jo.add("islands", new JsonArray());
									JsonArray islands = jo.get("islands").getAsJsonArray();
									JsonObject curIsland = curIslandData.get(p.getUniqueId());
									islands.add(curIsland);
									curIslandData.remove(p.getUniqueId());
									p.sendMessage(ChatColor.GREEN + "Saved Island[#" + (islands.size() - 1) + "]");
								}
								Gson gson = new GsonBuilder().setPrettyPrinting().create();
								gson.toJson(jo, bw);
								p.sendMessage(ChatColor.GREEN + "Saved Map Data to " + outFile.getName());
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
						toolModeData.remove(p.getUniqueId());
						toolModeMapNames.remove(p.getUniqueId());
						
						for(Block b : blocksReplaced.getOrDefault(p.getUniqueId(), new HashSet<>())) {
							if(!b.getType().equals(Material.WOOL)) continue;
							Wool wool = (Wool) b.getState().getData();
							if(wool.getColor().equals(DyeColor.YELLOW) || wool.getColor().equals(DyeColor.ORANGE)) {
								b.setType(Material.CHEST);
							} else if(wool.getColor().equals(DyeColor.PURPLE) || wool.getColor().equals(DyeColor.MAGENTA)) {
								b.setType(Material.BREWING_STAND);
							} else {
								b.setType(spawnsReplaced.getOrDefault(b, Material.GRASS));
							}
						}
						
						p.getInventory().clear();
					} else {
						toolModeData.put(p.getUniqueId(), new JsonObject());
						if(args.length > 0) toolModeMapNames.put(p.getUniqueId(), args[0]);
						p.sendMessage(ChatColor.GREEN + "Started Map Setup for " + toolModeMapNames.getOrDefault(p.getUniqueId(), p.getName()) + ".json");
						p.sendMessage(ChatColor.GREEN + "Use the command again to stop and save");
						p.sendMessage(ChatColor.GRAY + "Reference Point: Block[0, 50, 0]");
						
						p.getInventory().clear();
						ItemStack im = new ItemBuilder(Material.INK_SACK)
								.withData(dye(DyeColor.LIME))
								.buildMeta()
								.withDisplayName(ChatColor.GREEN + "Begin Island (Spawnpoint)")
								.item().build();
						im.setDurability(dye(DyeColor.LIME).getData());
						p.getInventory().setItem(1, im.clone());
						im = new ItemBuilder(Material.INK_SACK)
								.withData(dye(DyeColor.YELLOW))
								.buildMeta()
								.withDisplayName(ChatColor.GOLD + "Center Chest Marker")
								.item().build();
						im.setDurability(dye(DyeColor.YELLOW).getData());
						p.getInventory().setItem(6, im.clone());
						im = new ItemBuilder(Material.INK_SACK)
								.withData(dye(DyeColor.MAGENTA))
								.buildMeta()
								.withDisplayName(ChatColor.LIGHT_PURPLE + "Center BrewingStand Marker")
								.item().build();
						im.setDurability(dye(DyeColor.MAGENTA).getData());
						p.getInventory().setItem(7, im.clone());
						p.updateInventory();
					}
					
					return true;
				}
			};
			commandMap.register("swmaptool", "skywarsmaptool", cmd);
			commandMapField.setAccessible(false);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
	}
	
	public Dye dye(DyeColor color) {
		Dye d = new Dye();
		d.setColor(color);
		return d;
	}
	
	
}
