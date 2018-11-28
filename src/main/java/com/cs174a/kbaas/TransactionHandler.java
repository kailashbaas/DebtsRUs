package com.cs174a.kbaas;

import java.sql.Timestamp;
import java.util.ArrayList;

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
        db.insert_transaction(t, initiator.getTaxId());

        acct.deposit(amount);

        return db.update_acct(acct);
    }

    public boolean top_up(double amount, Account acct, Customer initiator) {
        if (amount <= 0 || !acct.isOpen() || !acct.getType().equals("Pocket")) {
            return false;
        }

        Timestamp time = new Timestamp(System.currentTimeMillis());
        Transaction t = new Transaction();
        t.setDatetime(time);
        t.setSrc(acct.getLinked_acct());
        t.setDest(acct);
        t.setMoney(amount);
        t.setType("Top-Up");
        db.insert_transaction(t, initiator.getTaxId());

        acct.deposit(amount);
        return db.update_acct(acct);
    }

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
        db.insert_transaction(t, initiator.getTaxId());

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
        db.insert_transaction(t, initiator.getTaxId());

        return db.update_acct(acct);
    }

    public boolean transfer(double amount, Account src, Account dest, Customer initiator) {
        if (amount > 2000 || amount <= 0 || amount > src.getBalance()
                || src.getType().equals("Pocket") || dest.getType().equals("Pocket")) {
            return false;
        }

        boolean commonOwners = src.getPrimary_owner() == dest.getPrimary_owner();
        boolean intermediate = false;

        if (!commonOwners) {
            String customr_src_id = String.valueOf(initiator.getTaxId());
            String src_id = String.valueOf(src.getAccountid());
            String dest_id = String.valueOf(dest.getAccountid());
            String query = "SELECT * FROM Owners WHERE ownerid = " + src_id;
            ArrayList<String> src_owners = db.query_owners(query);

            for (int i = 0; i < src_owners.size(); i++) {
                if (src_id.equals(src_owners.get(i).substring(0, src_owners.indexOf("|")))
                    || dest_id.equals(src_owners.get(i).substring(0, src_owners.indexOf("|")))) {
                    if (!intermediate) {
                        intermediate = true;
                    }
                    else {
                        commonOwners = true;
                        break;
                    }
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
            db.insert_transaction(t, initiator.getTaxId());

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
        db.insert_transaction(t, initiator.getTaxId());
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
        db.insert_transaction(t, initiator.getTaxId());

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
            db.insert_transaction(t, initiator.getTaxId());

            return db.update_acct(src) && db.update_acct(dest);
        }

        return false;
    }

    public boolean write_check(double amount, Account acct, Customer c) {
        if (amount <= 0 || amount > acct.getBalance() || !acct.getType().equals("Checking")) {
            return false;
        }

        acct.deposit(-1 * amount);
        if (acct.getBalance() <= 0.01) {
            acct.setOpen(false);
        }

        Timestamp current_time = new Timestamp(System.currentTimeMillis());
        String check_num = String.valueOf(c.getTaxId()).concat(String.valueOf(current_time));
        Check check = new Check();
        check.setSrc(acct);
        check.setCheck_num(Integer.parseInt(check_num));
        check.setMoney(amount);
        check.setDatetime(current_time);
        check.setMemo(c.getName().concat("|"));

        return db.insert_check(check);
    }

    public boolean accrue_interest(Account acct) {
        acct.deposit(acct.getBalance() * acct.getInterest_rate());
        acct.setInterest_added(true);
        return db.update_acct(acct);
    }
}