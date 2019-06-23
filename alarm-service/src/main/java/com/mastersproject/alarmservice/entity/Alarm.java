package com.mastersproject.alarmservice.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Alarm {

    @Id
    @GeneratedValue
    @Column(name="id")
    private long id;

    @Column(name="facilityId")
    private long facilityId;

    @Column(name="sensorId")
    private long sensorId;

    public Alarm() {
    }

    public Alarm(long facilityId, long sensorId) {
        this.facilityId = facilityId;
        this.sensorId = sensorId;
    }

    public long getId() {
        return id;
    }

    public long getFacilityId() {
        return facilityId;
    }

    public long getSensorId() {
        return sensorId;
    }

    @Override
    public String toString() {
        return "Alarm{" +
                "id=" + id +
                ", facilityId=" + facilityId +
                ", sensorId=" + sensorId +
                '}';
    }
}
