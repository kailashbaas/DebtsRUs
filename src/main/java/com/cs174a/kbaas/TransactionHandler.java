package com.cs174a.kbaas;

import java.sql.Timestamp;
import java.util.ArrayList;

// TODO: flat $5 fee on first transaction on a pocket account
public class TransactionHandler {

    private DatabaseAccessor db;
    private Account external_acct;

    public TransactionHandler() {
        this.db = new DatabaseAccessor();
        this.external_acct = Account.instantiateATMAcct();
    }

    public boolean deposit(double amount, Account acct, Customer initiator) {
        if (amount <= 0 || !acct.isOpen() || acct.getType().equals("Pocket")) {
            return false;
        }

        Timestamp time = new Timestamp(System.currentTimeMillis());
        Transaction t = new Transaction();
        t.setDatetime(time);
        t.setDest(acct);
        t.setSrc(external_acct);
        t.setMoney(amount);
        t.setType("Deposit");
        t.setInitiator(initiator);
        db.insert_transaction(t);

        acct.deposit(amount);

        return db.update_acct(acct);
    }

    public boolean top_up(double amount, Account acct, Customer initiator) {
        if (amount <= 0 || !acct.isOpen() || !acct.getType().equals("Pocket") || amount > acct.getLinked_acct().getBalance()) {
            return false;
        }

        Timestamp time = new Timestamp(System.currentTimeMillis());
        Transaction t = new Transaction();
        t.setDatetime(time);
        t.setSrc(acct.getLinked_acct());
        t.setDest(acct);
        t.setMoney(amount);
        t.setType("Top-Up");
        t.setInitiator(initiator);
        db.insert_transaction(t);

        acct.deposit(amount);
        acct.getLinked_acct().deposit(-1 * amount);
        return db.update_acct(acct) && db.update_acct(acct.getLinked_acct());
    }

    // amount > 0
    public boolean withdraw(double amount, Account acct, Customer initiator) {
        if (amount <= 0 || amount > acct.getBalance() || !acct.isOpen() || acct.getType().equals("Pocket")) {
            return false;
        }
        acct.deposit(-1 * amount);
        if (acct.getBalance() <= 0.01) {
            acct.setOpen(false);
        }

        Timestamp time = new Timestamp(System.currentTimeMillis());
        Transaction t = new Transaction();
        t.setDatetime(time);
        t.setSrc(acct);
        t.setDest(external_acct);
        t.setMoney(amount);
        t.setType("Withdrawal");
        t.setInitiator(initiator);
        db.insert_transaction(t);

        return db.update_acct(acct);
    }

    public boolean purchase(double amount, Account acct, Customer initiator) {
        if (amount <= 0 || amount > acct.getBalance() || !acct.isOpen() || !acct.getType().equals("Pocket")) {
            return false;
        }
        acct.deposit(-1 * amount);
        if (acct.getBalance() <= 0.01) {
            acct.setOpen(false);
        }

        Timestamp time = new Timestamp(System.currentTimeMillis());
        Transaction t = new Transaction();
        t.setDatetime(time);
        t.setSrc(acct);
        t.setDest(external_acct);
        t.setMoney(amount);
        t.setType("Purchase");
        t.setInitiator(initiator);
        db.insert_transaction(t);

        return db.update_acct(acct);
    }

    public boolean transfer(double amount, Account src, Account dest, Customer initiator) {
        if (amount > 2000 || amount <= 0 || amount > src.getBalance()
                || src.getType().equals("Pocket") || dest.getType().equals("Pocket")) {
            return false;
        }

        boolean commonOwners = src.getPrimary_owner() == dest.getPrimary_owner();
        boolean src_owner = false, dest_owner = false;

        if (!commonOwners) {
            String customr_src_id = String.valueOf(initiator.getTaxId());
            String src_id = String.valueOf(src.getAccountid());
            String dest_id = String.valueOf(dest.getAccountid());
            String query = "SELECT * FROM Owners WHERE ownerid = " + src_id;
            ArrayList<String> src_owners = db.query_owners(query);

            for (int i = 0; i < src_owners.size(); i++) {
                if (src_id.equals(src_owners.get(i).substring(0, src_owners.indexOf("|")))) {
                    if (!src_owner) {
                        src_owner = true;
                    }
                }
                else if (dest_id.equals(src_owners.get(i).substring(0, src_owners.indexOf("|")))) {
                    if (!dest_owner) {
                        dest_owner = true;
                    }
                }
                commonOwners = src_owner && dest_owner;
                if (commonOwners) {
                    break;
                }
            }
        }

        if (commonOwners) {
            src.deposit(-1 * amount);
            dest.deposit(amount);
            if (src.getBalance() <= 0.01) {
                src.setOpen(false);
            }
            Timestamp time = new Timestamp(System.currentTimeMillis());
            Transaction t = new Transaction();
            t.setDatetime(time);
            t.setSrc(src);
            t.setDest(dest);
            t.setMoney(amount);
            t.setType("Transfer");
            t.setInitiator(initiator);
            db.insert_transaction(t);

            return db.update_acct(src) && db.update_acct(dest);
        }
        return false;
    }

