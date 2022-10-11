package org.example;

import org.springframework.stereotype.Repository;

/**
 * HospitalDAO继承基类
 */
@Repository
public interface HospitalDAO extends MyBatisBaseDao<Hospital, Integer> {
}