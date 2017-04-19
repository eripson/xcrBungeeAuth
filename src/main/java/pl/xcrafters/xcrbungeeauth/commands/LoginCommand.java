package pl.xcrafters.xcrbungeeauth.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import pl.xcrafters.xcrbungeeauth.data.DataLogin;
import pl.xcrafters.xcrbungeeauth.AuthPlugin;
import pl.xcrafters.xcrbungeeauth.data.DataUser;

public class LoginCommand extends Command{

    AuthPlugin plugin;
    
    public LoginCommand(AuthPlugin plugin){
        super("login", null);
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerCommand(plugin, this);
    }
    
    @Override
    public void execute(CommandSender sender, String[] args){
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer player = (ProxiedPlayer) sender;
            DataUser user = plugin.dataManager.getUserByNick(player.getName());
            if(user.getPremium()){
                player.sendMessage(plugin.color("&6Ta komenda jest wylaczona dla graczy z kontem premium."));
                return;
            }
            if(user.loggedIn){
                player.sendMessage(plugin.color("&cJestes juz zalogowany!"));
                return;
            }
            if(user.getPassword() == null && !user.getPremium()){
                player.sendMessage(plugin.color("&6Najpierw zarejestruj sie komenda /register!"));
                return;
            }
            if(args.length == 0){
                player.sendMessage(plugin.color("&cPoprawne uzycie: &6/login <haslo>"));
                return;
            }
            if(plugin.cmpPassWithHash(args[0], user.getPassword())){
                player.sendMessage(plugin.color("&8\u00BB &6Zalogowano do gry!"));
                user.loggedIn = true;
                player.connect(user.targetServer);
                String ip = player.getAddress().getAddress().getHostAddress();
                user.setLastIP(ip);
                user.setLastJoined(System.currentTimeMillis());
                user.messageTask.cancel();
                user.messageTask = null;
                user.targetServer = null;
                user.update();

                DataLogin login = plugin.dataManager.createLogin(player.getName(), player.getAddress().getAddress().getHostAddress());
                login.setTime(System.currentTimeMillis());
                login.insert();
            } else {
                player.sendMessage(plugin.color("&cNiepoprawne haslo!"));
            }
        }
    }
    
}
