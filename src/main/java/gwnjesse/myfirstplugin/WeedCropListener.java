package gwnjesse.myfirstplugin;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class WeedCropListener implements Listener {

    private final WeedCraft plugin;
    private final WeedItems items;

    public WeedCropListener(WeedCraft plugin, WeedItems items) {
        this.plugin = plugin;
        this.items = items;
    }

    @EventHandler
    public void onSeedPlant(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getItem() == null) return;

        // Must be custom Weed Seed
        if (!items.isWeedSeed(event.getItem())) return;

        Block farmland = event.getClickedBlock();
        if (farmland.getType() != Material.FARMLAND) return;

        Block above = farmland.getRelative(0, 1, 0);
        if (above.getType() != Material.AIR) return;

        // Plant vanilla wheat crop (we treat it as weed)
        above.setType(Material.WHEAT);

        event.getPlayer().sendMessage("§aYou planted a §2Weed Seed§a.");
        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ITEM_CROP_PLANT, 1f, 1f);
    }

    @EventHandler
    public void onWeedBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (block.getType() != Material.WHEAT) return;

        Ageable age = (Ageable) block.getBlockData();

        // Not matured yet → normal break
        if (age.getAge() < age.getMaximumAge()) return;

        // Prevent normal wheat drops
        event.setDropItems(false);

        // Drop Raw Weed instead (first stage of processing)
        block.getWorld().dropItemNaturally(
                block.getLocation(),
                items.getRawWeed()
        );

        event.getPlayer().sendMessage("§2You harvested Mature Weed!");
        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }
}
