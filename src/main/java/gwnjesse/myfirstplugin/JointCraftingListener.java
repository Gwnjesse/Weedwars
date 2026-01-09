package gwnjesse.myfirstplugin;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

/**
 * Handles the crafting recipe for joints: Ground Weed + Paper = Joint
 * Validates the craft when ground weed + paper are placed in any order.
 */
public class JointCraftingListener implements Listener {

    private final WeedItems items;

    public JointCraftingListener(WeedCraft plugin, WeedItems items) {
        this.items = items;
        plugin.getLogger().info("Joint crafting listener registered!");
    }

    /**
     * Validate and complete the joint crafting recipe on the fly.
     * Ground Weed + Paper in any order = Joint
     */
    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        CraftingInventory inv = event.getInventory();
        ItemStack[] matrix = inv.getMatrix();

        // Count ground weed and paper in the crafting grid
        int groundWeedCount = 0;
        int paperCount = 0;
        int totalItems = 0;

        for (ItemStack item : matrix) {
            if (item == null || item.getType() == Material.AIR) continue;

            totalItems++;

            if (items.isGroundWeed(item)) {
                groundWeedCount += item.getAmount();
            } else if (item.getType() == Material.PAPER) {
                paperCount += item.getAmount();
            }
        }

        // Valid craft: exactly 1 ground weed and 1 paper total
        if (groundWeedCount >= 1 && paperCount >= 1 && totalItems == 2) {
            // Create the result with the amount of joints we can make
            ItemStack result = items.getJoint();
            result.setAmount(Math.min(groundWeedCount, paperCount));
            event.getInventory().setResult(result);
        } else {
            event.getInventory().setResult(null);
        }
    }
}
