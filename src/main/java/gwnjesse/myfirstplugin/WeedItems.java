package gwnjesse.myfirstplugin;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class WeedItems {

    private final WeedCraft plugin; // FIXED: use your main class

    public WeedItems(WeedCraft plugin) {
        this.plugin = plugin;
    }

    public ItemStack getWeedSeed() {
        ItemStack seed = new ItemStack(Material.WHEAT_SEEDS);
        ItemMeta meta = seed.getItemMeta();

        meta.displayName(Component.text("§aWeed Seed"));
        meta.lore(java.util.List.of(
                Component.text("§7Plant this on farmland."),
                Component.text("§2Grows into Weed.")
        ));
        meta.setCustomModelData(Integer.valueOf(12345));

        meta.getPersistentDataContainer().set(
                plugin.getNamespacedKey("weed_seed"),
                PersistentDataType.INTEGER,
                1
        );

        seed.setItemMeta(meta);
        return seed;
    }

    public ItemStack getRawWeed() {
        ItemStack raw = new ItemStack(Material.WHEAT);
        ItemMeta meta = raw.getItemMeta();

        meta.displayName(Component.text("§2Raw Weed"));
        meta.lore(java.util.List.of(
                Component.text("§7Harvested from mature plants."),
                Component.text("§aHang on a fence to dry for 60-120 seconds.")
        ));

        meta.getPersistentDataContainer().set(
                plugin.getNamespacedKey("weed_raw"),
                PersistentDataType.INTEGER,
                1
        );

        raw.setItemMeta(meta);
        return raw;
    }

    public ItemStack getDriedWeed() {
        ItemStack dried = new ItemStack(Material.HAY_BLOCK);
        ItemMeta meta = dried.getItemMeta();

        meta.displayName(Component.text("§6Dried Weed"));
        meta.lore(java.util.List.of(
                Component.text("§7Dried and ready for processing."),
                Component.text("§aUse a grindstone to make ground weed.")
        ));

        meta.getPersistentDataContainer().set(
                plugin.getNamespacedKey("weed_dried"),
                PersistentDataType.INTEGER,
                1
        );

        dried.setItemMeta(meta);
        return dried;
    }

    public ItemStack getGroundWeed() {
        ItemStack ground = new ItemStack(Material.COCOA_BEANS);
        ItemMeta meta = ground.getItemMeta();

        meta.displayName(Component.text("§8Ground Weed"));
        meta.lore(java.util.List.of(
                Component.text("§7Processed and ready to sell."),
                Component.text("§aSell this to a Dealer.")
        ));

        meta.getPersistentDataContainer().set(
                plugin.getNamespacedKey("weed_ground"),
                PersistentDataType.INTEGER,
                1
        );

        ground.setItemMeta(meta);
        return ground;
    }

    public ItemStack getJoint() {
        ItemStack joint = new ItemStack(Material.STICK);
        ItemMeta meta = joint.getItemMeta();

        meta.displayName(Component.text("§7Joint"));
        meta.lore(java.util.List.of(
                Component.text("§7Rolled from ground weed and paper."),
                Component.text("§aReady to use or trade.")
        ));

        meta.getPersistentDataContainer().set(
                plugin.getNamespacedKey("joint"),
                PersistentDataType.INTEGER,
                1
        );

        joint.setItemMeta(meta);
        return joint;
    }

    public boolean isWeedSeed(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(
                plugin.getNamespacedKey("weed_seed"), PersistentDataType.INTEGER
        );
    }

    public boolean isRawWeed(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(
                plugin.getNamespacedKey("weed_raw"), PersistentDataType.INTEGER
        );
    }

    public boolean isDriedWeed(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(
                plugin.getNamespacedKey("weed_dried"), PersistentDataType.INTEGER
        );
    }

    public boolean isGroundWeed(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(
                plugin.getNamespacedKey("weed_ground"), PersistentDataType.INTEGER
        );
    }

    public boolean isJoint(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(
                plugin.getNamespacedKey("joint"), PersistentDataType.INTEGER
        );
    }
}
