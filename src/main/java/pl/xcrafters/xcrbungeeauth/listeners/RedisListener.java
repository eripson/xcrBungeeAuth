package pl.xcrafters.xcrbungeeauth.listeners;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import pl.xcrafters.xcrbungeeauth.AuthPlugin;
import pl.xcrafters.xcrbungeeauth.data.DataUser;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

public class RedisListener extends JedisPubSub {

    AuthPlugin plugin;

    public RedisListener(AuthPlugin plugin) {
        this.plugin = plugin;
        plugin.redisManager.subscribe(this, "AuthInsertUser", "AuthUpdateUser", "AuthDeleteUser");
    }

    Gson gson = new Gson();

    public void onMessage(String channel, final String json) {
        try {
            JsonObject object = gson.fromJson(json, JsonObject.class);

            int id = object.get("id").getAsInt();
            String instance = object.get("instance").getAsString();

            if(instance.equals(plugin.redisManager.getInstance())) {
                return;
            }

            if (channel.equals("AuthInsertUser")) {
                DataUser user = plugin.mySQLManager.loadUser(id);
                plugin.dataManager.users.put(user.getNick().toLowerCase(), user);
                if(user.getUUID() != null) {
                    plugin.dataManager.usersByUUID.put(user.getUUID(), user);
                }
            }
            if (channel.equals("AuthUpdateUser")) {
                DataUser user = plugin.dataManager.getUserById(id);

                UUID uuid = user.getUUID();

                user.synchronize();

                UUID afterUUID = user.getUUID();

                if(uuid == null && afterUUID != null) {
                    plugin.dataManager.usersByUUID.put(user.getUUID(), user);
                }
            }
            if (channel.equals("AuthDeleteUser")) {
                DataUser user = plugin.dataManager.getUserById(id);
                plugin.dataManager.users.remove(user.getNick().toLowerCase());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onSubscribe(String channel, int subscribedChannels) {
    }

    public void onUnsubscribe(String channel, int subscribedChannels) {
    }

    public void onPSubscribe(String pattern, int subscribedChannels) {
    }

    public void onPUnsubscribe(String pattern, int subscribedChannels) {
    }

    public void onPMessage(String pattern, String channel, String message) {
    }

}