    public boolean collect(double amount, Account acct, Customer initiator) {
        if (amount <= 0 || amount > acct.getBalance() || !acct.getType().equals("Pocket")) {
            return false;
        }

        acct.deposit(-1 * amount);
        acct.getLinked_acct().deposit(amount * 0.97);
        if (acct.getBalance() <= 0.01) {
            acct.setOpen(false);
        }

        Timestamp time = new Timestamp(System.currentTimeMillis());
        Transaction t = new Transaction();
        t.setDatetime(time);
        t.setSrc(acct);
        t.setDest(acct.getLinked_acct());
        t.setMoney(amount);
        t.setType("Collect");
        t.setInitiator(initiator);
        db.insert_transaction(t);
        return db.update_acct(acct) && db.update_acct(acct.getLinked_acct());
    }

    public boolean pay_friend(double amount, Account src, Account dest, Customer initiator) {
        if (amount <= 0 || amount > src.getBalance() || !src.getType().equals("Pocket")
                || !dest.getType().equals("Pocket")) {
            return false;
        }

        src.deposit(-1 * amount);
        dest.deposit(amount);
        if (src.getBalance() <= 0.01) {
            src.setOpen(false);
        }

        Timestamp time = new Timestamp(System.currentTimeMillis());
        Transaction t = new Transaction();
        t.setDatetime(time);
        t.setSrc(src);
        t.setDest(dest);
        t.setMoney(amount);
        t.setType("Pay-Friend");
        t.setInitiator(initiator);
        db.insert_transaction(t);

        return db.update_acct(src) && db.update_acct(dest);
    }

    public boolean wire(double amount, Account src, Account dest, Customer initiator) {
        if (amount > 2000 || amount <= 0 || amount > src.getBalance()
                || src.getType().equals("Pocket") || dest.getType().equals("Pocket")) {
            return false;
        }

        boolean valid_owner = (src.getPrimary_owner() == initiator);

        String src_id = String.valueOf(src.getAccountid());
        String query = "SELECT * FROM Owners WHERE accountid = " + src_id;
        ArrayList<String> src_owners = db.query_owners(query);

        for (int i = 0; i < src_owners.size(); i++) {
            if (String.valueOf(initiator.getTaxId()).equals(src_owners.get(i).substring(src_owners.indexOf("|") + 1))) {
                valid_owner = true;
                break;
            }
        }

        if (valid_owner) {
            src.deposit(-1 * amount);
            dest.deposit(amount);
            if (src.getBalance() <= 0.01) {
                src.setOpen(false);
            }

            Timestamp time = new Timestamp(System.currentTimeMillis());
            Transaction t = new Transaction();
            t.setDatetime(time);
            t.setSrc(src);
            t.setDest(dest);
            t.setMoney(amount);
            t.setType("Wire");
            t.setInitiator(initiator);
            db.insert_transaction(t);

            return db.update_acct(src) && db.update_acct(dest);
        }

        return false;
    }

    public boolean write_check(double amount, Account acct, String memo) {
        if (amount <= 0 || amount > acct.getBalance() || !acct.getType().contains("Checking")) {
            return false;
        }

        acct.deposit(-1 * amount);
        if (acct.getBalance() <= 0.01) {
            acct.setOpen(false);
        }

        Timestamp current_time = new Timestamp(System.currentTimeMillis());
        String check_num = String.valueOf(acct.getAccountid()).concat(String.valueOf(current_time));
        Check check = new Check();
        check.setSrc(acct);
        check.setCheck_num(Integer.parseInt(check_num));
        check.setMoney(amount);
        check.setDatetime(current_time);
        check.setMemo(memo);

        return db.insert_check(check);
    }

    public boolean accrue_interest(Account acct) {
        acct.deposit(acct.getAvg_daily_balance() * acct.getInterest_rate() / 12);
        acct.setInterest_added(true);
        return db.update_acct(acct);
    }

    public void generate_monthly_statement(Account acct) {

    }

    public Account getExternal_acct() {
        return external_acct;
    }
}