package gwnjesse.myfirstplugin;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;

public class WeedCraft extends JavaPlugin {

    private PoliceManager policeManager;
    private ShopManager shopManager;
    private PropertyManager propertyManager;

    @Override
    public void onEnable() {
        try {
            WeedItems weedItems = new WeedItems(this);

            // create managers before listeners so they're available
            this.policeManager = new PoliceManager(this);
            this.shopManager = new ShopManager(this);
            this.propertyManager = new PropertyManager(this);

            // Register the listener
            getServer().getPluginManager().registerEvents(new WeedCropListener(this, weedItems), this);
            // Register weed processing listener (fence drying and grindstone grinding)
            getServer().getPluginManager().registerEvents(new WeedProcessingListener(this, weedItems), this);
            // Register villager buyer listener
            getServer().getPluginManager().registerEvents(new Villager(this, weedItems), this);
            // Register joint crafting listener (ground weed + paper = joint)
            getServer().getPluginManager().registerEvents(new JointCraftingListener(this, weedItems), this);
            // Register joint smoking listener (right-click to smoke)
            getServer().getPluginManager().registerEvents(new JointSmokeListener(this, weedItems), this);
            // Register police manager listener (targets handling)
            getServer().getPluginManager().registerEvents(this.policeManager, this);
            // Register shop listener (GUI clicks)
            getServer().getPluginManager().registerEvents(this.shopManager, this);
            // Register property manager listener (door purchases)
            getServer().getPluginManager().registerEvents(this.propertyManager, this);

            // ensure config exists for storing balances
            saveDefaultConfig();

            // Add the weedseed command
            this.getCommand("weedseed").setExecutor((sender, command, label, args) -> {
                if (!(sender instanceof org.bukkit.entity.Player player)) return true;

                // Get the ItemStack from WeedItems
                org.bukkit.inventory.ItemStack seed = weedItems.getWeedSeed();

                // Debug: check if item is null
                if (seed == null) {
                    player.sendMessage("§cError: Weed Seed is null!");
                    return true;
                } else {
                    player.sendMessage("§aDebug: Weed Seed is valid and ready to give!");
                }

                // Give the item to the player
                player.getInventory().addItem(seed);
                player.sendMessage("§aYou got a Weed Seed!");
                return true;
            });

            // Add the balance command
            this.getCommand("balance").setExecutor((sender, command, label, args) -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("This command can only be used by players.");
                    return true;
                }

                double bal = getConfig().getDouble("balances." + player.getUniqueId().toString(), 0.0);
                player.sendMessage("§aYour balance: §e" + ((int) bal) + " coins");
                return true;
            });

            // Add the weedshop command
            this.getCommand("weedshop").setExecutor((sender, cmd, label, args) -> {
                if (!(sender instanceof Player p)) return true;
                shopManager.openShop(p);
                return true;
            });

            // Add the spawnshopclerk command
            this.getCommand("spawnshopclerk").setExecutor((sender, cmd, label, args) -> {
                if (!(sender instanceof Player p)) return true;
                shopManager.spawnClerk(p);
                return true;
            });

            // Add the setpropertyprice admin command
            this.getCommand("setpropertyprice").setExecutor((sender, cmd, label, args) -> {
                if (!(sender instanceof Player p)) return true;
                if (args.length < 1) {
                    p.sendMessage("§cUsage: /setpropertyprice <price>");
                    return true;
                }
                try {
                    int price = Integer.parseInt(args[0]);
                    propertyManager.setPropertyPrice(p, price);
                } catch (NumberFormatException ex) {
                    p.sendMessage("§cPrice must be a number.");
                }
                return true;
            });

            // Add the getpropertyprice admin command
            this.getCommand("getpropertyprice").setExecutor((sender, cmd, label, args) -> {
                if (!(sender instanceof Player p)) return true;
                propertyManager.getPropertyPrice(p);
                return true;
            });

            // Add the confirmproperty command
            this.getCommand("confirmproperty").setExecutor((sender, cmd, label, args) -> {
                if (!(sender instanceof Player p)) return true;
                if (args.length < 1) {
                    p.sendMessage("§cUsage: /confirmproperty <location>");
                    return true;
                }
                propertyManager.confirmPurchase(p, args[0]);
                return true;
            });

            // Add the cancelproperty command
            this.getCommand("cancelproperty").setExecutor((sender, cmd, label, args) -> {
                if (!(sender instanceof Player p)) return true;
                propertyManager.cancelPurchase(p);
                return true;
            });

            getLogger().info("WeedCraft enabled – custom items loaded!");
        } catch (Throwable t) {
            // Log full stack trace so we can see why the plugin was disabled
            getLogger().log(Level.SEVERE, "Exception in onEnable(): ", t);
            // rethrow as RuntimeException so server will disable the plugin (but we have a full log)
            throw t instanceof RuntimeException ? (RuntimeException) t : new RuntimeException(t);
        }

    }

    public NamespacedKey getNamespacedKey(String key) {
        return new NamespacedKey(this, key);
    }

    public PoliceManager getPoliceManager() {
        return policeManager;
    }

    public PropertyManager getPropertyManager() {
        return propertyManager;
    }

}
