package com.internshipbcc.itrip.Util;

import java.util.Date;

/**
 * Created by Sena on 20/03/2018.
 */

public class Review {
    public String id, user, body;
    public int star;
    public Date date;

    public Review(String id, String user, String body, int star, Date date) {
        this.id = id;
        this.user = user;
        this.body = body;
        this.star = star;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
