package gwnjesse.myfirstplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class PoliceManager implements Listener {

    private final WeedCraft plugin;

    public PoliceManager(WeedCraft plugin) {
        this.plugin = plugin;
    }

    public void checkPolice(Player player, double newBalance) {

        int threshold = (int) (newBalance / 100); // 100, 200, 300...
        int lastTriggered = plugin.getConfig().getInt("policeTriggered." + player.getUniqueId(), 0);

        if (threshold <= lastTriggered) return; // already triggered this stage

        // Update so it doesn't trigger again
        plugin.getConfig().set("policeTriggered." + player.getUniqueId(), threshold);
        plugin.saveConfig();

        // Pillagers amount scales: 3, 6, 9, ...
        int policeAmount = threshold * 3;

        spawnPolice(player, policeAmount);
    }

    /**
     * When a player's balance goes down (e.g., they buy from the shop), reset the last triggered
     * threshold to match their current hundreds bucket. Example: balance drops from 520 to 40 ->
     * lastTriggered becomes 0, so the next time they pass 100 it will spawn again.
     */
    public void syncAfterSpend(Player player, double newBalance) {
        int threshold = (int) (newBalance / 100);
        plugin.getConfig().set("policeTriggered." + player.getUniqueId(), threshold);
        plugin.saveConfig();
    }

    private void spawnPolice(Player player, int amount) {

        Location loc = player.getLocation();
        player.sendMessage("§c§lTHE POLICE ARE AFTER YOU!");
        player.sendMessage("§7" + amount + " officers are raiding your grow op!");

        for (int i = 0; i < amount; i++) {
            Location spawnLoc = loc.clone().add(
                    (Math.random() * 10) - 20,
                    0,
                    (Math.random() * 10) - 20
            );

            Pillager p = (Pillager) player.getWorld().spawnEntity(spawnLoc, EntityType.PILLAGER);

            // mark as police and remember target player
            p.getPersistentDataContainer().set(policeKey(), PersistentDataType.INTEGER, 1);
            p.getPersistentDataContainer().set(ownerKey(), PersistentDataType.STRING, player.getUniqueId().toString());

            // basic police gear
            p.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));

            // focus on the player
            p.setTarget(player);
        }
    }

    @EventHandler
    public void onPoliceTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getEntity() instanceof Pillager p)) return;
        if (!p.getPersistentDataContainer().has(policeKey(), PersistentDataType.INTEGER)) return;

        // prevent police from targeting villagers or other entities; keep focus on owner player
        if (event.getTarget() instanceof org.bukkit.entity.Villager || !(event.getTarget() instanceof Player)) {
            Player owner = getOwner(p);
            if (owner != null && owner.isOnline()) {
                p.setTarget(owner);
            }
            event.setCancelled(true);
        }
    }

    private Player getOwner(Pillager p) {
        String raw = p.getPersistentDataContainer().get(ownerKey(), PersistentDataType.STRING);
        if (raw == null) return null;
        try {
            UUID id = UUID.fromString(raw);
            return Bukkit.getPlayer(id);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private NamespacedKey policeKey() {
        return plugin.getNamespacedKey("police_pillager");
    }

    private NamespacedKey ownerKey() {
        return plugin.getNamespacedKey("police_owner");
    }
}
