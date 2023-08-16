package mc.smpadditions;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.manager.IDeathManager;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.block.EnderChest;
import org.bukkit.event.inventory.InventoryMoveItemEvent;


import java.util.*;

public class Additions extends JavaPlugin implements Listener {
    private final int spawnRadius = 60;
    public static Additions additions;
    public static final List<String> groupNames = Arrays.asList(
            "Supporter", "Build_Team", "Build_Team+", "Premium", "Mojang",
            "Helper", "PIG+++", "Events", "Mcp", "Youtube", "NAP",
            "Ender", "Furious", "ASH", "Vip", "Vip+", "Mvp", "Mvp+",
            "Mvp++", "GameMaster", "Female", "Male", "Non-Binary",
            "Eboy", "Egirl", "Sexy", "Pro", "Ace", "Sweaty",
            "Unbeatable", "simp", "Europe", "Blood", "Test",
            "2P6ldu1teo45", "Niko", "Regar", "Duck"
    );


    private final long[] warningTimes = {10, 7, 5, 3, 2, 1};
    private final String[] warningMessages = {
            "Server will restart in 10 minutes!",
            "Server will restart in 7 minutes!",
            "Server will restart in 5 minutes!",
            "Server will restart in 3 minutes!",
            "Server will restart in 2 minutes!",
            "Server will restart in 1 minute!",
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(!event.getPlayer().hasPlayedBefore()){
            getServer().dispatchCommand(getServer().getConsoleSender(), "ep user " + event.getPlayer().getName() + " setgroup " + randomGroup());
        }
    }

    public static String randomGroup() {
        if (groupNames.isEmpty()) {
            return null;
        }

        Random random = new Random();
        int randomIndex = random.nextInt(groupNames.size());

        return groupNames.get(randomIndex);
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
            event.getWhoClicked().sendMessage(ChatColor.RED + "You can only use crystals to respawn dragon and not for PVP!");
            Player player = (Player) event.getView().getPlayer();
            player.sendTitle(ChatColor.RED + "WARNING!", "You can only use crystals to respawn dragon and not for PVP!");
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

    @EventHandler
    public void spawnBlockBreak(BlockBreakEvent event) {
        if (isWithinSpawnRadius(event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot break blocks within the spawn area. Go around 50 blocks away to be able to break");
        }
    }

    @EventHandler
    public void spawnBlockPlace(BlockPlaceEvent event) {
        if (isWithinSpawnRadius(event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot place blocks within the spawn area. Go around 50 blocks away to be able to place");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) {
            return;
        }

        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory instanceof ShulkerBox || topInventory instanceof EnderChest) {
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.DRAGON_EGG) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        Inventory destination = event.getDestination();
        if (destination instanceof ShulkerBox || destination instanceof EnderChest) {
            if (event.getItem() != null && event.getItem().getType() == Material.DRAGON_EGG) {
                event.setCancelled(true);
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

        TextComponent message = new TextComponent(ChatColor.GOLD + "Make sure you have joined the Discord server and voted for outreach to more players!\n");

        TextComponent discordText = new TextComponent(ChatColor.BLUE + "Discord: ");
        TextComponent discordLinkText = new TextComponent("Click here");
        discordLinkText.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, discordLink));
        discordText.addExtra(discordLinkText);
        message.addExtra(discordText);
        message.addExtra("\n");

        TextComponent planetMinecraftText = new TextComponent(ChatColor.BLUE + "Planet Minecraft: ");
        TextComponent planetMinecraftLinkText = new TextComponent("Click here");
        planetMinecraftLinkText.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, planetMinecraftLink));
        planetMinecraftText.addExtra(planetMinecraftLinkText);
        message.addExtra(planetMinecraftText);
        message.addExtra("\n");

        TextComponent minecraftServersText = new TextComponent(ChatColor.BLUE + "Minecraft Servers: ");
        TextComponent minecraftServersLinkText = new TextComponent("Click here");
        minecraftServersLinkText.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, minecraftServersLink));
        minecraftServersText.addExtra(minecraftServersLinkText);
        message.addExtra(minecraftServersText);

        Bukkit.spigot().broadcast(message);
    }

    @EventHandler
    public void playerInvunerable(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Make the player invulnerable for 2 seconds
        player.setInvulnerable(true);
        getServer().getScheduler().runTaskLater(this, () -> player.setInvulnerable(false), 60);
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
            List<Entity> trackedEnemies = deathManager.getTrackedEnemies(player);
            for(Entity p : trackedEnemies){
                if(p instanceof Player){
                    Player pl = (Player) p;
                    double addAmount = pl.getMaxHealth();
                    if(addAmount <= 18){
                        pl.setMaxHealth(addAmount + 2);
                    } else {
                        getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lsgive heart_item 1 " + pl.getName());
                        pl.sendMessage(ChatColor.DARK_GREEN + "You were given a heart item because you have max health");
                    }
                }
            }

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