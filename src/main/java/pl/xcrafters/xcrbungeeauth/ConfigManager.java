package pl.xcrafters.xcrbungeeauth;

import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import pl.xcrafters.xcrbungeeauth.AuthPlugin.OnlineMode;

import java.io.*;

public class ConfigManager {

    AuthPlugin plugin;
    
    Configuration config;
    
    public ConfigManager(AuthPlugin plugin){
        this.plugin = plugin;
        load();
    }

    public OnlineMode onlineMode;
    
    public String mysqlHost;
    public String mysqlBase;
    public String mysqlUser;
    public String mysqlPass;

    public String redisHost;
    
    public boolean sessionsEnabled;
    public Long sessionsTime;
    
    public int allowedMultiAccounts;
    
    public ServerInfo authServer;

    public void load(){
        saveDefaultConfig();
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(this.plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        onlineMode = OnlineMode.valueOf(config.getString("config.online-mode"));
        
        mysqlHost = config.getString("config.mysql.host");
        mysqlBase = config.getString("config.mysql.base");
        mysqlUser = config.getString("config.mysql.user");
        mysqlPass = config.getString("config.mysql.pass");

        redisHost = config.getString("config.redis.host");
        
        sessionsEnabled = config.getBoolean("config.sessions-enabled");
        sessionsTime = plugin.parseTimeSpan(config.getString("config.sessions-time"));
        
        authServer = ProxyServer.getInstance().getServerInfo(config.getString("config.auth-server"));
        
        allowedMultiAccounts = config.getInt("config.allowed-multiaccounts");
    }
    
    public void save(){
        config.set("config.online-mode", onlineMode.name().toUpperCase());

        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDefaultConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                InputStream is = plugin.getResourceAsStream("config.yml");
                OutputStream os = new FileOutputStream(configFile);
                ByteStreams.copy(is, os);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
}
