package pl.xcrafters.xcrbungeeauth.commands;

import java.security.NoSuchAlgorithmException;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import pl.xcrafters.xcrbungeeauth.AuthPlugin;
import pl.xcrafters.xcrbungeeauth.data.DataLogin;
import pl.xcrafters.xcrbungeeauth.data.DataUser;

public class RegisterCommand extends Command{

    AuthPlugin plugin;
    
    public RegisterCommand(AuthPlugin plugin){
        super("register", null);
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
            if(user.getPassword() != null && !user.getPremium()){
                player.sendMessage(plugin.color("&cJestes juz zarejestrowany!"));
                return;
            }
            if(args.length < 3){
                player.sendMessage(plugin.color("&cPoprawne uzycie: &6/register <haslo> <powtorz haslo> &4<kod z mapy>"));
                return;
            }
            if(args[0].equals(args[1])){
                if(!args[2].equals(user.captcha)){
                    player.sendMessage(plugin.color("&cNiepoprawny kod captcha! Poprawny kod z mapy to: &6" + user.captcha));
                    user.captcha = plugin.generateRandomString();
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("Captcha");
                    out.writeUTF(player.getName());
                    out.writeUTF(user.captcha);
                    player.getServer().getInfo().sendData("BungeeCord", out.toByteArray());
                    return;
                }
                try {
                    user.setPassword(plugin.shaSalted(args[1], plugin.createSalt(16)));
                } catch (NoSuchAlgorithmException ex) {
                }
                user.setLastIP(player.getAddress().getAddress().getHostAddress());
                user.setLastJoined(System.currentTimeMillis());
                user.update();
                player.sendMessage(plugin.color("&8\u00BB &6Pomyslnie zarejestrowano!"));
                user.loggedIn = true;
                player.connect(user.targetServer);
                if(user.messageTask != null) {
                    user.messageTask.cancel();
                    user.messageTask = null;
                }
                user.targetServer = null;

                DataLogin login = plugin.dataManager.createLogin(player.getName(), player.getAddress().getAddress().getHostAddress());
                login.setTime(System.currentTimeMillis());
                login.insert();
            } else {
                player.sendMessage(plugin.color("&cPodane hasla nie zgadzaja sie!"));
            }
        }
    }
    
}
