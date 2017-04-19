package pl.xcrafters.xcrbungeeauth;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import pl.xcrafters.xcrbungeeauth.commands.AuthCommand;
import pl.xcrafters.xcrbungeeauth.commands.LoginCommand;
import pl.xcrafters.xcrbungeeauth.data.DataManager;
import pl.xcrafters.xcrbungeeauth.data.DataUser;
import pl.xcrafters.xcrbungeeauth.commands.ChangePasswordCommand;
import pl.xcrafters.xcrbungeeauth.commands.RegisterCommand;
import pl.xcrafters.xcrbungeeauth.listeners.*;
import pl.xcrafters.xcrbungeeauth.mysql.MySQLManager;
import pl.xcrafters.xcrbungeeauth.redis.RedisManager;

public class AuthPlugin extends Plugin {

    public ConfigManager configManager;
    public DataManager dataManager;
    public MySQLManager mySQLManager;
    public RedisManager redisManager;
    
    AuthCommand authCommand;
    ChangePasswordCommand changePasswordCommand;
    LoginCommand loginCommand;
    RegisterCommand registerCommand;
    
    PreLoginListener preLoginListener;
    ServerConnectListener serverConnectListener;
    PlayerDisconnectListener playerDisconnectListener;
    PostLoginListener postLoginListener;
    PlayerChatListener playerChatListener;
    ServerConnectedListener serverConnectedListener;
    RedisListener redisListener;

    public enum OnlineMode {OFFLINE, ONLINE, RESTRICTED};
    public enum OnlineType {OFFLINE, ONLINE, ERROR};
    
    static AuthPlugin instance;
    
    @Override
    public void onEnable(){
        this.configManager = new ConfigManager(this);
        this.dataManager = new DataManager(this);
        try {
            this.mySQLManager = new MySQLManager(this);
        } catch (SQLException ex) {
            Logger.getLogger(AuthPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.redisManager = new RedisManager(this);
        
        this.authCommand = new AuthCommand(this);
        this.changePasswordCommand = new ChangePasswordCommand(this);
        this.loginCommand = new LoginCommand(this);
        this.registerCommand = new RegisterCommand(this);
        
        this.preLoginListener = new PreLoginListener(this);
        this.serverConnectListener = new ServerConnectListener(this);
        this.playerDisconnectListener = new PlayerDisconnectListener(this);
        this.postLoginListener = new PostLoginListener(this);
        this.playerChatListener = new PlayerChatListener(this);
        this.serverConnectedListener = new ServerConnectedListener(this);
        this.redisListener = new RedisListener(this);

        instance = this;
    }
    
    @Override
    public void onDisable(){
        try {
            mySQLManager.saveAll();
        } catch (SQLException ex) {
            Logger.getLogger(AuthPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
        for(ProxiedPlayer player : ProxyServer.getInstance().getPlayers()){
            DataUser user = dataManager.getUserByPlayer(player);
            if(!user.loggedIn){
                player.disconnect(color("&cZostales wyrzucony z powodu przeladowania serwera!"));
            }
        }
    }

    public static AuthPlugin getInstance() {
        return instance;
    }

    Gson gson = new Gson();
    
    public String color(String string){
        return string.replaceAll("&([0-9a-z])", "\u00A7$1");
    }
    
     public Long parseTimeSpan(String string){
        String[] p = string.split(":");
        if (p.length == 3)
        {
            int h = Integer.parseInt(p[0]);
            int m = Integer.parseInt(p[1]);
            int s = Integer.parseInt(p[2]);
            long time = 1000 * s + 60000 * m + 3600000 * h;
            return time;
        }
        return -1L;
    }
    
    public String sha256(String msg) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        sha.reset();
        sha.update(msg.getBytes());
        byte[] digest = sha.digest();
        return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest));
    }

    public String shaSalted(String message, String salt) throws NoSuchAlgorithmException {
        return "$SHA$" + salt + "$" + sha256(sha256(message) + salt);
    }

    public String createSalt(int length) throws NoSuchAlgorithmException {
        SecureRandom rnd = new SecureRandom();
        byte[] msg = new byte[40];
        rnd.nextBytes(msg);
        MessageDigest sha = MessageDigest.getInstance("SHA1");
        sha.reset();
        byte[] digest = sha.digest(msg);
        return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest)).substring(0, length);
    }

    public boolean cmpPassWithHash(String pass, String hash) {
        if (hash.contains("$")) {
            String[] line = hash.split("\\$");
            if (line.length > 3 && line[1].equals("SHA")) {
                try {
                    return hash.equals(shaSalted(pass, line[2]));
                } catch(NoSuchAlgorithmException ex){
                    
                }
            }
        }
        return false;
    }
    
    public OnlineType hasPaid(String nick){
        try {
            URL url = new URL("https://minecraft.net/haspaid.jsp?user=" + nick);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String hasPaid;
            while ((hasPaid = in.readLine()) != null) {
                if(Boolean.valueOf(hasPaid)){
                    in.close();
                    return OnlineType.ONLINE;
                }
                break;
            }
            in.close();
        } catch (MalformedURLException ex) {
        } catch (IOException ex) {
        }
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(
                    "https://api.mojang.com/profiles/minecraft").
                    openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(connection.
                    getOutputStream());
            out.write(("[\"" + nick + "\"]").getBytes());
            out.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            Profile[] profiles = gson.fromJson(reader, Profile[].class);
            if (profiles != null && profiles.length >= 1) {
                for(Profile profile : profiles){
                    if(profile.name.equalsIgnoreCase(nick)){
                        return OnlineType.ONLINE;
                    }
                }
            }
            return OnlineType.OFFLINE;
        } catch (IOException ex) {
            getLogger().log(Level.INFO, "Blad wyboru trybu logowania dla gracza " + nick + ".");
        }
        return OnlineType.ERROR;
    }

    private static final String CHAR_LIST = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    public String generateRandomString(){
        StringBuffer randStr = new StringBuffer();
        for(int i=0; i<8; i++){
            int number = getRandomNumber();
            char ch = CHAR_LIST.charAt(number);
            randStr.append(ch);
        }
        return randStr.toString();
    }

    private int getRandomNumber() {
        int randomInt = 0;
        Random randomGenerator = new Random();
        randomInt = randomGenerator.nextInt(CHAR_LIST.length());
        if (randomInt - 1 == -1) {
            return randomInt;
        } else {
            return randomInt - 1;
        }
    }
    
}
