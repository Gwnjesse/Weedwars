package gwnjesse.myfirstplugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PropertyManager handles door purchases.
 * Admins can set prices on doors with /setpropertyprice <price>.
 * Players see a confirmation prompt on first interaction and can click to buy.
 * Purchases are tracked per-player per-location.
 */
public class PropertyManager implements Listener {

    private final WeedCraft plugin;
    private final Map<UUID, String> pendingPurchases = new HashMap<>(); // player UUID -> location key

    public PropertyManager(WeedCraft plugin) {
        this.plugin = plugin;
    }

    /**
     * Admin command: /setpropertyprice <price>
     * Sets a price on the door the admin is looking at.
     */
    public void setPropertyPrice(Player admin, int price) {
        Block target = admin.getTargetBlockExact(5);
        if (target == null || !isDoor(target)) {
            admin.sendMessage("§cLook at a door within 5 blocks.");
            return;
        }

        String locKey = locToKey(target.getLocation());
        plugin.getConfig().set("properties." + locKey + ".price", price);
        plugin.saveConfig();

        admin.sendMessage("§aProperty at " + locKey + " set to §e" + price + " coins§a.");
    }

    /**
     * Admin command: /getpropertyprice
     * Shows the price of the door the admin is looking at.
     */
    public void getPropertyPrice(Player admin) {
        Block target = admin.getTargetBlockExact(5);
        if (target == null || !isDoor(target)) {
            admin.sendMessage("§cLook at a door within 5 blocks.");
            return;
        }

        String locKey = locToKey(target.getLocation());
        int price = plugin.getConfig().getInt("properties." + locKey + ".price", -1);

        if (price < 0) {
            admin.sendMessage("§cThis property has no price set.");
        } else {
            admin.sendMessage("§aProperty at " + locKey + " costs §e" + price + " coins§a.");
        }
    }

    @EventHandler
    public void onPlayerInteractDoor(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null || !isDoor(block)) return;

        Player player = event.getPlayer();
        String locKey = locToKey(block.getLocation());
        String purchaseKey = "properties." + locKey + ".owned." + player.getUniqueId();

        // Check if player already owns this property
        if (plugin.getConfig().getBoolean(purchaseKey, false)) {
            return; // They own it, allow normal interaction
        }

        // Check if property has a price
        int price = plugin.getConfig().getInt("properties." + locKey + ".price", -1);
        if (price < 0) {
            return; // No price set, allow normal interaction
        }

        // Player doesn't own it and there's a price; ask for confirmation
        event.setCancelled(true);

        double bal = plugin.getConfig().getDouble("balances." + player.getUniqueId(), 0);

        if (bal < price) {
            player.sendMessage("§cYou need §e" + price + " coins§c to buy this property. You have §e" + ((int) bal) + "§c.");
            return;
        }

        // Send confirmation prompt
        player.sendMessage(" ");
        player.sendMessage("§6§l=== Property Purchase ===");
        player.sendMessage("§aProperty Cost: §e" + price + " coins");
        player.sendMessage("§aYour Balance: §e" + ((int) bal) + " coins");
        player.sendMessage(" ");
        
        // Clickable confirm button using ClickEvent
        Component confirmMsg = Component.text("§a[CLICK TO CONFIRM PURCHASE]")
                .clickEvent(ClickEvent.runCommand("/confirmproperty " + locKey));
        player.sendMessage(confirmMsg);
        player.sendMessage("§c[Type /cancelproperty to cancel]");
        player.sendMessage("§6§l=========================");
        player.sendMessage(" ");

        // Store pending purchase for confirmation
        pendingPurchases.put(player.getUniqueId(), locKey);
    }

    /**
     * Confirm a pending property purchase (called when player clicks the confirm button).
     */
    public void confirmPurchase(Player player, String locKey) {
        if (!pendingPurchases.containsKey(player.getUniqueId())) {
            player.sendMessage("§cNo pending purchase.");
            return;
        }

        if (!pendingPurchases.get(player.getUniqueId()).equals(locKey)) {
            player.sendMessage("§cInvalid purchase location.");
            return;
        }

        String purchaseKey = "properties." + locKey + ".owned." + player.getUniqueId();
        double bal = plugin.getConfig().getDouble("balances." + player.getUniqueId(), 0);
        int price = plugin.getConfig().getInt("properties." + locKey + ".price", -1);

        // Charge player
        plugin.getConfig().set("balances." + player.getUniqueId(), bal - price);
        plugin.getConfig().set(purchaseKey, true);
        plugin.saveConfig();

        // Sync police thresholds after spending
        PoliceManager pm = plugin.getPoliceManager();
        if (pm != null) {
            pm.syncAfterSpend(player, bal - price);
        }

        player.sendMessage("§a§l✓ Property purchased for §e" + price + " coins§a§l!");
        pendingPurchases.remove(player.getUniqueId());
    }

    /**
     * Cancel a pending property purchase.
     */
    public void cancelPurchase(Player player) {
        if (pendingPurchases.containsKey(player.getUniqueId())) {
            pendingPurchases.remove(player.getUniqueId());
            player.sendMessage("§cProperty purchase cancelled.");
        }
    }

    private boolean isDoor(Block block) {
        Material mat = block.getType();
        return mat.toString().contains("DOOR") || mat.toString().contains("TRAPDOOR");
    }

    private String locToKey(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }
}
