public class SavingsAccount
{
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

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
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
}