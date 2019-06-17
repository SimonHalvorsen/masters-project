package com.mastersproject.alarmservice.alarm;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;


@Repository
@Transactional
public class AlarmDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public long insert(Alarm alarm){
        entityManager.persist(alarm);
        return alarm.getId();
    }
}
