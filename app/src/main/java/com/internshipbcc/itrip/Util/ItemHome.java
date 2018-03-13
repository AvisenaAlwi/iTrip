package com.internshipbcc.itrip.Util;

/**
 * Created by Sena on 12/03/2018.
 */

public class ItemHome {
    public String id;
    public String title, des;
    public String image;
    public boolean isWisata;

    public ItemHome(String id, String title, String des, String image, boolean isWisata) {
        this.id = id;
        this.title = title;
        this.des = des;
        this.image = image;
        this.isWisata = isWisata;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setIsWisata(boolean isWisata) {
        this.isWisata = isWisata;
    }
}
