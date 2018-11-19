public abstract class Account
{
    protected int accountid;
    protected boolean open;
    protected String branch;
    protected double interest_rate;
    protected double balance;
    protected double avg_daily_balance;
    protected Customer primary_owner;
    protected String type;


    public abstract int getAccountid();
    public abstract boolean isOpen();
    public abstract String getBranch();
    public abstract double getInterest_rate();
    public abstract double getBalance();
    public abstract double getAvg_daily_balance();
    public abstract Customer getPrimary_owner();
    public abstract String getType();

    public abstract void setAccountid(int accountid);
    public abstract void setOpen(boolean open);
    public abstract void setBranch(String branch);
    public abstract void setInterest_rate(double interest_rate);
    public abstract void setBalance(double balance);
    public abstract void setAvg_daily_balance(double balance);
    public abstract void setPrimary_owner(Customer primary_owner);
}
