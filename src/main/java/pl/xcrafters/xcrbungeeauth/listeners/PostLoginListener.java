package pl.xcrafters.xcrbungeeauth.listeners;

import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import pl.xcrafters.xcrbungeeauth.AuthPlugin;
import pl.xcrafters.xcrbungeeauth.data.DataLogin;
import pl.xcrafters.xcrbungeeauth.data.DataUser;

public class PostLoginListener implements Listener{

    AuthPlugin plugin;
    
    public PostLoginListener(AuthPlugin plugin){
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPostLogin(PostLoginEvent event){
        final ProxiedPlayer player = event.getPlayer();
        DataUser user = plugin.dataManager.getUserByPlayer(player);
        if(user.getUUID() == null) {
            user.setUUID(player.getUniqueId());
            user.update();
        }
        String ip = player.getAddress().getAddress().getHostAddress();
        if(user.getPremium()){
            user.loggedIn = true;
            user.setFirstJoined(System.currentTimeMillis());
            user.setLastIP(ip);
            user.update();

            DataLogin login = plugin.dataManager.createLogin(player.getName(), ip);
            login.setTime(System.currentTimeMillis());
            login.insert();
            return;
        }
        if(plugin.configManager.sessionsEnabled &&
                !user.getPremium() &&
                user.getLastIP() != null &&
                user.getLastIP().equals(ip) &&
                System.currentTimeMillis() - user.getLastJoined() <= plugin.configManager.sessionsTime &&
                user.getPassword() != null){
            user.loggedIn = true;
            user.setFirstJoined(System.currentTimeMillis());
            user.setLastIP(ip);
            user.update();

            DataLogin login = plugin.dataManager.createLogin(player.getName(), ip);
            login.setTime(System.currentTimeMillis());
            login.insert();

            player.sendMessage(plugin.color("&eTwoja sesja zostala przywrocona."));
            return;
        }
        final String msg = plugin.color("&e" + (user.getPassword() != null ? "Zaloguj sie komenda /login <haslo>" : "Zarejestruj sie komenda /register <haslo> <haslo> &c<kod z mapy>"));
        player.sendMessage(msg);
        ScheduledTask task = ProxyServer.getInstance().getScheduler().schedule(plugin, new Runnable() {
            public void run() {
                player.sendMessage(msg);
            }
        }, 5, 5, TimeUnit.SECONDS);
        user.messageTask = task;
        user.loggedIn = false;
    }
    
}
