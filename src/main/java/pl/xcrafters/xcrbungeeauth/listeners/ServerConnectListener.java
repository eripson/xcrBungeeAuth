package pl.xcrafters.xcrbungeeauth.listeners;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import pl.xcrafters.xcrbungeeauth.data.DataUser;
import pl.xcrafters.xcrbungeeauth.AuthPlugin;

public class ServerConnectListener implements Listener{

    AuthPlugin plugin;
    
    public ServerConnectListener(AuthPlugin plugin){
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerConnect(ServerConnectEvent event){
        ServerInfo target = event.getTarget();
        DataUser user = plugin.dataManager.getUserByPlayer(event.getPlayer());
        final ProxiedPlayer player = event.getPlayer();
        if(user.loggedIn){
            return;
        }
        user.targetServer = target;
        event.setTarget(plugin.configManager.authServer);
    }
    
}
