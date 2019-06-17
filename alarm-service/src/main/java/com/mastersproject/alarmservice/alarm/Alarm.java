package com.mastersproject.alarmservice.alarm;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Alarm {

    @Id
    @GeneratedValue
    private long id;
    private long facilityId;
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
