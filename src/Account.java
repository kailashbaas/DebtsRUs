public interface Account
{
    private int accountid;
    private boolean open;
    private String branch;
    private double interest_rate;
    private double balance;
    private double avg_daily_balance;
    private Customer primary_owner;
    private String type;


    public int getAccountid();
    public boolean getStatus();
    public String getBranch();
    public double getInterest_rate();
    public double getBalance();
    public double getAvg_daily_balance();
    public Customer getPrimary_owner();
    public String getType();

    public void setAccountid(int accountid);
    public void setstatus(boolean status);
    public void setBranch(String branch);
    public void setInterest_rate(double interest_rate);
    public void setBalance(double balance);
    public void setAvg_daily_balance(double balance);
    public void setPrimary_owner(Customer primary_owner);
    public void setType(String type);
}