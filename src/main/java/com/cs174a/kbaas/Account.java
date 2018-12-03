package com.cs174a.kbaas;

public abstract class Account {
    protected int accountid;
    protected boolean open;
    protected String branch;
    protected double interest_rate;
    protected boolean interest_added;
    protected double balance;
    protected Customer primary_owner;
    protected String type;
    protected Account linked_acct;

    public abstract int getAccountid();
    public abstract boolean isOpen();
    public abstract String getBranch();
    public abstract double getInterest_rate();
    public abstract boolean getInterest_added();
    public abstract double getBalance();
    public abstract Customer getPrimary_owner();
    public abstract String getType();
    public abstract Account getLinked_acct();
    public abstract void setAccountid(int accountid);
    public abstract void setOpen(boolean open);
    public abstract void setBranch(String branch);
    public abstract void setInterest_rate(double interest_rate);
    public abstract void setInterest_added(boolean interest_added);
    public abstract void setBalance(double balance);
    public abstract void deposit(double balance);
    public abstract void setPrimary_owner(Customer primary_owner);
    public abstract void setType(String type);
    public abstract void setLinked_acct(Account acct);

    public static Account instantiateAcct(String type) {
        Account a = null;
        if (type.contains("Checking")) {
            a = new CheckingAccount(type);
        } else if (type.equals("Savings")) {
            a = new SavingsAccount();
        } else if (type.equals("Pocket")) {
            a = new PocketAccount();
        }
        return a;
    }

    public static Account instantiateATMAcct() {
        Account a = new CheckingAccount("Student");
        a.setOpen(true);
        a.setType("ATM");
        a.setAccountid(0);
        return a;
    }
}
