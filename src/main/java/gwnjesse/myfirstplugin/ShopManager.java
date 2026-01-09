package gwnjesse.myfirstplugin;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Piglin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ShopManager implements Listener {

    private final WeedCraft plugin;

    private static final String TITLE_MAIN = "§aWeed Shop | Categories";
    private static final String TITLE_TOOLS = "§aWeed Shop | Tools";
    private static final String TITLE_ARMOR = "§aWeed Shop | Armor";
    private static final String TITLE_UTILITY = "§aWeed Shop | Utility";

    private static final Component TITLE_MAIN_C = Component.text(TITLE_MAIN);
    private static final Component TITLE_TOOLS_C = Component.text(TITLE_TOOLS);
    private static final Component TITLE_ARMOR_C = Component.text(TITLE_ARMOR);
    private static final Component TITLE_UTILITY_C = Component.text(TITLE_UTILITY);

    public ShopManager(WeedCraft plugin) {
        this.plugin = plugin;
    }

    public void openShop(Player player) {
        openMain(player);
    }

    /**
     * Spawn a tagged shop clerk piglin at the player's location.
     */
    public void spawnClerk(Player player) {
        World world = player.getWorld();
        world.spawn(player.getLocation(), Piglin.class, piglin -> {
            piglin.customName(Component.text("§eShop Clerk"));
            piglin.setCustomNameVisible(true);
            piglin.setImmuneToZombification(true);
            piglin.setPersistent(true);
            piglin.setRemoveWhenFarAway(false);
            piglin.getPersistentDataContainer().set(clerkKey(), PersistentDataType.BYTE, (byte) 1);
        });
        player.sendMessage("§aSpawned a shop clerk piglin.");
    }

    @EventHandler
    public void onPiglinInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Piglin piglin)) return;

        PersistentDataContainer pdc = piglin.getPersistentDataContainer();
        if (!pdc.has(clerkKey(), PersistentDataType.BYTE)) return;

        Player player = event.getPlayer();
        event.setCancelled(true);
        openMain(player);
    }

    @EventHandler
    public void onPiglinZombify(EntityTransformEvent event) {
        if (!(event.getEntity() instanceof Piglin piglin)) return;
        PersistentDataContainer pdc = piglin.getPersistentDataContainer();
        if (!pdc.has(clerkKey(), PersistentDataType.BYTE)) return;
        // Prevent clerks from converting into zombified piglins
        event.setCancelled(true);
    }

    private void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE_MAIN_C);
        inv.setItem(11, createMenuItem(Material.IRON_SWORD, "§eTools", List.of("§7Weapons and shields")));
        inv.setItem(13, createMenuItem(Material.IRON_CHESTPLATE, "§bArmor", List.of("§7Armor sets")));
        inv.setItem(15, createMenuItem(Material.BONE_MEAL, "§fUtility", List.of("§7Farming and supplies")));
        player.openInventory(inv);
    }

    private void openTools(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_TOOLS_C);
        int slot = 10;
        slot = place(inv, slot, priced(Material.WOODEN_SWORD, 1, "§eWooden Sword", 100));
        slot = place(inv, slot, priced(Material.STONE_SWORD, 1, "§eStone Sword", 150));
        slot = place(inv, slot, priced(Material.GOLDEN_SWORD, 1, "§eGolden Sword", 180));
        slot = place(inv, slot, priced(Material.IRON_SWORD, 1, "§eIron Sword", 250));
        slot = place(inv, slot, priced(Material.DIAMOND_SWORD, 1, "§eDiamond Sword", 500));
        slot = place(inv, slot, priced(Material.NETHERITE_SWORD, 1, "§eNetherite Sword", 800));
        slot = place(inv, slot, priced(Material.SHIELD, 1, "§bShield", 150));
        slot = place(inv, slot, priced(Material.BOW, 1, "§bBow", 200));
        slot = place(inv, slot, priced(Material.CROSSBOW, 1, "§bCrossbow", 300));
        slot = place(inv, slot, priced(Material.WOODEN_HOE, 1, "§fWooden Hoe", 50));
        addBack(inv);
        player.openInventory(inv);
    }

    private void openArmor(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_ARMOR_C);
        int slot = 10;
        slot = place(inv, slot, priced(Material.LEATHER_HELMET, 1, "§fLeather Helmet", 90));
        slot = place(inv, slot, priced(Material.LEATHER_CHESTPLATE, 1, "§fLeather Chestplate", 120));
        slot = place(inv, slot, priced(Material.LEATHER_LEGGINGS, 1, "§fLeather Leggings", 100));
        slot = place(inv, slot, priced(Material.LEATHER_BOOTS, 1, "§fLeather Boots", 80));

        slot = place(inv, slot, priced(Material.CHAINMAIL_HELMET, 1, "§fChain Helmet", 120));
        slot = place(inv, slot, priced(Material.CHAINMAIL_CHESTPLATE, 1, "§fChain Chestplate", 160));
        slot = place(inv, slot, priced(Material.CHAINMAIL_LEGGINGS, 1, "§fChain Leggings", 140));
        slot = place(inv, slot, priced(Material.CHAINMAIL_BOOTS, 1, "§fChain Boots", 110));

        slot = place(inv, slot, priced(Material.IRON_HELMET, 1, "§fIron Helmet", 180));
        slot = place(inv, slot, priced(Material.IRON_CHESTPLATE, 1, "§fIron Chestplate", 240));
        slot = place(inv, slot, priced(Material.IRON_LEGGINGS, 1, "§fIron Leggings", 220));
        slot = place(inv, slot, priced(Material.IRON_BOOTS, 1, "§fIron Boots", 180));

        slot = place(inv, slot, priced(Material.DIAMOND_HELMET, 1, "§fDiamond Helmet", 380));
        slot = place(inv, slot, priced(Material.DIAMOND_CHESTPLATE, 1, "§fDiamond Chestplate", 500));
        slot = place(inv, slot, priced(Material.DIAMOND_LEGGINGS, 1, "§fDiamond Leggings", 460));
        slot = place(inv, slot, priced(Material.DIAMOND_BOOTS, 1, "§fDiamond Boots", 360));

        slot = place(inv, slot, priced(Material.NETHERITE_HELMET, 1, "§fNetherite Helmet", 650));
        slot = place(inv, slot, priced(Material.NETHERITE_CHESTPLATE, 1, "§fNetherite Chestplate", 800));
        slot = place(inv, slot, priced(Material.NETHERITE_LEGGINGS, 1, "§fNetherite Leggings", 760));
        slot = place(inv, slot, priced(Material.NETHERITE_BOOTS, 1, "§fNetherite Boots", 620));

        addBack(inv);
        player.openInventory(inv);
    }

    private void openUtility(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_UTILITY_C);
        int slot = 10;
        slot = place(inv, slot, priced(Material.BONE_MEAL, 16, "§fBone Meal x16", 50));
        slot = place(inv, slot, priced(Material.SHEARS, 1, "§fShears", 80));
        slot = place(inv, slot, priced(Material.BUCKET, 1, "§fBucket", 60));
        slot = place(inv, slot, priced(Material.WATER_BUCKET, 1, "§fWater Bucket", 90));
        slot = place(inv, slot, priced(Material.MILK_BUCKET, 1, "§fMilk Bucket", 120));
        slot = place(inv, slot, priced(Material.TORCH, 32, "§fTorch x32", 60));
        slot = place(inv, slot, priced(Material.COOKED_BEEF, 16, "§fSteak x16", 120));
        slot = place(inv, slot, priced(Material.GOLDEN_APPLE, 2, "§fGolden Apple x2", 250));
        slot = place(inv, slot, priced(Material.DIRT, 1, "§fDirt x4", 200));
        addBack(inv);
        player.openInventory(inv);
    }

    private int place(Inventory inv, int slot, ItemStack item) {
        inv.setItem(slot, item);
        return slot + 1;
    }

    private void addBack(Inventory inv) {
        inv.setItem(49, createMenuItem(Material.BARRIER, "§cBack", List.of("§7Return to categories")));
    }

    private ItemStack createMenuItem(Material m, String name, List<String> lore) {
        ItemStack item = new ItemStack(m);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(lore.stream().map(Component::text).toList());
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack priced(Material m, int amount, String name, int price) {
        ItemStack item = new ItemStack(m, amount);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Price: §c" + price));
        meta.lore(lore);
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(priceKey(), PersistentDataType.INTEGER, price);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onShopClick(InventoryClickEvent e) {
        Component rawTitle = e.getView().title();
        if (rawTitle == null) return;

        // Only handle our shop titles; if not one of them, exit
        boolean isShopView = rawTitle.equals(TITLE_MAIN_C)
                || rawTitle.equals(TITLE_TOOLS_C)
                || rawTitle.equals(TITLE_ARMOR_C)
                || rawTitle.equals(TITLE_UTILITY_C);
        if (!isShopView) return;

        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player player)) return;
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        Material type = clickedItem.getType();

        // Navigation from main menu
        if (rawTitle.equals(TITLE_MAIN_C)) {
            switch (type) {
                case IRON_SWORD -> openTools(player);
                case IRON_CHESTPLATE -> openArmor(player);
                case BONE_MEAL -> openUtility(player);
                default -> {}
            }
            return;
        }

        // Back button from category menus
        if (type == Material.BARRIER) {
            openMain(player);
            return;
        }

        // Purchases in category menus
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Integer price = pdc.get(priceKey(), PersistentDataType.INTEGER);
        if (price == null) return;

        ItemStack reward = clickedItem.clone();
        reward.setItemMeta(meta);

        attemptPurchase(player, price, reward);
    }

    private void attemptPurchase(Player player, int price, ItemStack reward) {

        double bal = plugin.getConfig().getDouble("balances." + player.getUniqueId(), 0);

        if (bal < price) {
            player.sendMessage("§cYou don't have enough money!");
            return;
        }

        plugin.getConfig().set("balances." + player.getUniqueId(), bal - price);
        plugin.saveConfig();

        // Reset police threshold to current balance bucket after spending
        PoliceManager pm = plugin.getPoliceManager();
        if (pm != null) {
            pm.syncAfterSpend(player, bal - price);
        }

        ItemMeta meta = reward.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().remove(priceKey());
            reward.setItemMeta(meta);
        }

        player.getInventory().addItem(reward);
        player.sendMessage("§aPurchase successful!");
    }

    private NamespacedKey priceKey() {
        return plugin.getNamespacedKey("shop_price");
    }

    private NamespacedKey clerkKey() {
        return plugin.getNamespacedKey("shop_clerk");
    }
}
