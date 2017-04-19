package pl.xcrafters.xcrbungeeauth.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import pl.xcrafters.xcrbungeeauth.mysql.DataQuery;
import pl.xcrafters.xcrbungeeauth.data.DataManager.QueryType;

public class DataUser implements DataInterface {

    DataManager dataManager;
    
    public DataUser(DataManager dataManager){
        this.dataManager = dataManager;
    }
    
    public DataUser(DataManager dataManager, ResultSet rs) throws SQLException{
        this.dataManager = dataManager;
        id = rs.getInt("userID");
        nick = rs.getString("nick");
        uuid = rs.getString("uuid") != null ? UUID.fromString(rs.getString("uuid")) : null;
        pass = rs.getString("password");
        premium = rs.getBoolean("premium");
        firstIP = rs.getString("firstIP");
        lastIP = rs.getString("lastIP");
        firstJoined = rs.getLong("firstJoined");
        lastJoined = rs.getLong("lastJoined");
        checked = rs.getInt("checked") == 1;
        registeredByAdmin = rs.getInt("registeredByAdmin") == 1;
    }
    
    private boolean cNick, cUUID, cPass, cPremium, cFirstIP, cLastIP, cFirstJoined, cLastJoined, cChecked, cRegisteredByAdmin;
    
    private int id;
    
    private String nick, pass, firstIP, lastIP;
    private UUID uuid;
    private boolean premium, checked, registeredByAdmin;
    private Long firstJoined, lastJoined;
    
    public ServerInfo targetServer;
    public boolean loggedIn = false;
    public ScheduledTask messageTask;
    public String captcha;
    
    public void setPrimary(int id){
        this.id = id;
    }
    
    public int getPrimary(){
        return id;
    }
    
    public void setNick(String nick){ this.nick = nick; cNick = true; }
    public void setUUID(UUID uuid){ this.uuid = uuid; cUUID = true; }
    public void setPassword(String pass){ this.pass = pass; cPass = true; }
    public void setPremium(boolean premium){ this.premium = premium; cPremium = true; }
    public void setChecked(boolean checked){ this.checked = checked; cChecked = true; }
    public void setFirstIP(String firstIP){ this.firstIP = firstIP; cFirstIP = true; }
    public void setLastIP(String lastIP){ this.lastIP = lastIP; cLastIP = true; }
    public void setFirstJoined(Long firstJoined){ this.firstJoined = firstJoined; cFirstJoined = true; }
    public void setLastJoined(Long lastJoined){ this.lastJoined = lastJoined; cLastJoined = true; }
    public void setRegisteredByAdmin(boolean registeredByAdmin){ this.registeredByAdmin = registeredByAdmin; cRegisteredByAdmin = true; }
    
    public String getNick(){ return nick; }
    public UUID getUUID(){ return uuid; }
    public String getPassword(){ return pass; }
    public boolean getPremium(){ return premium; }
    public boolean getChecked(){ return checked; }
    public String getFirstIP(){ return firstIP; }
    public String getLastIP(){ return lastIP; }
    public Long getFirstJoined(){ return firstJoined; }
    public Long getLastJoined(){ return lastJoined; }
    public boolean getRegisteredByAdmin(){ return registeredByAdmin; }
    
    public void insert(){
        dataManager.users.put(nick.toLowerCase(), this);
        DataQuery query = new DataQuery(this, QueryType.INSERT);
        dataManager.plugin.mySQLManager.queries.add(query);
    }
    
    public void update(){
        if(cNick){
            for(Entry<String, DataUser> entry : dataManager.users.entrySet()){
                String nickname = entry.getKey();
                DataUser user = entry.getValue();
                if(user.getNick().equalsIgnoreCase(nick)){
                    dataManager.users.remove(nickname);
                }
            }
            dataManager.users.put(nick.toLowerCase(), this);
        }
        if(cUUID) {
            for(Entry<UUID, DataUser> entry : dataManager.usersByUUID.entrySet()){
                UUID uuid = entry.getKey();
                DataUser user = entry.getValue();
                if(user.equals(this)){
                    dataManager.usersByUUID.remove(uuid);
                }
            }
            if(uuid != null) {
                dataManager.usersByUUID.put(uuid, this);
            }
        }
        DataQuery query = new DataQuery(this, QueryType.UPDATE);
        dataManager.plugin.mySQLManager.queries.add(query);
    }
    
