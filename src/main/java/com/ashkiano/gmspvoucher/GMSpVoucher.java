package com.ashkiano.gmspvoucher;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

//TODO přidat permisi
//TODO přidat konfigurovatelné lore a name
//TODO přidat odpočet
//TODO když je někde kde by umřel při změně modu zpět na survival, tak h portnout na spawn nebo na safe místo
//TODO opravit chybu že když bude odpojen, tak se mu nevrátí zpět původní gamemode
public class GMSpVoucher extends JavaPlugin implements Listener {
    private final String voucherLore = ChatColor.GREEN + "Use to get 5 minutes in Spectator mode!";

    @Override
    public void onEnable() {
        this.saveDefaultConfig(); // Saves the default config if it doesn't exist
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("givevoucher").setExecutor(new GiveVoucherCommand());

        Metrics metrics = new Metrics(this, 19348);

        this.getLogger().info("Thank you for using the GMSpVoucher plugin! If you enjoy using this plugin, please consider making a donation to support the development. You can donate at: https://paypal.me/josefvyskocil");

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Verify if the item in hand has lore
        if (event.getAction().toString().contains("RIGHT") &&
                itemInHand != null &&
                itemInHand.getType() == Material.PAPER &&
                itemInHand.hasItemMeta() &&
                itemInHand.getItemMeta().hasLore() &&
                itemInHand.getItemMeta().getLore().contains(voucherLore)) {

            final GameMode originalGameMode = player.getGameMode();
            player.setGameMode(GameMode.SPECTATOR);
            Bukkit.getScheduler().runTaskLater(this, () -> player.setGameMode(originalGameMode), 5 * 60 * 20L);
            itemInHand.setAmount(itemInHand.getAmount() - 1);
        }
    }

    private class GiveVoucherCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                String permission = getConfig().getString("givevoucher-permission");
                if (player.hasPermission(permission)) {
                    ItemStack voucher = new ItemStack(Material.PAPER);
                    ItemMeta meta = voucher.getItemMeta();
                    meta.setDisplayName(ChatColor.GOLD + "GMSp Voucher");
                    meta.setLore(Arrays.asList(voucherLore));
                    voucher.setItemMeta(meta);
                    player.getInventory().addItem(voucher);
                    player.sendMessage(ChatColor.GREEN + "You have received a GMSp Voucher!");
                } else {
                    player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                }
            } else {
                sender.sendMessage("Only players can use this command.");
            }
            return true;
        }
    }
}
