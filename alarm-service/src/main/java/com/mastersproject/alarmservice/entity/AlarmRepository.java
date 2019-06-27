package com.mastersproject.alarmservice.entity;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;


public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    Alarm findBySensorId(long sensorId);

    List<Alarm> findAllByActive(boolean active);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Alarm a SET a.active = :active WHERE a.sensorId = :sensorId")
    void updateAlarmStatus(@Param("sensorId") long facilityId, @Param("active") boolean active);

}
