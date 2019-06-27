package com.mastersproject.alarmservice.entity;


import javax.persistence.*;

@Entity
@Table(name="Alarm")
public class Alarm {


    @Id
    @GeneratedValue
    @Column(name = "id")
    private long id;

    @Column(name = "facilityId")
    private long facilityId;

    @Column(name = "sensorId")
    private long sensorId;

    @Column(name = "active")
    private boolean active;

    @Column(name = "timestamp")
    private String timestamp;

    public Alarm() {
    }

    public Alarm(long facilityId, long sensorId, boolean active, String timestamp) {
        this.facilityId = facilityId;
        this.sensorId = sensorId;
        this.active = active;
        this.timestamp = timestamp;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Alarm{" +
                "id=" + id +
                ", facilityId=" + facilityId +
                ", sensorId=" + sensorId +
                ", active=" + active +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
