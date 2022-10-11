package org.example;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.example.School;
import org.example.SchoolExample;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolDAO {
    long countByExample(@Param("example") SchoolExample example,@Param("appid") String ...appid);

    int deleteByExample(SchoolExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(School record);

    int insertSelective(School record);

    List<School> selectByExample(SchoolExample example);

    School selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") School record, @Param("example") SchoolExample example);

    int updateByExample(@Param("record") School record, @Param("example") SchoolExample example);

    int updateByPrimaryKeySelective(School record);

    int updateByPrimaryKey(School record);
}