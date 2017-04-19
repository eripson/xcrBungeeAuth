package pl.xcrafters.xcrbungeeauth.mysql;

import pl.xcrafters.xcrbungeeauth.data.DataInterface;
import pl.xcrafters.xcrbungeeauth.data.DataManager.QueryType;

public class DataQuery {

    public DataQuery(DataInterface data, QueryType type){
        this.data = data;
        this.type = type;
    }
    
    public DataInterface data;
    public QueryType type;
    
}
