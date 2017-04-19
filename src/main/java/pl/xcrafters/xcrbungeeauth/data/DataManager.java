package pl.xcrafters.xcrbungeeauth.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import pl.xcrafters.xcrbungeeauth.AuthPlugin;

public class DataManager {

    AuthPlugin plugin;
    
    public ConcurrentHashMap<String, DataUser> users = new ConcurrentHashMap();
    public ConcurrentHashMap<UUID, DataUser> usersByUUID = new ConcurrentHashMap();

    public DataManager(AuthPlugin plugin){
        this.plugin = plugin;
    }

    public enum QueryType {INSERT, UPDATE, DELETE}; 
    
    public DataUser getUserByPlayer(ProxiedPlayer player){
        return getUserByNick(player.getName());
    }
    
    public DataUser getUserByNick(String nick){
        return users.get(nick.toLowerCase());
    }

    public DataUser getUserByUUID(UUID uuid) { return usersByUUID.get(uuid); }
    
    public DataUser createUser(String nick, AuthPlugin.OnlineType type, String firstIP, Long firstJoined) {
        DataUser user = new DataUser(this);
        user.setNick(nick);
        user.setPremium(type == AuthPlugin.OnlineType.ONLINE);
        user.setFirstIP(firstIP);
        user.setLastIP(firstIP);
        user.setFirstJoined(firstJoined);
        user.setLastJoined(firstJoined);
        user.setChecked(type != AuthPlugin.OnlineType.ERROR);
        return user;
    }
    
    public DataUser createUser(String nick) {
        DataUser user = new DataUser(this);
        user.setNick(nick);
        user.setPremium(plugin.hasPaid(nick) == AuthPlugin.OnlineType.ONLINE);
        return user;
    }

    public List<DataUser> getUsersByIP(String ip) {
        List<DataUser> users = new ArrayList();
        for(DataUser user : this.users.values()){
            if(user.getFirstIP() != null && user.getLastIP() != null && (user.getFirstIP().equals(ip) || user.getLastIP().equals(ip))){
                users.add(user);
            }
        }
        return users;
    }

    public DataUser getUserById(int id){
        for(DataUser user : users.values()){
            if(user.getPrimary() == id){
                return user;
            }
        }
        return null;
    }

    public List<DataUser> getOnlineUsersByIP(String ip) {
        List<DataUser> users = new ArrayList();
        for(ProxiedPlayer online : ProxyServer.getInstance().getPlayers()){
            DataUser user = getUserByPlayer(online);
            if(user != null && online.getAddress().getAddress().getHostAddress().equals(ip)){
                users.add(user);
            }
        }
        return users;
    }
    
    public DataLogin createLogin(String nick, String ip) {
        DataLogin login = new DataLogin(this);
        login.setNick(nick);
        login.setIP(ip);
        return login;
    }
    
}
