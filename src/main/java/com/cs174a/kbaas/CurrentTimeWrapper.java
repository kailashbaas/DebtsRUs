package com.cs174a.kbaas;

import java.sql.Timestamp;

public class CurrentTimeWrapper {
    public static volatile Timestamp current_time = new Timestamp(System.currentTimeMillis());

    public void updateCurrent_time(Timestamp new_time) {
        current_time = new_time;
    }

    public Timestamp getCurrent_time() {
        return current_time;
    }
}