package com.example.googlemaptest;

public class Contacts {
    private int  spd;
    public String name;
    private Double x, y;
    private Long dev_id;

    public Contacts(String name, Long dev_id, Double x, Double y , int spd){
        this.setName(name);
        this.setDev_id(dev_id);
        this.setX(x);
        this.setY(y);
        this.setSpd(spd);
    }

    public int getSpd() {
        return spd;
    }

    public void setSpd(int spd) {
        this.spd = spd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Long getDev_id() {
        return dev_id;
    }

    public void setDev_id(Long dev_id) {
        this.dev_id = dev_id;
    }
}
