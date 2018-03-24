package com.internshipbcc.itrip.Util;

/**
 * Created by Sena on 24/03/2018.
 */

public class Reservasi {
    public String id;
    public String userId;
    public String titleWisata;
    public String idWisata;
    public String status;

    public Reservasi(String id, String userId, String titleWisata, String idWisata, String status) {
        this.id = id;
        this.userId = userId;
        this.titleWisata = titleWisata;
        this.idWisata = idWisata;
        this.status = status;
    }
}
