package com.cs174a.kbaas;

public class CheckingAccount extends Account {
    public int getAccountid() {
        return accountid;
    }

    public void setAccountid(int accountid) {
        this.accountid = accountid;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public double getInterest_rate() {
        return interest_rate;
    }

    public void setInterest_rate(double interest_rate) {
        this.interest_rate = interest_rate;
    }

    public boolean getInterest_added() {
        return this.interest_added;
    }

    public void setInterest_added(boolean interest_added) {
        this.interest_added = interest_added;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void deposit(double amount) {
        this.balance += amount;
    }

    public double getAvg_daily_balance() {
        return avg_daily_balance;
    }

    public void setAvg_daily_balance(double avg_daily_balance) {
        this.avg_daily_balance = avg_daily_balance;
    }

    public Customer getPrimary_owner() {
        return primary_owner;
    }

    public void setPrimary_owner(Customer primary_owner) {
        this.primary_owner = primary_owner;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Account getLinked_acct() {
        return linked_acct;
    }

    public void setLinked_acct(Account linked_acct) {
        this.linked_acct = linked_acct;
    }
}
