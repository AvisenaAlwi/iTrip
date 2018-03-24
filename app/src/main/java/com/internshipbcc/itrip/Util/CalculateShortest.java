package com.internshipbcc.itrip.Util;

import java.util.ArrayList;

/**
 * Created by Sena on 24/03/2018.
 */

public class CalculateShortest {
    public static ArrayList<LocationKu> findRecommendation(ArrayList<LocationKu> locationKu, int budget, boolean transport) {
        ArrayList<LocationKu> rec = new ArrayList<>();
        for (LocationKu loc : locationKu) {
            int total = 0;
//            total+=(int)loc.distance*2.0/30*10000;
            if (transport)
                total += 2 * (loc.distance * 4000);
            total += loc.htm;
            System.out.println(total + " " + 0.8 * budget);
            loc.totalKebutuhan = total;
            if (total <= 0.8 * budget)
                rec.add(loc);
        }
        return rec;
    }
}


