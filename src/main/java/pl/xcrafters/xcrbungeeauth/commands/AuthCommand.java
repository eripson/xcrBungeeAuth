package pl.xcrafters.xcrbungeeauth.commands;

import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import pl.xcrafters.xcrbungeeauth.data.DataLogin;
import pl.xcrafters.xcrbungeeauth.AuthPlugin;
import pl.xcrafters.xcrbungeeauth.data.DataUser;

public class AuthCommand extends Command{

    AuthPlugin plugin;
    
    public AuthCommand(AuthPlugin plugin){
        super("auth", "auth.manage");
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerCommand(plugin, this);
    }
    
    public void execute(final CommandSender sender, final String[] args) {
        if(args.length == 0){
            sender.sendMessage(plugin.color("&6/auth register <nick> [hasło] &7- rejestruje nowego gracza"));
            sender.sendMessage(plugin.color("&6/auth unregister <nick> &7- odrejestrowuje gracza"));
            sender.sendMessage(plugin.color("&6/auth changepass <nick> <haslo> &7- zmiana hasla dla gracza"));
            sender.sendMessage(plugin.color("&6/auth premium <nick> &7- zmienia tryb logowania dla danego gracza (premium/non-premium)"));
            sender.sendMessage(plugin.color("&6/auth mode <online/offline> &7- zmiana trybu logowania (tylko osoby z premium/wszyscy)"));
            sender.sendMessage(plugin.color("&6/auth multi <nick/ip> &7- sprawdzanie multikont gracza"));
            sender.sendMessage(plugin.color("&6/auth ip <nick> &7- sprawdzanie adresow IP przypisanych do gracza"));
            sender.sendMessage(plugin.color("&6/auth track <nick/ip> &7- sprawdzanie ostatnich logowan dla nicku lub adresu IP"));
            sender.sendMessage(plugin.color("&6/auth reload &7- przeladowanie konfiguracji"));
            return;
        }
        if(args[0].equalsIgnoreCase("register") && (args.length == 2 || args.length == 3)) {
            if(!sender.hasPermission("auth.register")) {
                sender.sendMessage(plugin.color("&cNie masz uprawnien do rejestracji kont!"));
                return;
            }
            final String nick = args[1];
            DataUser user = plugin.dataManager.getUserByNick(nick);
            if(user != null) {
                sender.sendMessage(plugin.color("&cGracz o nicku " + user.getNick() + " jest juz zarejestrowany!"));
                return;
            }
            sender.sendMessage(plugin.color("&6Rozpoczynanie procesu rejestracji gracza &8" + nick + "&6..."));
            ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
                public void run() {
                    DataUser user = plugin.dataManager.createUser(nick);
                    if(args.length == 3) {
                        try {
                            user.setPassword(plugin.shaSalted(args[2], plugin.createSalt(16)));
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                    }
                    user.setRegisteredByAdmin(true);
                    user.insert();
                    sender.sendMessage(plugin.color("&6Pomyslnie zarejestrowano gracza &8" + user.getNick() + "&6!"));
                }
            });
        } else if(args[0].equalsIgnoreCase("unregister") && args.length == 2) {
            if(!sender.hasPermission("auth.unregister")) {
                sender.sendMessage(plugin.color("&cNie masz uprawnien do odrejestrowywania kont!"));
                return;
            }
            DataUser user = plugin.dataManager.getUserByNick(args[1]);
            if(user == null){
                sender.sendMessage(plugin.color("&cGracza o podanym nicku nie ma w bazie danych!"));
                return;
            }
            user.delete();
            if(ProxyServer.getInstance().getPlayer(user.getNick()) != null){
                ProxyServer.getInstance().getPlayer(user.getNick()).disconnect(plugin.color("&6Zostales odrejestrowany przez administratora!"));
            }
            sender.sendMessage(plugin.color("&6Pomyslnie odrejestrowano gracza &8" + user.getNick() + "&6!"));
        } else if(args[0].equalsIgnoreCase("changepass") && args.length == 3) {
            if(!sender.hasPermission("auth.changepass")) {
                sender.sendMessage(plugin.color("&cNie masz uprawnien do zmiany hasel graczy!"));
                return;
            }
            DataUser user = plugin.dataManager.getUserByNick(args[1]);
            if(user == null){
                sender.sendMessage(plugin.color("&cGracza o podanym nicku nie ma w bazie danych!"));
                return;
            }
            try {
                user.setPassword(plugin.shaSalted(args[2], plugin.createSalt(16)));
            } catch (NoSuchAlgorithmException ex) {
            }
            user.update();
            sender.sendMessage(plugin.color("&6Pomyslnie zmieniono haslo gracza &8" + user.getNick() + "&6!"));
        } else if(args[0].equalsIgnoreCase("premium") && args.length == 2) {
            if(!sender.hasPermission("auth.premium")) {
                sender.sendMessage(plugin.color("&cNie masz uprawnien do zmiany trybu premium!"));
                return;
            }
            DataUser user = plugin.dataManager.getUserByNick(args[1]);
            if(user == null){
                sender.sendMessage(plugin.color("&cGracza o podanym nicku nie ma w bazie danych!"));
                return;
            }
            user.setUUID(null);
            user.setPremium(!user.getPremium());
            user.update();
            if(ProxyServer.getInstance().getPlayer(user.getNick()) != null){
                ProxyServer.getInstance().getPlayer(user.getNick()).disconnect(plugin.color("&6Twoj tryb premium zostal zmieniony na tryb " + (user.getPremium() ? "&2premium" : "&4non-premium") + "&6!"));
            }
            sender.sendMessage(plugin.color("&6Pomyslnie zmieniono tryb gracza &8" + user.getNick() + " &6na tryb " + (user.getPremium() ? "&2premium" : "&4non-premium") + "&6!"));
        } else if(args[0].equalsIgnoreCase("mode") && args.length == 2) {
            if(!sender.hasPermission("auth.mode")) {
                sender.sendMessage(plugin.color("&cNie masz uprawnien do zmiany trybu OnlineMode!"));
                return;
            }
            if(!args[1].equalsIgnoreCase("online") && !args[1].equalsIgnoreCase("offline")){
                sender.sendMessage(plugin.color("&cNiepoprawny tryb OnlineMode!"));
                return;
            }
            AuthPlugin.OnlineMode mode = args[1].equalsIgnoreCase("online") ? AuthPlugin.OnlineMode.ONLINE : AuthPlugin.OnlineMode.OFFLINE;
            plugin.configManager.onlineMode = mode;
            sender.sendMessage(plugin.color("&cUstawiono tryb OnlineMode na " + mode.name()));
        } else if(args[0].equalsIgnoreCase("ip") && args.length == 2) {
            if(!sender.hasPermission("auth.ip")) {
                sender.sendMessage(plugin.color("&cNie masz uprawnien do sprawdzania adresow IP przypisanych do konta gracza!"));
                return;
            }
            final String nick = args[1];
            ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
                public void run() {
                    sender.sendMessage(plugin.color("&6Adresy IP przypisane do konta gracza " + nick + ":"));
                    String toSend = ChatColor.GRAY + "";
                    for (String ip : plugin.mySQLManager.getIPs(nick)) {
                        toSend += ip + ", ";
                    }
                    sender.sendMessage(toSend);
                }
            });
        } else if(args[0].equalsIgnoreCase("multi") && args.length == 2) {
            if(!sender.hasPermission("auth.multi")) {
                sender.sendMessage(plugin.color("&cNie masz uprawnien do sprawdzania multikont graczy!"));
                return;
            }
            ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
                public void run() {
                    String ip;
                    if(args[1].matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
                        ip = args[1];
                        sender.sendMessage(plugin.color("&6Konta przypisane do adresu IP " + ip + ":"));
                    } else {
                        String nick = args[1];
                        DataUser user = plugin.dataManager.getUserByNick(nick);
                        if(user == null) {
                            sender.sendMessage(plugin.color("&cGracza o podanym nicku nie ma w bazie danych!"));
                            return;
                        }
                        ip = user.getLastIP();
                        sender.sendMessage(plugin.color("&6Multikonta gracza " + user.getNick() + ":"));
                    }
                    String toSend = ChatColor.GRAY + "";
                    for(DataUser data : plugin.mySQLManager.getUsersByIP(ip)) {
                        toSend += data.getNick() + (data.getRegisteredByAdmin() ? " [A] " : "") + ", ";
                    }
                    sender.sendMessage(toSend);
                }
            });
        } else if(args[0].equalsIgnoreCase("track") && args.length == 2) {
            if(!sender.hasPermission("auth.track")) {
                sender.sendMessage(plugin.color("&cNie masz uprawnien do sprawdzania ostatnich logowan!"));
                return;
            }
            ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
                public void run() {
                    SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if (args[1].matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
                        String ip = args[1];
                        sender.sendMessage(plugin.color("&6Ostatnie logowania dla adresu IP " + ip + ":"));
                        sender.sendMessage(plugin.color("&8[nick - ip - czas]"));
                        for (DataLogin login : plugin.mySQLManager.getLastLoginsByIP(ip)) {
                            sender.sendMessage(plugin.color("&7" + login.getNick() + " - " + login.getIP() + " - " + dt.format(login.getTime())));
                        }
                    } else {
                        String nick = args[1];
                        sender.sendMessage(plugin.color("&6Ostatnie logowania gracza " + nick + ":"));
                        sender.sendMessage(plugin.color("&8[nick - ip - czas]"));
                        for (DataLogin login : plugin.mySQLManager.getLastLoginsByNick(nick)) {
                            sender.sendMessage(plugin.color("&7" + login.getNick() + " - " + login.getIP() + " - " + dt.format(login.getTime())));
                        }
                    }
                }
            });
        } else if(args[0].equalsIgnoreCase("reload")) {
            if(!sender.hasPermission("auth.reload")) {
                sender.sendMessage(plugin.color("&cNie masz uprawnien do przeladowywania konfiguracji!"));
                return;
            }
            plugin.configManager.load();
            sender.sendMessage(ChatColor.GOLD + "Poprawnie przeladowano plik konfiguracji.");
        } else {
            sender.sendMessage(plugin.color("&6/auth register <nick> &7- rejestruje nowego gracza"));
            sender.sendMessage(plugin.color("&6/auth unregister <nick> &7- odrejestrowuje gracza"));
            sender.sendMessage(plugin.color("&6/auth changepass <nick> <haslo> &7- zmiana hasla dla gracza"));
            sender.sendMessage(plugin.color("&6/auth premium <nick> &7- zmienia tryb logowania dla danego gracza (premium/non-premium)"));
            sender.sendMessage(plugin.color("&6/auth mode <online/offline> &7- zmiana trybu logowania (tylko osoby z premium/wszyscy)"));
            sender.sendMessage(plugin.color("&6/auth multi <nick/ip> &7- sprawdzanie multikont gracza"));
            sender.sendMessage(plugin.color("&6/auth ip <nick> &7- sprawdzanie adresow IP przypisanych do gracza"));
            sender.sendMessage(plugin.color("&6/auth ip <nick/ip> &7- sprawdzanie ostatnich logowan dla nicku lub adresu IP"));
            sender.sendMessage(plugin.color("&6/auth reload &7- przeladowanie konfiguracji"));
        }
    }
    
}
