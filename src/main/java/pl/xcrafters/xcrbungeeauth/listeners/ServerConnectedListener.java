package pl.xcrafters.xcrbungeeauth.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import pl.xcrafters.xcrbungeeauth.AuthPlugin;
import pl.xcrafters.xcrbungeeauth.data.DataUser;

public class ServerConnectedListener implements Listener {

    AuthPlugin plugin;

    public ServerConnectedListener(AuthPlugin plugin) {
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
        ProxiedPlayer player = event.getPlayer();
        DataUser user = plugin.dataManager.getUserByPlayer(player);
        if(!user.loggedIn && user.getPassword() == null && event.getServer().getInfo().equals(plugin.configManager.authServer)) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Captcha");
            out.writeUTF(player.getName());
            out.writeUTF(user.captcha);
            event.getServer().sendData("BungeeCord", out.toByteArray());
        }
    }

}
