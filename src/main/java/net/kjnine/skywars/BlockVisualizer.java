package net.kjnine.skywars;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.Dye;
import org.bukkit.material.Wool;
import org.bukkit.scheduler.BukkitRunnable;

public class BlockVisualizer extends BukkitRunnable {

	private SWMapTool pl;
	
	public BlockVisualizer(SWMapTool pl) {
		this.pl = pl;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		pl.toolModeData.keySet().forEach(u -> {
			Player p = pl.getServer().getPlayer(u);
			if(p != null) {
				if(p.getItemInHand() != null && p.getItemInHand().getType().equals(Material.INK_SACK)) {
					Block t = p.getTargetBlock(null, 50);
					if(pl.lastTarget.containsKey(p.getUniqueId()) && !pl.lastTarget.get(p.getUniqueId()).equals(t))
						pl.lastTarget.get(p.getUniqueId()).getState().update(true);
					pl.lastTarget.put(p.getUniqueId(), t);
					Dye d = (Dye) p.getItemInHand().getData();
					if(d.getColor().equals(DyeColor.LIME)) {
						if(!(t.getType().equals(Material.WOOL) && ((Wool)t.getState().getData()).getColor().equals(DyeColor.LIME))) {
							p.sendBlockChange(t.getLocation(), Material.STAINED_GLASS, (byte)( 0x0f ^ d.getData()));
						}
					} else if(d.getColor().equals(DyeColor.GRAY)) {
						if(!(t.getType().equals(Material.WOOL) && ((Wool)t.getState().getData()).getColor().equals(DyeColor.LIME))) {
							p.sendBlockChange(t.getLocation(), Material.STAINED_GLASS, (byte)( 0x0f ^ d.getData()));
						}
					} else if(d.getColor().equals(DyeColor.YELLOW) || d.getColor().equals(DyeColor.ORANGE)) {
						if(t.getType().equals(Material.CHEST))
							p.sendBlockChange(t.getLocation(), Material.STAINED_GLASS, (byte)( 0x0f ^ d.getData()));
					} else if(d.getColor().equals(DyeColor.PURPLE) || d.getColor().equals(DyeColor.MAGENTA)) {
						if(t.getType().equals(Material.BREWING_STAND))
							p.sendBlockChange(t.getLocation(), Material.STAINED_GLASS, (byte)( 0x0f ^ d.getData()));
					}
				} else if(pl.lastTarget.containsKey(p.getUniqueId())) {
					pl.lastTarget.get(p.getUniqueId()).getState().update(true);
					pl.lastTarget.remove(p.getUniqueId());
				}
			}
		});
	}
	
}
