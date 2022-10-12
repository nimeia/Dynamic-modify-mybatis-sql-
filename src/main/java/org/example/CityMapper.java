package org.example;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

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

    @Select(" select * " +
            " from city " +
            "         left join hospital h on city.name = h.name " +
            "         left join school s on city.appid = s.appid " +
            " where city.name = #{name} ;")
    List<Map> selectCity(@Param("name") String name,@Param("appid") String appid);

    @Select(" select * " +
            " from city  ,hospital h " +
            "          left join school s on appid = s.appid " +
            " where city.name = #{name}  and city.name = h.name ;")
    List<Map> selectCityTwo(@Param("name") String name,@Param("appid") String appid);

    @Select(" select * from city c,school s where c.name = s.name ")
    List<Map> selectCityThree(@Param("name") String name,@Param("appid") String appid);

    @Select(" select * from city c,school s where c.name = s.name and c.name = '${city.name}' ")
    List<Map> selectCityFour(@Param("city") City city, @Param("appid") String ... appid);

    @Select(" select * from city c,school s where c.name = s.name and c.name = #{city.name} ")
    List<Map> selectCityFive(@Param("city") City city, @Param("appid") String ... appid);

    @Select(" select * from city c,school s where c.name = s.name and c.name = '${city.name}' ")
    List<Map> selectCitySix(City city, @Param("appid") String ... appid);

    @Select(" select * from city c,school s where c.name = s.name and c.name = '${name}' ")
    List<Map> selectCity7(City city, @Param("appid") String ... appid);

    @Select(" select * from city c,school s where c.name = s.name and c.name = '${name}' ")
    List<Map> selectCity8(@Param("name") String name,City city, @Param("appid") String ... appid);

    @Select(" select * from city c,school s where c.name = s.name and c.name = '${name}' ")
    List<Map> selectCity9( String name,City city, @Param("appid") String ... appid);


    @Select(" select * from city c,school s where c.name = s.name and c.name = '${city.name}'  limit ${pageSize} offset #{currentPage}")
    List<Map> selectPage(@Param("city") City city, @Param("currentPage") Integer currentPage, @Param("pageSize") Integer pageSize, @Param("appid") String ... appid);

    @Select("select * from (select * from city where name=#{city.name}) a ,school s where a.state='${city.state}' and a.name=s.name;")
    List<Map> subSelectTest(@Param("city") City city,@Param("appid") String ... appid);
}