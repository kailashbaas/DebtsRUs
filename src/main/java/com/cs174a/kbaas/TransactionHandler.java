package com.cs174a.kbaas;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class TransactionHandler {

    private DatabaseAccessor db;
    private Account external_acct;

    public TransactionHandler() {
        this.db = new DatabaseAccessor();
        this.external_acct = Account.instantiateATMAcct();
    }

    public boolean deposit(double amount, Account acct, Customer initiator, Timestamp time) {
        if (amount <= 0 || !acct.isOpen() || acct.getType().contains("Pocket")) {
            return false;
        }

        Transaction t = new Transaction();
        t.setDatetime(time);
        t.setDest(acct);
        t.setSrc(external_acct);
        t.setMoney(amount);
        t.setType("Deposit");
        t.setInitiator(initiator);
        db.insert_transaction(t);

        System.out.println("predeposit");
        acct.deposit(amount);
        System.out.println("amount: " + amount);
        System.out.println("postdeposit");

        return db.update_acct(acct);
    }

    public boolean top_up(double amount, Account acct, Customer initiator, Timestamp time) {
        boolean first_transac = first_transaction(acct, time);
        if (amount <= 0 || (first_transac && amount <= 5) || !acct.isOpen() || !acct.getType().contains("Pocket") || amount > acct.getLinked_acct().getBalance()) {
            return false;
        }

        if (first_transac) {
            acct.deposit(-5);
        }

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
    public boolean withdraw(double amount, Account acct, Customer initiator, Timestamp time) {
        if (amount <= 0 || amount > acct.getBalance() || !acct.isOpen() || acct.getType().contains("Pocket")) {
            return false;
        }
        acct.deposit(-1 * amount);
        if (acct.getBalance() <= 0.01) {
            acct.setOpen(false);
        }

        Transaction t = new Transaction();
        t.setDatetime(time);
        t.setSrc(acct);
        t.setDest(external_acct);
        t.setMoney(amount);
        t.setType("Withdrawal");
        t.setInitiator(initiator);
        System.out.println("pre insert transac");
        db.insert_transaction(t);
        System.out.println("post insert transac");

        return db.update_acct(acct);
    }

    public boolean purchase(double amount, Account acct, Customer initiator, Timestamp time) {
        boolean first_transac = first_transaction(acct, time);
        if (amount <= 0 || (first_transac && amount <= 5) ||  amount > acct.getBalance() || !acct.isOpen() || !acct.getType().contains("Pocket")) {
            return false;
        }

        if (first_transac) {
            acct.deposit(-5);
        }

        acct.deposit(-1 * amount);
        if (acct.getBalance() <= 0.01) {
            acct.setOpen(false);
        }

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

    public boolean transfer(double amount, Account src, Account dest, Customer initiator, Timestamp time) {
        if (amount > 2000 || amount <= 0 || amount > src.getBalance()
                || src.getType().contains("Pocket") || dest.getType().equals("Pocket")) {
            return false;
        }

        boolean commonOwners = src.getPrimary_owner() == dest.getPrimary_owner();
        boolean src_owner = false, dest_owner = false;

        if (!commonOwners) {
            String customer_src_id = String.valueOf(initiator.getTaxId());
            String src_id = String.valueOf(src.getAccountid());
            String dest_id = String.valueOf(dest.getAccountid());
            String query = "SELECT * FROM Owners WHERE ownerid = " + customer_src_id;
            ArrayList<String> src_owners = db.query_owners(query);

            for (int i = 0; i < src_owners.size(); i++) {
                String owner_tuple = src_owners.get(i);
                if (src_id.equals(owner_tuple.substring(0, owner_tuple.indexOf("|")))) {
                    if (!src_owner) {
                        src_owner = true;
                    }
                }
                else if (dest_id.equals(owner_tuple.substring(0, owner_tuple.indexOf("|")))) {
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

    public boolean collect(double amount, Account acct, Customer initiator, Timestamp time) {
        boolean first_transac = first_transaction(acct, time);
        if (amount <= 0 || (first_transac && amount <= 5) || amount > acct.getBalance() || !acct.getType().contains("Pocket")) {
            return false;
        }

        if (first_transac) {
            acct.deposit(-5);
        }

        acct.deposit(-1 * amount);
        acct.getLinked_acct().deposit(amount * 0.97);
        if (acct.getBalance() <= 0.01) {
            acct.setOpen(false);
        }

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

    public boolean pay_friend(double amount, Account src, Account dest, Customer initiator, Timestamp time) {
        boolean first_transac = first_transaction(src, time);
        if (amount <= 0 || (first_transac && amount <= 5) || amount > src.getBalance() || !src.getType().contains("Pocket")
                || !dest.getType().contains("Pocket")) {
            return false;
        }

        if (first_transac) {
            src.deposit(-5);
        }

        src.deposit(-1 * amount);
        dest.deposit(amount);
        if (src.getBalance() <= 0.01) {
            src.setOpen(false);
        }

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

    public boolean wire(double amount, Account src, Account dest, Customer initiator, Timestamp time) {
        if (amount <= 0 || amount > src.getBalance()
                || src.getType().contains("Pocket") || dest.getType().equals("Pocket")) {
            return false;
        }

        boolean valid_owner = (src.getPrimary_owner() == initiator);

        String src_id = String.valueOf(src.getAccountid());
        String query = "SELECT * FROM Owners WHERE accountid = " + src_id;
        ArrayList<String> src_owners = db.query_owners(query);

        for (int i = 0; i < src_owners.size(); i++) {
            String owner_tuple = src_owners.get(i);
            if (String.valueOf(initiator.getTaxId()).equals(owner_tuple.substring(owner_tuple.indexOf("|") + 1))) {
                valid_owner = true;
                break;
            }
        }

        if (valid_owner) {
            src.deposit(-1 * amount);
            dest.deposit(amount * 0.98);
            if (src.getBalance() <= 0.01) {
                src.setOpen(false);
            }

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

    public boolean write_check(double amount, Account acct, String memo, Timestamp time) {
        if (amount <= 0 || amount > acct.getBalance() || !acct.getType().contains("Checking")) {
            return false;
        }

        acct.deposit(-1 * amount);
        if (acct.getBalance() <= 0.01) {
            acct.setOpen(false);
        }

        String check_num = String.valueOf(acct.getAccountid()).concat(String.valueOf(time));
        Check check = new Check();
        check.setSrc(acct);
        check.setCheck_num(Integer.parseInt(check_num));
        check.setMoney(amount);
        check.setDatetime(time);
        check.setMemo(memo);

        return db.insert_check(check);
    }

    public boolean accrue_interest(Account acct, Timestamp time) {
        double avg_daily_balance = generate_avg_daily_balance(acct, time);
        acct.deposit(avg_daily_balance * acct.getInterest_rate() / 12);
        acct.setInterest_added(true);
        return db.update_acct(acct);
    }

    private double generate_avg_daily_balance(Account acct, Timestamp time) {
        Timestamp last_month = new Timestamp(time.getTime() - (30 * 24 * 60 * 60 * 1000));
        String acctid = String.valueOf(acct.getAccountid());

        String transactions_sql = "SELECT * FROM Transactions WHERE datetime >= TO_TIMESTAMP('" + last_month.toString()
                + "', YYYY-MM-DD HH24:MI:SS.FF9) AND (source = " + acctid + "OR destination = " + acctid + ")"
                + " ORDER BY datetime ASC";
        String checks_sql = "SELECT * FROM Checks WHERE source = " + acctid + " ORDER BY datetime ASC";

        ArrayList<Transaction> transactions = db.query_transaction(transactions_sql);
        ArrayList<Check> checks = db.query_check(checks_sql);
        ArrayList<Map.Entry<Double, LocalDate>> checks_and_transactions = new ArrayList<>();

        double transaction_total = 0;

        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            LocalDate new_time = t.getDatetime().toLocalDateTime().toLocalDate();
            double amt = 0;
            if (acct.getAccountid() == t.getSrc().getAccountid()) {
                transaction_total -= t.getMoney();
                amt -= t.getMoney();
            }
            else {
                transaction_total += t.getMoney();
                amt += t.getMoney();
            }
            AbstractMap.SimpleEntry<Double, LocalDate> entry = new AbstractMap.SimpleEntry<>(amt, new_time);
            checks_and_transactions.add(entry);
        }

        for (int i = 0; i < checks.size(); i++) {
            Check c = checks.get(i);
            LocalDate new_time = c.getDatetime().toLocalDateTime().toLocalDate();
            transaction_total -= c.getMoney();
            AbstractMap.SimpleEntry<Double, LocalDate> entry = new AbstractMap.SimpleEntry<>(-1 * c.getMoney(), new_time);
            checks_and_transactions.add(entry);
        }

        Collections.sort(checks_and_transactions, new Comparator<Map.Entry<Double, LocalDate>>() {
            @Override
            public int compare(Map.Entry<Double, LocalDate> doubleLocalDateEntry, Map.Entry<Double, LocalDate> t1) {
                return doubleLocalDateEntry.getValue().compareTo(t1.getValue());
            }
        });

        int start = 0;
        checks_and_transactions = condense_checks_and_transactions(checks_and_transactions);
        double curr_balance = acct.getBalance() + transaction_total;
        LocalDate old_date = last_month.toLocalDateTime().toLocalDate();
        if (old_date.equals(checks_and_transactions.get(0).getValue())) {
            curr_balance += checks_and_transactions.get(0).getKey();
            start = 1;
        }
        int num_days;
        double total = 0;

        for (int i = start; i < checks_and_transactions.size(); i++) {
            double amount = checks_and_transactions.get(i).getKey();
            LocalDate date = checks_and_transactions.get(i).getValue();

            num_days = date.getDayOfYear() - old_date.getDayOfYear();
            total += (curr_balance * num_days  / 30);
            curr_balance += amount;

            old_date = date;
        }

        if (!old_date.equals(time.toLocalDateTime().toLocalDate())) {
            int difference = time.toLocalDateTime().getDayOfYear() - old_date.getDayOfYear();
            total += (curr_balance * difference / 30);
        }

        return total;
    }

    private ArrayList<Map.Entry<Double, LocalDate>> condense_checks_and_transactions(ArrayList<Map.Entry<Double, LocalDate>> list) {
        double total = list.get(0).getKey();
        LocalDate old_date = list.get(0).getValue();
        ArrayList<Map.Entry<Double, LocalDate>> result = new ArrayList<>();

        for (int i = 1; i < list.size(); i++) {
            double amount = list.get(i).getKey();
            LocalDate date = list.get(i).getValue();
            if (date.equals(old_date)) {
                total += amount;
            }
            else {
                result.add(new AbstractMap.SimpleEntry<>(total, old_date));
                old_date = date;
                total = amount;
            }
        }

        return result;
    }

    public Account getExternal_acct() {
        return external_acct;
    }

    private boolean first_transaction(Account acct, Timestamp time) {
        Timestamp start_of_month = Timestamp.valueOf(time.toLocalDateTime().toLocalDate().withDayOfMonth(1).atTime(0, 0));
        System.out.println("start of month " + start_of_month.toString());

        String sql = "SELECT COUNT(*) AS CC FROM Transactions WHERE source = " + String.valueOf(acct.getAccountid())
                + " OR (destination = " + String.valueOf(acct.getAccountid()) + " AND type = 'Top-Up')"
                + " AND datetime >= TO_TIMESTAMP('" + start_of_month.toString() + "', 'YYYY-MM-DD HH24:MI:SS.FF9')";
        return (db.aggregate_query(sql, "CC") == 0);
    }
}
