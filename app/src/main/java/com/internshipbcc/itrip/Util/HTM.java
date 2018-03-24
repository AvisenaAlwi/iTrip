package com.internshipbcc.itrip.Util;

import java.util.Calendar;

/**
 * Created by Sena on 20/03/2018.
 */

public class HTM {
    public static int getHarga(String htm) {
        int hari = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        if (hari == 0 || hari == 6) { //weekend
            return Integer.valueOf(htm.split(",")[1]);
        } else {
            return Integer.valueOf(htm.split(",")[0]);
        }
    }

    public static String getWeekDay(String htm) {
        return "Rp." + htm.split(",")[0];
    }

    public static String getWeekEnd(String htm) {
        return "Rp." + htm.split(",")[1];
    }
}