    public void delete(){
        dataManager.users.remove(nick.toLowerCase());
        dataManager.usersByUUID.remove(uuid);
        DataQuery query = new DataQuery(this, QueryType.DELETE);
        dataManager.plugin.mySQLManager.queries.add(query);
    }
    
    public String prepareQuery(QueryType type){
        String query = null;
        if(type == QueryType.DELETE){
            query = "DELETE FROM Users ";
            query += "WHERE userID=" + getPrimary();
            return query;
        }
        if(!cNick && !cPass && !cPremium && !cFirstIP && !cLastIP && !cFirstJoined && !cLastJoined){
            return query;
        }
        List<String> columns = new ArrayList();
        List<String> values = new ArrayList();
        if(cNick){
            cNick = false;
            columns.add("nick");
            values.add(nick);
        }
        if(cUUID){
            cUUID = false;
            columns.add("uuid");
            values.add(uuid == null ? null : uuid.toString());
        }
        if(cPass){
            cPass = false;
            columns.add("password");
            values.add(pass == null ? "" : pass);
        }
        if(cPremium){
            cPremium = false;
            columns.add("premium");
            values.add(String.valueOf(premium ? 1 : 0));
        }
        if(cChecked){
            cChecked = false;
            columns.add("checked");
            values.add(String.valueOf(checked ? 1 : 0));
        }
        if(cRegisteredByAdmin){
            cRegisteredByAdmin = false;
            columns.add("registeredByAdmin");
            values.add(String.valueOf(registeredByAdmin ? 1 : 0));
        }
        if(cFirstIP){
            cFirstIP = false;
            columns.add("firstIP");
            values.add(firstIP);
        }
        if(cLastIP){
            cLastIP = false;
            columns.add("lastIP");
            values.add(lastIP);
        }
        if(cFirstJoined){
            cFirstJoined = false;
            columns.add("firstJoined");
            values.add(String.valueOf(firstJoined));
        }
        if(cLastJoined){
            cLastJoined = false;
            columns.add("lastJoined");
            values.add(String.valueOf(lastJoined));
        }
        if(!values.isEmpty() && !columns.isEmpty()){
            if(type == QueryType.UPDATE){
                query = "UPDATE Users SET ";
                for(String column : columns){
                    int index = columns.indexOf(column);
                    String value = values.get(index);
                    String comma = "";
                    if(index > 0){
                        comma = ",";
                    }
                    if(value != null){
                        query += comma + column + "='" + value + "'";
                    } else {
                        query += comma + column + "=NULL";
                    }
                }
                query += " WHERE userID=" + getPrimary();
            }
            else if(type == QueryType.INSERT){
                query = "INSERT INTO Users (";
                for(String column : columns){
                    int index = columns.indexOf(column);
                    String comma = "";
                    if(index > 0){
                        comma = ",";
                    }
                    query += comma + column;
                }
                query += ") VALUES (";
                for(String value : values){
                    int index = values.indexOf(value);
                    String comma = "";
                    if(index > 0){
                        comma = ",";
                    }
                    if (value != null){
                        query += comma + "'" + value + "'";
                    } else {
                        query += comma + "NULL";
                    }
                }
                query += ")";
            }
        }
        return query;
    }

    public String getUpdateChannel(QueryType type) {
        switch (type) {
            case INSERT:
                return "AuthInsertUser";
            case UPDATE:
                return "AuthUpdateUser";
            case DELETE:
                return "AuthDeleteUser";
            default:
                return null;
        }
    }

    public void synchronize() {
        try {
            Connection conn = dataManager.plugin.mySQLManager.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Users WHERE userID=" + id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                this.id = rs.getInt("userID");
                this.nick = rs.getString("nick");
                this.uuid = rs.getString("uuid") != null ? UUID.fromString(rs.getString("uuid")) : null;
                this.pass = rs.getString("password");
                this.premium = rs.getBoolean("premium");
                this.firstIP = rs.getString("firstIP");
                this.lastIP = rs.getString("lastIP");
                this.firstJoined = rs.getLong("firstJoined");
                this.lastJoined = rs.getLong("lastJoined");
                this.checked = rs.getInt("checked") == 1;
                this.registeredByAdmin = rs.getInt("registeredByAdmin") == 1;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataUser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
