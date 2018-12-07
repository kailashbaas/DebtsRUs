package com.cs174a.kbaas;

import java.sql.Timestamp;

public class Check {
    private Account src;
    private String check_num;
    private Timestamp datetime;
    private String memo;
    private double money;
    private Customer initiator;

    public Account getSrc() {
        return src;
    }

    public void setSrc(Account src) {
        this.src = src;
    }

    public String getCheck_num() {
        return check_num;
    }

    public void setCheck_num(String check_num) {
        this.check_num = check_num;
    }

    public Timestamp getDatetime() {
        return datetime;
    }

    public void setDatetime(Timestamp datetime) {
        this.datetime = datetime;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public Customer getInitiator() {
        return initiator;
    }

    public void setInitiator(Customer initiator) {
        this.initiator = initiator;
    }
}
