package org.example;

import org.springframework.stereotype.Repository;

/**
 * CityDAO继承基类
 */
@Repository
public interface CityDAO extends MyBatisBaseDao<City, Integer> {
}