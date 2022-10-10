package org.example;

public class CityInsertProvider {

    public String insertCity(City city){
        if(System.currentTimeMillis()%2==0){
            return "insert into city (state, name) " +
                    "values (#{state},'${name}');";
        }else {
            return "insert into city (state, name ,top_id) " +
                    "values (#{state},'${name}','${topId}');";
        }

    }
}
