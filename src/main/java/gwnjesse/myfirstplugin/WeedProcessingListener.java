package gwnjesse.myfirstplugin;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Handles weed processing: hanging on fences to dry and grinding in grindstones.
 * - Place raw weed on a fence: starts drying (random 60-120 seconds)
 * - Drying completes: dried weed drops
 * - Shift-click dried weed in grindstone: converts to ground weed (sellable)
 */
public class WeedProcessingListener implements Listener {

    private final WeedCraft plugin;
    private final WeedItems items;

    public WeedProcessingListener(WeedCraft plugin, WeedItems items) {
        this.plugin = plugin;
        this.items = items;
    }

    /**
     * Handle placing raw weed on a fence to dry it.
     */
    @EventHandler
    public void onPlaceRawWeedOnFence(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getItem() == null) return;

        Block block = event.getClickedBlock();
        if (!isFence(block)) return;

        ItemStack item = event.getItem();
        if (!items.isRawWeed(item)) return;

        event.setCancelled(true);

        // Start drying process (60-120 seconds)
        int dryTime = 60 + (int) (Math.random() * 60); // 60-120 seconds

        // Position item frame based on clicked face of fence
        org.bukkit.block.BlockFace face = event.getBlockFace();
        org.bukkit.Location frameLocation = block.getLocation().add(0.5, 0.5, 0.5);
        
        // Adjust position based on which face was clicked
        if (face == org.bukkit.block.BlockFace.NORTH) {
            frameLocation.add(0, 0, -0.4);
        } else if (face == org.bukkit.block.BlockFace.SOUTH) {
            frameLocation.add(0, 0, 0.4);
        } else if (face == org.bukkit.block.BlockFace.EAST) {
            frameLocation.add(0.4, 0, 0);
        } else if (face == org.bukkit.block.BlockFace.WEST) {
            frameLocation.add(-0.4, 0, 0);
        } else if (face == org.bukkit.block.BlockFace.UP) {
            frameLocation.add(0, 0.4, 0);
        } else if (face == org.bukkit.block.BlockFace.DOWN) {
            frameLocation.add(0, -0.4, 0);
        }

        // Create invisible item frame with wheat to show drying
        ItemFrame frame = (ItemFrame) block.getWorld().spawn(
                frameLocation,
                ItemFrame.class,
                itemFrame -> {
                    itemFrame.setItem(new ItemStack(Material.WHEAT));
                    itemFrame.setVisible(false);
                    itemFrame.setFixed(true);
                    itemFrame.getPersistentDataContainer().set(
                            plugin.getNamespacedKey("drying_frame"),
                            org.bukkit.persistence.PersistentDataType.BYTE,
                            (byte) 1
                    );
                }
        );

        // Use a scheduled task to handle drying
        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if block still exists and is a fence
                if (!isFence(block)) {
                    frame.remove();
                    return;
                }

                // Remove the item frame
                frame.remove();

                // Drop dried weed
                block.getWorld().dropItemNaturally(
                        block.getLocation(),
                        items.getDriedWeed()
                );

                event.getPlayer().sendMessage("§6Your weed has dried!");
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ITEM_CROP_PLANT, 1f, 1.5f);
            }
        }.runTaskLater(plugin, dryTime * 20L);

        // Consume one raw weed from player
        item.setAmount(item.getAmount() - 1);
        event.getPlayer().sendMessage("§aHanging weed to dry... Will be ready in " + dryTime + " seconds!");
        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_WOOD_PLACE, 1f, 1f);
    }

    /**
     * Handle shift-clicking a grindstone while holding dried weed to convert to ground weed.
     */
    @EventHandler
    public void onGrindstoneInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        Block block = event.getClickedBlock();
        if (block.getType() != Material.GRINDSTONE) return;

        // Only process if player is shift-clicking (sneaking)
        if (!event.getPlayer().isSneaking()) return;

        ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) return;

        // Check if player has dried weed
        if (!items.isDriedWeed(hand)) return;

        event.setCancelled(true);

        // Get the amount to convert
        int amount = hand.getAmount();

        // Create ground weed with same amount
        ItemStack groundWeed = items.getGroundWeed();
        groundWeed.setAmount(amount);

        // Replace hand item with ground weed
        event.getPlayer().getInventory().setItemInMainHand(groundWeed);

        // Play sound
        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1f, 1f);
        event.getPlayer().sendMessage("§aGround weed created! Ready to sell!");
    }

    /**
     * Check if a block is a fence (any type).
     */
    private boolean isFence(Block block) {
        Material type = block.getType();
        return type == Material.OAK_FENCE
                || type == Material.BIRCH_FENCE
                || type == Material.SPRUCE_FENCE
                || type == Material.DARK_OAK_FENCE
                || type == Material.ACACIA_FENCE
                || type == Material.JUNGLE_FENCE
                || type == Material.NETHER_BRICK_FENCE
                || type == Material.CRIMSON_FENCE
                || type == Material.WARPED_FENCE
                || type == Material.MANGROVE_FENCE;
    }
}
