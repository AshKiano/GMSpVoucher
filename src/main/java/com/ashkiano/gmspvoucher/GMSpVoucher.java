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
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

        checkForUpdates();
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

    private void checkForUpdates() {
        try {
            String pluginName = this.getDescription().getName();
            URL url = new URL("https://www.ashkiano.com/version_check.php?plugin=" + pluginName);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String jsonResponse = response.toString();
                JSONObject jsonObject = new JSONObject(jsonResponse);
                if (jsonObject.has("error")) {
                    this.getLogger().warning("Error when checking for updates: " + jsonObject.getString("error"));
                } else {
                    String latestVersion = jsonObject.getString("latest_version");

                    String currentVersion = this.getDescription().getVersion();
                    if (currentVersion.equals(latestVersion)) {
                        this.getLogger().info("This plugin is up to date!");
                    } else {
                        this.getLogger().warning("There is a newer version (" + latestVersion + ") available! Please update!");
                    }
                }
            } else {
                this.getLogger().warning("Failed to check for updates. Response code: " + responseCode);
            }
        } catch (Exception e) {
            this.getLogger().warning("Failed to check for updates. Error: " + e.getMessage());
        }
    }
}
