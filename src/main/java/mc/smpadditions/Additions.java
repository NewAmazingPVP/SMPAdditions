package mc.smpadditions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Additions extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
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
}
