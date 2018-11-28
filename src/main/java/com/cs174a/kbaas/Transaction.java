package com.cs174a.kbaas;

import java.sql.Timestamp;

public class Transaction {
    private Account src;
    private Account dest;
    private Timestamp datetime;
    private String type;
    private double money;

    public Account getSrc() {
        return src;
    }

    public void setSrc(Account src) {
        this.src = src;
    }

    public Account getDest() {
        return dest;
    }

    public void setDest(Account dest) {
        this.dest = dest;
    }

    public Timestamp getDatetime() {
        return datetime;
    }

    public void setDatetime(Timestamp datetime) {
        this.datetime = datetime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }
}
