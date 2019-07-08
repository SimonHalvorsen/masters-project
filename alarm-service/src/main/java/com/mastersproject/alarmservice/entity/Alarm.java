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
    private double timestamp;

    @Column(name = "published")
    private boolean published;


    public Alarm() {
    }

    public Alarm(long facilityId, long sensorId, boolean active, double timestamp, boolean published) {
        this.facilityId = facilityId;
        this.sensorId = sensorId;
        this.active = active;
        this.timestamp = timestamp;
        this.published = published;
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

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
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
