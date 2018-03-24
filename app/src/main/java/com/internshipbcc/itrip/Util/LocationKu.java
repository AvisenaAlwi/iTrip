package com.internshipbcc.itrip.Util;

/**
 * Created by Sena on 24/03/2018.
 */

public class LocationKu {

    public String id;
    public String name;
    public double distance;
    public int htm, totalKebutuhan;

    public LocationKu(String id, String name, double distance, int htm, int totalKebutuhan) {
        this.id = id;
        this.name = name;
        this.distance = distance;
        this.htm = htm;
        this.totalKebutuhan = totalKebutuhan;
    }
}
