package pl.xcrafters.xcrbungeeauth.listeners;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import pl.xcrafters.xcrbungeeauth.AuthPlugin;
import pl.xcrafters.xcrbungeeauth.data.DataUser;

public class PlayerDisconnectListener implements Listener{

    AuthPlugin plugin;
    
    public PlayerDisconnectListener(AuthPlugin plugin){
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }
    
    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event){
        ProxiedPlayer player = event.getPlayer();
        DataUser user = plugin.dataManager.getUserByPlayer(player);
        if(user != null){
            user.loggedIn = false;
            user.targetServer = null;
            if(user.messageTask != null){
                user.messageTask.cancel();
                user.messageTask = null;
            }
        }
    }
    
}
