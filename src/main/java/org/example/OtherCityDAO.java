package org.example;

import org.springframework.stereotype.Repository;

/**
 * CityDAO继承基类
 */
@Repository
public interface OtherCityDAO extends MyBatisBaseDao<OtherCity, Integer> {
}