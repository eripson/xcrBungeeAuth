package pl.xcrafters.xcrbungeeauth.listeners;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import pl.xcrafters.xcrbungeeauth.AuthPlugin;

public class PlayerChatListener implements Listener {

    AuthPlugin plugin;

    public PlayerChatListener(AuthPlugin plugin) {
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }

//    @EventHandler(priority = EventPriority.HIGHEST)
//    public void onPlayerChat(PlayerChatEvent event) {
//        ProxiedPlayer player = event.getSender();
//        DataUser user = plugin.dataManager.getUserByPlayer(player);
//        if (!user.getPremium()) {
//            event.setPrefix(event.getPrefix() + plugin.color("&8*&r"));
//        }
//    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(ChatEvent event) {
        if (event.getSender() instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) event.getSender();
            if(player.getServer().getInfo().equals(plugin.configManager.authServer)){
                if(event.getMessage().split(" ")[0].startsWith("/login") || event.getMessage().split(" ")[0].startsWith("/register")){
                    return;
                }
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Nie mozesz wysylac wiadomosci na chacie przed zalogowaniem sie!");
            }
        }
    }

}
