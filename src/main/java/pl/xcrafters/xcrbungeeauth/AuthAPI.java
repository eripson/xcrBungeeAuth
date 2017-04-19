package pl.xcrafters.xcrbungeeauth;

import pl.xcrafters.xcrbungeeauth.data.DataUser;

import java.util.UUID;

public class AuthAPI {

    public static boolean isPremium(String nick) {
        DataUser user = AuthPlugin.getInstance().dataManager.getUserByNick(nick);
        if(user != null){
            return user.getPremium();
        }
        return false;
    }

    public static String getIP(String nick){
        DataUser user = AuthPlugin.getInstance().dataManager.getUserByNick(nick);
        if(user != null){
            return user.getLastIP();
        }
        return null;
    }

    public static int getTotalPlayers(){
        return AuthPlugin.getInstance().dataManager.users.size();
    }

    public static UUID getUUIDByNick(String nick) {
        DataUser user = AuthPlugin.getInstance().dataManager.getUserByNick(nick);
        if(user != null) {
            return user.getUUID();
        }
        return null;
    }

    public static String getNickByUUID(UUID uuid) {
        DataUser user = AuthPlugin.getInstance().dataManager.getUserByUUID(uuid);
        if(user != null) {
            return user.getNick();
        }
        return null;
    }

}
