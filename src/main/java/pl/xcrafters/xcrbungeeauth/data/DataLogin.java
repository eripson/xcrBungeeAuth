package pl.xcrafters.xcrbungeeauth.data;

import pl.xcrafters.xcrbungeeauth.mysql.DataQuery;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataLogin implements DataInterface {

    private int id;

    public void setPrimary(int id) { this.id = id; }

    public int getPrimary() { return id; }

    DataManager dataManager;

    public DataLogin(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public DataLogin(DataManager dataManager, ResultSet rs) {
        this.dataManager = dataManager;
        try {
            id = rs.getInt("loginID");
            nick = rs.getString("nick");
            ip = rs.getString("ip");
            time = rs.getLong("time");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean cNick, cIP, cTime;

    private String nick, ip;
    private long time;

    public void setNick(String nick) { this.nick = nick; cNick = true; }
    public void setIP(String ip) { this.ip = ip; cIP = true; }
    public void setTime(long time) { this.time = time; cTime = true; }

    public String getNick() { return nick; }
    public String getIP() { return ip; }
    public long getTime() { return time; }

    public void insert() {
        DataQuery query = new DataQuery(this, DataManager.QueryType.INSERT);
        dataManager.plugin.mySQLManager.queries.add(query);
    }

    public void update() { }

    public void delete() { }

    public String prepareQuery(DataManager.QueryType type){
        String query = null;
        if(type == DataManager.QueryType.DELETE){
            query = "DELETE FROM Logins ";
            query += "WHERE loginID=" + getPrimary();
            return query;
        }
        if(!cNick && !cIP && !cTime){
            return query;
        }
        List<String> columns = new ArrayList();
        List<String> values = new ArrayList();
        if(cNick){
            cNick = false;
            columns.add("nick");
            values.add(nick);
        }
        if(cIP){
            cIP = false;
            columns.add("ip");
            values.add(ip);
        }
        if(cTime){
            cTime = false;
            columns.add("time");
            values.add(String.valueOf(time));
        }
        if(!values.isEmpty() && !columns.isEmpty()){
            if(type == DataManager.QueryType.UPDATE){
                query = "UPDATE Logins SET ";
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
                query += " WHERE loginID=" + getPrimary();
            }
            else if(type == DataManager.QueryType.INSERT){
                query = "INSERT INTO Logins (";
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

    public String getUpdateChannel(DataManager.QueryType type) {
        return null;
    }

    public void synchronize() { }

}
