package com.cs174a.kbaas;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseAccessor {
    private static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String DB_URL = "jdbc:oracle:thin:@cloud-34-133.eci.ucsb.edu:1521:XE";
    private static final String USERNAME = "kailashbaas";
    private static final String PASSWORD = "6551261";

    public HashMap<Integer, Account> query_acct(String query) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        HashMap<Integer, Account> accts = new HashMap<Integer, Account>();

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            stmt = conn.createStatement();

            rs = stmt.executeQuery(query);
            while (rs.next()) {
                Account acct = Account.instantiateAcct(rs.getString("type"));
                acct.setAccountid(rs.getInt("accountid"));
                acct.setOpen(rs.getString("open").equals("Y"));
                acct.setBranch(rs.getString("branch"));
                acct.setInterest_rate(rs.getDouble("interest_rate"));
                acct.setInterest_added(rs.getString("interest_added").equals("Y"));
                acct.setBalance(rs.getDouble("balance"));
                acct.setType(rs.getString("type"));
                if (acct.getType().contains("Pocket")) {
                    String sql = "SELECT * FROM Accounts WHERE accountid = " + String.valueOf(rs.getInt("linked_account"));
                    acct.setLinked_acct(this.query_acct(sql).get(rs.getInt("linked_account")));
                }
                String primary_owner_sql = "SELECT * FROM Customers WHERE tax_id = " + String.valueOf(rs.getInt("primary_owner"));
                acct.setPrimary_owner(this.query_customer(primary_owner_sql, "tax_id").get(rs.getInt("primary_owner")));
                accts.put(acct.accountid, acct);
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    Thread.sleep(1500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return accts;
    }

    // Check.src will be null for all checks, will require additional processing to set
    // tihs field
    public ArrayList<Check> query_check(String query) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        ArrayList<Check> checks = new ArrayList<Check>();

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            stmt = conn.createStatement();

            rs = stmt.executeQuery(query);

            while (rs.next()) {
                Check c = new Check();
                String src_sql = "SELECT * FROM Accounts WHERE accountid = " + String.valueOf(rs.getInt("source"));
                HashMap<Integer, Account> src = this.query_acct(src_sql);
                c.setSrc(src.get(rs.getInt("source")));
                c.setCheck_num(rs.getInt("check_num"));
                c.setDatetime(rs.getTimestamp("datetime"));
                c.setMoney(rs.getDouble("money"));
                c.setMemo(rs.getString("memo"));
                checks.add(c);
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    Thread.sleep(1500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return checks;
    }

    public HashMap<Integer, Customer> query_customer(String query, String key) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        HashMap<Integer, Customer> customers = new HashMap<Integer, Customer>();
        boolean tax_id_key = key.equals("tax_id");

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            stmt = conn.createStatement();

            rs = stmt.executeQuery(query);

            while (rs.next()) {
                String address = rs.getString("address");
                int pin = rs.getInt("pin");
                String name = rs.getString("name");
                int taxId = rs.getInt("tax_id");
                Customer c = new Customer(taxId, pin, name, address);
                if (tax_id_key) {
                    customers.put(taxId, c);
                } else {
                    customers.put(pin, c);
                }
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    Thread.sleep(1500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return customers;
    }

    public ArrayList<Transaction> query_transaction(String query) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        ArrayList<Transaction> transactions = null;

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            stmt = conn.createStatement();

            rs = stmt.executeQuery(query);

            while (rs.next()) {
                Transaction t = new Transaction();
                String src_sql = "SELECT * FROM Accounts WHERE accountid = " + rs.getInt("source");
                HashMap<Integer, Account> src = this.query_acct(src_sql);
                t.setSrc(src.get(rs.getInt("source")));
                String dest_sql = "SELECT * FROM Accounts WHERE accountid = " + rs.getInt("destination");
                HashMap<Integer, Account> dest = this.query_acct(dest_sql);
                t.setDest(src.get(rs.getInt("destination")));
                t.setDatetime(rs.getTimestamp("datetime"));
                t.setMoney(rs.getDouble("money"));
                t.setType(rs.getString("type"));
                transactions.add(t);
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    Thread.sleep(1500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return transactions;
    }

    // Returns an arraylist of strings containing the accountids and ownerids of the owners
    // in the following format:
    // accountid|ownerid
    // If either value is negative there was an error
    public ArrayList<String> query_owners(String query) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        ArrayList<String> owners = new ArrayList<>();

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            stmt = conn.createStatement();

            rs = stmt.executeQuery(query);

            while (rs.next()) {
                int accountid = rs.getInt("accountid");
                int ownerid = rs.getInt("ownerid");
                String owner_tuple = String.valueOf(accountid).concat("|").concat(String.valueOf(ownerid));
                owners.add(owner_tuple);
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    Thread.sleep(1500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return owners;
    }

    public double aggregate_query(String query, String name) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        double result = -1;

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            stmt = conn.createStatement();

            rs = stmt.executeQuery(query);

            while (rs.next()) {
                result = rs.getDouble(name);
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    Thread.sleep(1500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public void insert_new_acct(Account acct, ArrayList<Customer> owners, ArrayList<Customer> new_owners) {
        int accountid = acct.getAccountid();
        System.out.println("here1");
        for (int i = 0; i < new_owners.size(); i++) {
            insert_customer(new_owners.get(i));
        }
        System.out.println("here2");
        insert_acct(acct);
        System.out.println("here3");
        for (int i = 0; i < owners.size(); i++) {
            insert_owner(owners.get(i), accountid);
        }
        System.out.println("here4");
    }

    private void insert_acct(Account acct) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "INSERT INTO Accounts(accountid, open, branch, interest_rate, interest_added, " 
            + "balance, type, primary_owner, linked_account) " +
                "VALUES(?,?,?,?,?,?,?,?,?)";

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            pstmt = conn.prepareStatement(sql);
            String open = "Y";
            if (!acct.isOpen()) {
                open = "N";
            }
            String interest_added = "N";
            if (acct.getInterest_added()) {
                interest_added = "Y";
            }
            pstmt.setObject(1, acct.getAccountid());
            pstmt.setObject(2, open);
            pstmt.setObject(3, acct.getBranch());
            pstmt.setObject(4, acct.getInterest_rate());
            pstmt.setObject(5, interest_added);
            pstmt.setObject(6, acct.getBalance());
            pstmt.setObject(7, acct.getType());
            pstmt.setObject(8, acct.getPrimary_owner().getTaxId());
            if (acct.getLinked_acct() != null) {
                pstmt.setObject(9, acct.getLinked_acct().getAccountid());
                System.out.println("set linked acct" + acct.getLinked_acct().getAccountid());
            } else {
                pstmt.setObject(9, null);
            }
            System.out.println("preupdate");
            pstmt.executeUpdate();
            System.out.println("postupdate");
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    Thread.sleep(1500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void insert_customer(Customer c) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String customers_sql = "INSERT INTO Customers(tax_id, pin, name, address) VALUES(?,?,?,?)";

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            pstmt = conn.prepareStatement(customers_sql);
            pstmt.setObject(1, c.getTaxId());
            pstmt.setObject(2, c.getPin());
            pstmt.setObject(3, c.getName());
            pstmt.setObject(4, c.getAddress());
            pstmt.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    Thread.sleep(1500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void insert_owner(Customer c, int accountid) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String owners_sql = "INSERT INTO Owners(accountid, ownerid) VALUES(?,?)";

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            pstmt = conn.prepareStatement(owners_sql);
            pstmt.setObject(1, accountid);
            pstmt.setObject(2, c.getTaxId());
            pstmt.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    Thread.sleep(1500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void insert_transaction(Transaction t) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String transaction_sql = "INSERT INTO Transactions(source, destination, datetime, type, money, initiator) VALUES(?,?,?,?,?,?)";

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            pstmt = conn.prepareStatement(transaction_sql);
            pstmt.setObject(1, t.getSrc().getAccountid());
            pstmt.setObject(2, t.getDest().getAccountid());
            pstmt.setObject(3, t.getDatetime());
            pstmt.setObject(4, t.getType());
            pstmt.setObject(5, t.getMoney());
            pstmt.setObject(6, t.getInitiator().getTaxId());
            System.out.println("preinsert");
            pstmt.executeUpdate();
            System.out.println("postinsert");
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    Thread.sleep(1500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean insert_check(Check c) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "INSERT INTO Checks(source, check_num, datetime, memo, money) VALUES(?,?,?,?,?)";

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            pstmt = conn.prepareStatement(sql);
            pstmt.setObject(1, c.getSrc().getAccountid());
            pstmt.setObject(2, c.getCheck_num());
            pstmt.setObject(3, c.getDatetime());
            pstmt.setObject(4, c.getMemo());
            pstmt.setObject(5, c.getMoney());
            pstmt.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    Thread.sleep(1500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean update_acct(Account acct) {
        if (acct.getBalance() < 0) {
            return false;
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "UPDATE Accounts SET open = ?, branch = ?, interest_rate = ?, " +
                "interest_added = ?, balance = ?, " +
                "primary_owner = ?, linked_account = ? WHERE accountid = ?";
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            pstmt = conn.prepareStatement(sql);
            String open = "Y";
            if (!acct.isOpen()) {
                open = "N";
            }
            String interest_added = "N";
            if (acct.getInterest_added()) {
                interest_added = "Y";
            }
            pstmt.setObject(1, open);
            pstmt.setObject(2, acct.getBranch());
            pstmt.setObject(3, acct.getInterest_rate());
            pstmt.setObject(4, interest_added);
            pstmt.setObject(5, acct.getBalance());
            System.out.println("in update_acct: " + (acct.getPrimary_owner() == null));
            pstmt.setObject(6, acct.getPrimary_owner().getTaxId());
            if (acct.getLinked_acct() != null) {
                System.out.println("set linked acct");
                pstmt.setObject(7, acct.getLinked_acct().getAccountid());
            } else {
                pstmt.setObject(7, null);
            }
            pstmt.setObject(8, acct.getAccountid());
            System.out.println("acct balance in dbaccessor " + acct.getBalance());
            pstmt.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    Thread.sleep(1500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void generic_update(String sql) {
        Connection conn = null;
        Statement stmt = null;

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            stmt = conn.createStatement();

            stmt.executeUpdate(sql);
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    Thread.sleep(1500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean update_customer(Customer c) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "UPDATE Customers SET pin = ?, name = ?, address = ? WHERE tax_id = ?";
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            pstmt = conn.prepareStatement(sql);
            pstmt.setObject(1, c.getPin());
            pstmt.setObject(2, c.getName());
            pstmt.setObject(3, c.getAddress());
            pstmt.setObject(4, c.getTaxId());
            pstmt.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    Thread.sleep(1500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean verifyPin(int pin) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT pin FROM Customers WHERE pin = ?";
        boolean valid_pin = false;

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            pstmt = conn.prepareStatement(sql);
            pstmt.setObject(1, pin);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                valid_pin = true;
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    Thread.sleep(1500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return valid_pin;
    }

    public boolean delete_acct(Account acct) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "DELETE FROM Accounts WHERE accountid = ?";
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            pstmt = conn.prepareStatement(sql);
            pstmt.setObject(1, acct.getAccountid());
            pstmt.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    Thread.sleep(1500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String delete_customers = "DELETE FROM Customers C WHERE C.tax_id NOT IN (" +
            "SELECT DISTINCT O.ownerid FROM Owners O)";
        generic_update(delete_customers);
        return true;
    }

    public boolean delete_customer(Customer c) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "DELETE FROM Customers WHERE tax_id = ?";
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            pstmt = conn.prepareStatement(sql);
            pstmt.setObject(1, c.getTaxId());
            pstmt.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    Thread.sleep(1500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    // this method deletes all transactions from the database
    public void delete_transactions(Timestamp time) {
        Connection conn = null;
        Statement stmt = null;
        Timestamp old_time = Timestamp.valueOf(time.toLocalDateTime().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
        String sql = "DELETE FROM Transactions T WHERE datetime < ";
        sql += "TO_TIMESTAMP('" + old_time.toString() + "', 'YYYY-MM-DD HH24:MI:SS.FF9')";
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            stmt = conn.createStatement();

            stmt.executeUpdate(sql);
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    Thread.sleep(1500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
