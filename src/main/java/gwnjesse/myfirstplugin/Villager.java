package gwnjesse.myfirstplugin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener that lets players sell Ground Weed and Joints to vanilla Villager NPCs.
 * - Right-click a Villager while holding Ground Weed -> sells for 20 coins.
 * - Right-click a Villager while holding Joint -> sells for 40 coins.
 * - Balances are stored in the plugin config under `balances.<uuid>`.
 */
public class Villager implements Listener {

    private final WeedCraft plugin;
    private final WeedItems weedItems;
    private final double PRICE_PER_GROUND_WEED = 20.0;
    private final double PRICE_PER_JOINT = 40.0;

    public Villager(WeedCraft plugin, WeedItems weedItems) {
        this.plugin = plugin;
        this.weedItems = weedItems;
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof org.bukkit.entity.Villager)) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();

        // Check for ground weed or joint
        boolean isGroundWeed = weedItems != null && weedItems.isGroundWeed(hand);
        boolean isJoint = weedItems != null && weedItems.isJoint(hand);
        
        if (hand == null || hand.getType() == Material.AIR || (!isGroundWeed && !isJoint)) {
            if (hand != null && hand.getType() != Material.AIR) {
                player.sendMessage("§cThis villager only buys Ground Weed or Joints.");
            }
            return;
        }

        // Cancel vanilla trading UI
        event.setCancelled(true);

        // Determine price based on item type
        double price = isGroundWeed ? PRICE_PER_GROUND_WEED : PRICE_PER_JOINT;
        String itemName = isGroundWeed ? "Ground Weed" : "Joint";

        // Remove 1 item
        if (hand.getAmount() <= 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            hand.setAmount(hand.getAmount() - 1);
            player.getInventory().setItemInMainHand(hand);
        }

        // Pay the player
        addMoney(player, price);
        player.sendMessage("§aYou sold " + itemName + " for §e" + (int) price + " coins§a.");
    }


    private void addMoney(Player player, double amount) {
        String path = "balances." + player.getUniqueId().toString();
        double bal = plugin.getConfig().getDouble(path, 0.0);
        bal += amount;
        plugin.getConfig().set(path, bal);
        plugin.saveConfig();

        // Notify police manager about the new balance so it can spawn police if thresholds reached
        PoliceManager pm = plugin.getPoliceManager();
        if (pm != null) {
            pm.checkPolice(player, bal);
        }
    }

    public double getBalance(java.util.UUID uuid) {
        return plugin.getConfig().getDouble("balances." + uuid.toString(), 0.0);
    }
}

