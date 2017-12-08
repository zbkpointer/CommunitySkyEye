package com.live.communityskyeye;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/12/4.
 */

public class Place extends DataSupport{
    private int id;
    private String place_name;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setPlace_name(String place_name) {
        this.place_name = place_name;
    }

    public String getPlace_name() {
        return place_name;
    }
}
