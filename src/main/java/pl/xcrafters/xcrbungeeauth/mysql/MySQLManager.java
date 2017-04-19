package pl.xcrafters.xcrbungeeauth.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import pl.xcrafters.xcrbungeeauth.AuthPlugin;
import pl.xcrafters.xcrbungeeauth.data.DataLogin;
import pl.xcrafters.xcrbungeeauth.data.DataManager;
import pl.xcrafters.xcrbungeeauth.data.DataUser;

public class MySQLManager {

    AuthPlugin plugin;
    
    public MySQLManager(AuthPlugin plugin) throws SQLException{
        this.plugin = plugin;
        runScheduler();
        loadAll();
    }
    
    private Connection conn;
    
    public List<DataQuery> queries = new ArrayList();
    
    private Object saveLock = new Object();
    
    public Connection prepareConnection() {
        for (int i = 0; i < 5; i++) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                String url = "jdbc:mysql://" + plugin.configManager.mysqlHost + "/" + plugin.configManager.mysqlBase;
                return DriverManager.getConnection(url, plugin.configManager.mysqlUser, plugin.configManager.mysqlPass);
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "CANNOT CONNECT TO DATABASE!", ex);
            } catch (ClassNotFoundException ex) {
                plugin.getLogger().log(Level.SEVERE, "JDBC IS NOT FOUND - CANNOT CONNECT TO DATABASE!", ex);
            }
        }
        return null;
    }

    Gson gson = new Gson();

    public Connection getConnection() throws SQLException {
        if(conn == null || conn.isClosed()){
            conn = prepareConnection();
        }
        return conn;
    }
    
    public void runScheduler(){
        plugin.getProxy().getScheduler().schedule(plugin, new Runnable(){
            public void run(){
                try {
                    saveAll();
                } catch (SQLException ex) {
                    Logger.getLogger(MySQLManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, 10, 10, TimeUnit.SECONDS);
    }
    
    public void loadAll() throws SQLException{
        Connection connection = getConnection();
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM Users");
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
            DataUser user = new DataUser(plugin.dataManager, rs);
            plugin.dataManager.users.put(user.getNick().toLowerCase(), user);
            if(user.getUUID() != null) {
                plugin.dataManager.usersByUUID.put(user.getUUID(), user);
            }
        }
    }
    
    public void reloadAll() throws SQLException{
        saveAll();
        plugin.dataManager.users.clear();
        loadAll();
    }
    
    public void saveAll() throws SQLException{
        DataQuery[] toSend;
        synchronized(saveLock){
            toSend = new DataQuery[queries.size()];
            toSend = queries.toArray(toSend);
            queries.clear();
        }
        if(toSend.length > 0){
            for(DataQuery dataQuery : toSend){
                if(getConnection() != null){
                    DataManager.QueryType type = dataQuery.type;
                    String query = dataQuery.data.prepareQuery(type);
                    if(query != null){
                        try {
                            if(type == DataManager.QueryType.INSERT && dataQuery.data != null){
                                Statement statement = getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                                statement.execute(query, Statement.RETURN_GENERATED_KEYS);
                                ResultSet rs = statement.getGeneratedKeys();
                                while (rs.next()) {
                                    dataQuery.data.setPrimary(rs.getInt(1));
                                }
                                rs.close();
                                statement.close();
                            } else {
                                PreparedStatement statement = getConnection().prepareStatement(query);
                                statement.executeUpdate();
                                statement.close();
                            }

                            if(dataQuery.data.getUpdateChannel(type) != null) {
                                JsonObject object = new JsonObject();
                                object.addProperty("id", dataQuery.data.getPrimary());
                                object.addProperty("instance", plugin.redisManager.getInstance());

                                plugin.redisManager.sendMessage(dataQuery.data.getUpdateChannel(type), gson.toJson(object));
                            }
                        } catch (SQLException ex) {
                            plugin.getLogger().log(Level.WARNING, "Wystapil blad podczas zapisu query: " + query);
                            plugin.getLogger().log(Level.WARNING, ex.getMessage());
                        }
                    }
                } else {
                    queries.add(dataQuery);
                }
            }
        }
    }

    public List<String> getIPs(String nick) {
        List<String> ips = new ArrayList();
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Logins WHERE nick=?");
            ps.setString(1, nick);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String ip = rs.getString("ip");
                if(!ips.contains(ip)) {
                    ips.add(ip);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ips;
    }

    public List<DataUser> getUsersByIP(String ip) {
        List<DataUser> users = new ArrayList();
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Logins WHERE ip=?");
            ps.setString(1, ip);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                DataUser user = plugin.dataManager.getUserByNick(rs.getString("nick"));
                if(user != null && !users.contains(user)) {
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public List<DataLogin> getLastLoginsByNick(String nick) {
        List<DataLogin> logins = new ArrayList();
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Logins WHERE nick=? ORDER BY loginID DESC LIMIT 0,30");
            ps.setString(1, nick);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                DataLogin login = new DataLogin(plugin.dataManager, rs);
                logins.add(login);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logins;
    }

    public List<DataLogin> getLastLoginsByIP(String ip) {
        List<DataLogin> logins = new ArrayList();
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Logins WHERE ip=? ORDER BY loginID DESC LIMIT 0,30");
            ps.setString(1, ip);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                DataLogin login = new DataLogin(plugin.dataManager, rs);
                logins.add(login);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logins;
    }

    public DataUser loadUser(int id){
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Users WHERE userID=" + id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                DataUser user = new DataUser(plugin.dataManager, rs);
                return user;
            }
        } catch (SQLException ex) {
            Logger.getLogger(MySQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
