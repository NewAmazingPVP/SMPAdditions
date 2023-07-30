package mc.smpadditions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.manager.IDeathManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Calendar;
import java.util.TimeZone;

public class Additions extends JavaPlugin implements Listener {
    private final int spawnRadius = 60;
    public static Additions additions;

    private final long[] warningTimes = {10, 7, 5, 3, 2, 1, 30};
    private final String[] warningMessages = {
            "Server will restart in 10 minutes!",
            "Server will restart in 7 minutes!",
            "Server will restart in 5 minutes!",
            "Server will restart in 3 minutes!",
            "Server will restart in 2 minutes!",
            "Server will restart in 1 minute!",
            "Server will restart in 30 seconds!"
    };

    @Override
    public void onEnable() {
        additions = this;
        getServer().getPluginManager().registerEvents(this, this);
        registerCustomRecipes();

        int repeatDelayTicks = 7200 * 20;
        BukkitRunnable broadcastTask = new BukkitRunnable() {
            @Override
            public void run() {
                broadcastServerMessage();
            }
        };
        broadcastTask.runTaskTimer(this, 0, repeatDelayTicks);
        scheduleRestart();
    }

    private void registerCustomRecipes() {
        ItemStack shulker = new ItemStack(Material.SHULKER_BOX);
        ShapedRecipe shulkerRecipe = new ShapedRecipe(new NamespacedKey(this, "shulker_recipe"), shulker);
        shulkerRecipe.shape("DDD", "DCD", "DDD");
        shulkerRecipe.setIngredient('C', Material.CHEST);
        shulkerRecipe.setIngredient('D', Material.DIAMOND);
        Bukkit.addRecipe(shulkerRecipe);

        ItemStack endStone = new ItemStack(Material.END_STONE);
        ShapedRecipe endStoneRecipe = new ShapedRecipe(new NamespacedKey(this, "end_stone_recipe"), endStone);
        endStoneRecipe.shape("DNE");
        endStoneRecipe.setIngredient('D', Material.DIRT);
        endStoneRecipe.setIngredient('N', Material.NETHERRACK);
        endStoneRecipe.setIngredient('E', Material.COBBLESTONE);
        Bukkit.addRecipe(endStoneRecipe);

        ItemStack purpleBlock = new ItemStack(Material.PURPUR_BLOCK);
        ShapedRecipe purpleBlockRecipe = new ShapedRecipe(new NamespacedKey(this, "purple_block_recipe"), purpleBlock);
        purpleBlockRecipe.shape("NS");
        purpleBlockRecipe.setIngredient('N', Material.NETHER_BRICKS);
        purpleBlockRecipe.setIngredient('S', Material.STONE_BRICKS);
        Bukkit.addRecipe(purpleBlockRecipe);
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.END_CRYSTAL) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(ChatColor.RED + "Crafting of End Crystals is not allowed!");
            event.getView().getPlayer().closeInventory();
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player damaged = (Player) event.getEntity();

            if (event.getDamager() instanceof Player) {
                Player damager = (Player) event.getDamager();

                if (isWithinSpawnRadius(damaged.getLocation())) {
                    event.setCancelled(true);
                    damager.sendMessage(ChatColor.RED + "You cannot damage players within the spawn protection area!");
                }
            }
        }
    }

    private boolean isWithinSpawnRadius(Location location) {
        Location spawnLocation = location.getWorld().getSpawnLocation();
        return location.distanceSquared(spawnLocation) <= spawnRadius * spawnRadius;
    }

    private void broadcastServerMessage() {
        String discordLink = "https://discord.gg/PN8egFY3ap";
        String planetMinecraftLink = "https://www.planetminecraft.com/server/nappixel-lifesteal-smp-server-new-season-starts-today/vote/";
        String minecraftServersLink = "https://minecraftservers.org/vote/653407";

        String message = ChatColor.GREEN + "Make sure you have joined the Discord server and voted for outreach to more players!\n";
        message += ChatColor.BLUE + "Discord: " + ChatColor.YELLOW + discordLink + "\n";
        message += ChatColor.BLUE + "Planet Minecraft: " + ChatColor.YELLOW + planetMinecraftLink + "\n";
        message += ChatColor.BLUE + "Minecraft Servers: " + ChatColor.YELLOW + minecraftServersLink;

        Bukkit.broadcastMessage(message);
    }

    public ICombatLogX getAPI() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        Plugin plugin = pluginManager.getPlugin("CombatLogX");
        return (ICombatLogX) plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        ICombatLogX plugin = getAPI();
        IDeathManager deathManager = plugin.getDeathManager();

        if(deathManager.wasPunishKilled(player)) {
            double amount = player.getMaxHealth();
            player.setMaxHealth(amount - 2);
            double addAmount = player.getKiller().getMaxHealth();
            player.getKiller().setMaxHealth(addAmount + 2);
        }
    }

    private void scheduleRestart() {
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
        Calendar restartTime = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
        restartTime.set(Calendar.HOUR_OF_DAY, 3);
        restartTime.set(Calendar.MINUTE, 0);
        restartTime.set(Calendar.SECOND, 0);

        if (now.after(restartTime)) {
            restartTime.add(Calendar.DAY_OF_MONTH, 1);
        }

        long initialDelay = restartTime.getTimeInMillis() - now.getTimeInMillis();

        for (int i = 0; i < warningTimes.length; i++) {
            long warningDelay = initialDelay - (warningTimes[i] * 60 * 1000);
            scheduleWarning(warningDelay, warningMessages[i]);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                restartServer();
            }
        }.runTaskLater(this, initialDelay / 50L);
    }

    private void scheduleWarning(long delay, String message) {
        new BukkitRunnable() {
            @Override
            public void run() {
                broadcastWarning(message);
            }
        }.runTaskLater(this, delay / 50L);
    }

    private void broadcastWarning(String message) {
        getServer().broadcastMessage(ChatColor.RED + message);
    }

    private void restartServer() {
        getServer().broadcastMessage(ChatColor.AQUA + "Restarting the server...");
        getServer().spigot().restart();
    }

}