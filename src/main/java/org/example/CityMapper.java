package org.example;

import org.apache.ibatis.annotations.*;

@Mapper
public interface CityMapper {

    @Select("SELECT * FROM city WHERE state = #{state}")
    City findByState(@Param("state") String state);

    @Insert("insert into city ( state, name) " +
            "values (#{city.state},#{city.name});")
    void insert(@Param("city") City city);

    @Insert("insert into city ( state, name) " +
            "values ('${city.state}','${city.name}');")
    void insertTwo(@Param("city") City city);

    @Insert("insert into city ( state, name) " +
            "values ('${city.state}','${city.name}');")
    void insertFour(@Param("city") City city);

    @Insert("insert into city ( state, name, top_id) " +
            "values ('${city.state}','${city.name}', #{city.topId});")
    void insertFive(@Param("city") City city);

    @InsertProvider(type = CityInsertProvider.class , method = "insertCity")
    void insertSix(City city);

    @Insert("insert into city ( state, name,appid) " +
            "values ('${city.state}','${city.name}','${city.appid}');")
    void insertThree(@Param("city") City city);

    @Insert("insert into city ( state, name) " +
            "values ('1111','222222')")
    void insertOne();
}