package com.mastersproject.alarmservice.entity;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    Alarm findBySensorId(long sensorId);

    @Query("SELECT a FROM Alarm a WHERE a.active = :active AND a.published = :published")
    List<Alarm> findActiveNotPublished(@Param("active") boolean active, @Param("published") boolean published);

}
