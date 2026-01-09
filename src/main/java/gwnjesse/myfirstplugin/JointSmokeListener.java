package gwnjesse.myfirstplugin;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Handles joint smoking: right-click to smoke, displays joint in front of player,
 * spawns smoke particles, and breaks after 5 uses.
 */
public class JointSmokeListener implements Listener {

    private final WeedCraft plugin;
    private final WeedItems items;

    public JointSmokeListener(WeedCraft plugin, WeedItems items) {
        this.plugin = plugin;
        this.items = items;
    }

    @EventHandler
    public void onPlayerInteractJoint(PlayerInteractEvent event) {
        // Only handle right-click
        if (!event.getAction().isRightClick()) return;

        ItemStack item = event.getItem();
        if (item == null || !items.isJoint(item)) return;

        Player player = event.getPlayer();
        event.setCancelled(true);

        // Get use count from joint (stored in PDC)
        ItemMeta meta = item.getItemMeta();
        int uses = 0;
        if (meta != null && meta.getPersistentDataContainer().has(
                plugin.getNamespacedKey("joint_uses"),
                PersistentDataType.INTEGER)) {
            uses = meta.getPersistentDataContainer().get(
                    plugin.getNamespacedKey("joint_uses"),
                    PersistentDataType.INTEGER
            );
        }

        // Check if joint is broken (5+ uses)
        if (uses >= 5) {
            player.sendMessage("§cThis joint is too burnt!");
            item.setAmount(item.getAmount() - 1);
            return;
        }

        // Smoke the joint
        smokeJoint(player, item, uses);
    }

    /**
     * Create smoking animation and effect for the joint.
     */
    private void smokeJoint(Player player, ItemStack joint, int currentUses) {
        // Create armor stand to hold the joint in front of player (at mouth level, centered on head)
        Location standLocation = player.getEyeLocation().add(
                player.getLocation().getDirection().multiply(0.4)
        ).add(0, -0.6, 0);

        ArmorStand stand = player.getWorld().spawn(standLocation, ArmorStand.class, as -> {
            as.setVisible(false);
            as.setSmall(true);
            as.setArms(true);
            as.setBasePlate(false);
            as.setMarker(true);
            // Set right hand item to the joint
            as.getEquipment().setItemInMainHand(joint.clone());
        });

        // Face the armor stand toward the player's facing direction
        stand.setHeadPose(
                new org.bukkit.util.EulerAngle(
                        Math.toRadians(-90),
                        Math.toRadians(player.getLocation().getYaw() + 90),
                        0
                )
        );

        // Play smoke particles and sound for 2 seconds
        new BukkitRunnable() {
            int ticks = 0;
            int smokeTicks = 40;

            @Override
            public void run() {
                if (ticks >= smokeTicks) {
                    stand.remove();
                    incrementJointUses(player, joint);
                    this.cancel();
                    return;
                }

                // Spawn smoke particles
                Location particleLocation = stand.getEyeLocation();
                player.getWorld().spawnParticle(
                        Particle.SMOKE,
                        particleLocation,
                        2,
                        0.1, 0.1, 0.1,
                        0.05
                );

                // Play smoke sound occasionally
                if (ticks % 10 == 0) {
                    player.playSound(player.getLocation(), Sound.BLOCK_CAMPFIRE_CRACKLE, 0.3f, 1f);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);

        player.playSound(player.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 0.5f, 1.5f);
    }

    /**
     * Increment the use count on the joint and remove it if it reaches 5 uses.
     */
    private void incrementJointUses(Player player, ItemStack joint) {
        ItemMeta meta = joint.getItemMeta();
        if (meta == null) return;

        int currentUses = 0;
        if (meta.getPersistentDataContainer().has(
                plugin.getNamespacedKey("joint_uses"),
                PersistentDataType.INTEGER)) {
            currentUses = meta.getPersistentDataContainer().get(
                    plugin.getNamespacedKey("joint_uses"),
                    PersistentDataType.INTEGER
            );
        }

        currentUses++;

        if (currentUses >= 5) {
            // Joint is broken, remove it
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            if (mainHand != null && items.isJoint(mainHand)) {
                if (mainHand.getAmount() <= 1) {
                    player.getInventory().setItemInMainHand(null);
                } else {
                    mainHand.setAmount(mainHand.getAmount() - 1);
                }
            }
            player.sendMessage("§cYour joint burned out!");
        } else {
            // Update use count on the joint
            meta.getPersistentDataContainer().set(
                    plugin.getNamespacedKey("joint_uses"),
                    PersistentDataType.INTEGER,
                    currentUses
            );
            joint.setItemMeta(meta);
            
            // Update lore to show uses
            updateJointLore(meta, currentUses);
            joint.setItemMeta(meta);

            player.sendMessage("§7Joint uses: §e" + currentUses + "§7/5");
        }
    }

    /**
     * Update the lore to show remaining uses.
     */
    private void updateJointLore(ItemMeta meta, int uses) {
        java.util.List<net.kyori.adventure.text.Component> lore = new java.util.ArrayList<>();
        lore.add(net.kyori.adventure.text.Component.text("§7Rolled from ground weed and paper."));
        lore.add(net.kyori.adventure.text.Component.text("§aUses remaining: §e" + (5 - uses) + "§a/5"));
        meta.lore(lore);
    }
}
