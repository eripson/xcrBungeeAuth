package pl.xcrafters.xcrbungeeauth.listeners;

import java.util.logging.Level;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import pl.xcrafters.xcrbungeeauth.AuthPlugin;
import pl.xcrafters.xcrbungeeauth.data.DataUser;

public class PreLoginListener implements Listener {

    AuthPlugin plugin;
    
    public PreLoginListener(AuthPlugin plugin){
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPreLogin(final PreLoginEvent event){
        event.registerIntent(plugin);
        ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
            public void run() {
                PendingConnection conn = event.getConnection();
                if(event.isCancelled()){
                    event.completeIntent(plugin);
                    return;
                }
                String nick = event.getConnection().getName();
                if(!nick.matches("[a-zA-Z0-9_]{3,16}")){
                    event.setCancelled(true);
                    event.setCancelReason(plugin.color("&cTwoj nick zawiera niedozwolone znaki!"));
                    event.completeIntent(plugin);
                    return;
                }
                String ip = conn.getAddress().getAddress().getHostAddress();
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(nick);
                if(player != null){
                    event.setCancelled(true);
                    event.setCancelReason(plugin.color("&cGracz o podanym nicku jest juz online!"));
                    event.completeIntent(plugin);
                    return;
                }
                if(plugin.configManager.onlineMode == AuthPlugin.OnlineMode.ONLINE){
                    conn.setOnlineMode(true);
                    event.completeIntent(plugin);
                    return;
                }
                DataUser user = plugin.dataManager.getUserByNick(nick);
                if(user == null){
                    AuthPlugin.OnlineType type = plugin.hasPaid(nick);
                    boolean hasPaid = type == AuthPlugin.OnlineType.ONLINE;
                    if(type == AuthPlugin.OnlineType.OFFLINE && plugin.configManager.onlineMode == AuthPlugin.OnlineMode.RESTRICTED) {
                        event.setCancelReason(plugin.color("&7Aby wejsc na serwer z kontem non-premium\n§7aktywuj konto na naszej stronie: §6http://mchardcore.pl"));
                        event.setCancelled(true);
                        event.completeIntent(plugin);
                        return;
                    }
                    user = plugin.dataManager.createUser(nick, type, ip, System.currentTimeMillis());
                    user.setRegisteredByAdmin(false);
                    user.insert();
                    if(!hasPaid && plugin.configManager.authServer.getPlayers().size() > 50){
                        event.setCancelled(true);
                        event.setCancelReason(plugin.color("&cSerwer do logowania jest pelny!"));
                        event.completeIntent(plugin);
                        return;
                    }
                    conn.setOnlineMode(hasPaid);
                } else {
                    if(!user.getChecked()){
                        AuthPlugin.OnlineType type = plugin.hasPaid(nick);
                        boolean hasPaid = type == AuthPlugin.OnlineType.ONLINE;
                        user.setPremium(hasPaid);
                        if(type != AuthPlugin.OnlineType.ERROR) {
                            user.setChecked(true);
                        }
                        user.update();
                    }
                    if(user.getUUID() != null) {
                        conn.setUniqueId(user.getUUID());
                    }
                    conn.setOnlineMode(user.getPremium());
                }
                if(!user.getPremium() && user.getPassword() == null){
                    user.captcha = plugin.generateRandomString();
                }
                plugin.getLogger().log(Level.INFO, "Handled " + nick + " [" + (user.getPremium() ? "PREMIUM" : "NON-PREMIUM") + "]");
                event.completeIntent(plugin);
            }
        });
    }

}
