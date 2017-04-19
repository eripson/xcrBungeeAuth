package pl.xcrafters.xcrbungeeauth.commands;

import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import pl.xcrafters.xcrbungeeauth.AuthPlugin;
import pl.xcrafters.xcrbungeeauth.data.DataUser;

public class ChangePasswordCommand extends Command {

    AuthPlugin plugin;
    
    public ChangePasswordCommand(AuthPlugin plugin){
        super("changepass", null, "zmienhaslo", "changepw", "changepassword");
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerCommand(plugin, this);
    }
    
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer player = (ProxiedPlayer) sender;
            DataUser user = plugin.dataManager.getUserByPlayer(player);
            if(user.getPremium()){
                player.sendMessage(plugin.color("&6Ta komenda jest wylaczona dla graczy z kontem premium."));
                return;
            }
            if(!user.loggedIn){
                player.sendMessage(plugin.color("&cZaloguj sie, aby zmienic haslo!"));
                return;
            }
            if(args.length < 2){
                player.sendMessage(plugin.color("&cPoprawne uzycie: &6/changepass <haslo> <powtorz haslo>"));
                return;
            }
            if(!args[0].equals(args[1])){
                player.sendMessage(plugin.color("&cPodane hasla nie zgadzaja sie!"));
                return; 
            }
            try {
                user.setPassword(plugin.shaSalted(args[1], plugin.createSalt(16)));
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(ChangePasswordCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
            user.update();
            player.sendMessage(plugin.color("&8\u00BB &6Twoje haslo zostalo zmienione!"));
            return;
        }
    }
    
}
