package com.cs174a.kbaas;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseAccessor {
    private static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String DB_URL = "jdbc:oracle:thin:@cloud-34-133.eci.ucsb.edu:1521:XE";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    // Account.linked_account and Account.primary_owner will be null for all
    // accounts, will require additional processing to set these fields
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
                acct.accountid = rs.getInt("accountid");
                acct.open = rs.getBoolean("open");
                acct.branch = rs.getString("branch");
                acct.interest_rate = rs.getDouble("interest_rate");
                acct.interest_added = rs.getBoolean("interest_added");
                acct.balance = rs.getDouble("balance");
                acct.avg_daily_balance = rs.getDouble("avg_daily_balance");
                acct.type = rs.getString("type");
                acct.linked_acct = null;
                accts.put(acct.accountid, acct);
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
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
                c.setSrc(null);
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
                if (stmt != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
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
                if (stmt != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return customers;
    }

    // Transaction.src and Transaction.dst will be null for all transactions, will
    // require additional processing to set these fields
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
                if (stmt != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
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
        ArrayList<String> owners = null;

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
                if (stmt != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return owners;
    }

    public void insert_new_acct(Account acct, ArrayList<Customer> owners) {
        int accountid = acct.getAccountid();
        insert_acct(acct);
        for (int i = 0; i < owners.size(); i++) {
            insert_customer(owners.get(i), accountid);
        }
    }

    private void insert_acct(Account acct) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "INSERT INTO Accounts(accountid, open, branch, interest_rate, interest_added, " +
                "balance, avg_daily_balance, primary_owner, type, linked_account " +
                "VALUES(?,?,?,?,?,?,?,?,?,?)";

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, acct.getAccountid());
            pstmt.setBoolean(2, acct.isOpen());
            pstmt.setString(3, acct.getBranch());
            pstmt.setDouble(4, acct.getInterest_rate());
            pstmt.setBoolean(5, acct.getInterest_added());
            pstmt.setDouble(6, acct.getBalance());
            pstmt.setDouble(7, acct.getAvg_daily_balance());
            pstmt.setInt(8, acct.getPrimary_owner().getTaxId());
            pstmt.setString(9, acct.getType());
            if (acct.linked_acct == null) {
                pstmt.setObject(10, null);
            } else {
                pstmt.setObject(10, acct.getLinked_acct().getAccountid());
            }
            pstmt.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    // This method inserts into both Customers and Owners, as each customer needs to own an account
    // to be in the database
    private void insert_customer(Customer c, int accountid) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String customers_sql = "INSERT INTO Customers(tax_id, pin, name, address) VALUES(?,?,?,?)";
        String owners_sql = "INSERT INTO Owners(accountid, tax_id) VALUES(?, ?)";

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            pstmt = conn.prepareStatement(customers_sql);
            pstmt.setObject(1, c.getTaxId());
            pstmt.setObject(2, c.getPin());
            pstmt.setObject(3, c.getName());
            pstmt.setObject(4, c.getAddress());
            pstmt.executeUpdate();
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
                if (pstmt != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public void insert_transaction(Transaction t, int initiator_id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String initiator_sql = "INSERT INTO Initiators(source, destination, datetime, tax_id) VALUES(?,?,?,?)";
        String transaction_sql = "INSERT INTO Owners(source, destination, datetime, type, money) VALUES(?,?,?,?,?)";

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            pstmt = conn.prepareStatement(initiator_sql);
            pstmt.setObject(1, t.getSrc().getAccountid());
            pstmt.setObject(2, t.getDest().getAccountid());
            pstmt.setObject(3, t.getDatetime());
            pstmt.setObject(4, initiator_id);
            pstmt.executeUpdate();
            pstmt = conn.prepareStatement(transaction_sql);
            pstmt.setObject(1, t.getSrc().getAccountid());
            pstmt.setObject(2, t.getDest().getAccountid());
            pstmt.setObject(3, t.getDatetime());
            pstmt.setObject(4, t.getType());
            pstmt.setObject(5, t.getMoney());
            pstmt.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
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
                if (pstmt != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return true;
    }

    public boolean update_acct(Account acct) {
        if ((acct.getBalance() < 0) || (acct.getAvg_daily_balance() < 0)) {
            return false;
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "UPDATE Accounts SET open = ?, branch = ?, interest_rate = ?, " +
                "interest_added = ?, balance = ?, avg_daily_balance = ?, " +
                "primary_owner = ?, linked_account = ? WHERE accountid = ?";
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            pstmt = conn.prepareStatement(sql);
            pstmt.setObject(1, acct.isOpen());
            pstmt.setObject(2, acct.getBranch());
            pstmt.setObject(3, acct.getInterest_rate());
            pstmt.setObject(4, acct.getInterest_added());
            pstmt.setObject(5, acct.getBalance());
            pstmt.setObject(6, acct.getAvg_daily_balance());
            pstmt.setObject(7, acct.getPrimary_owner().getTaxId());
            if (acct.getLinked_acct() != null) {
                pstmt.setObject(8, acct.getLinked_acct().getAccountid());
            } else {
                pstmt.setObject(8, null);
            }
            pstmt.setObject(9, acct.getAccountid());
            pstmt.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return true;
    }

    public boolean update_customer(Customer c) {
        // TODO: change where pin is verified
        if (!verifyPin(c.getPin())) {
            return false;
        }
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
                if (pstmt != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
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
                if (pstmt != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
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
                if (pstmt != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
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
                if (pstmt != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return true;
    }
}
