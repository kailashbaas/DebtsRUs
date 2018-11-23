package com.cs174a.kbaas;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseAccessor
{
    private static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String DB_URL = "jdbc:oracle:thin:@cloud-34-133.eci.ucsb.edu:1521:XE";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    // Account.linked_account and Account.primary_owner will be null for all
    // accounts, will require additional processing to set these fields
    public static HashMap<Integer, Account> query_acct(String query)
    {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        HashMap<Integer, Account> accts = new HashMap<Integer, Account>();

        try
        {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            stmt = conn.createStatement();

            rs = stmt.executeQuery(query);
            while (rs.next())
            {
                Account acct = Account.instantiateAcct(rs.getString("type"));
                acct.accountid = rs.getInt("accountid");
                acct.open = rs.getBoolean("open");
                acct.branch = rs.getString("branch");
                acct.interest_rate = rs.getDouble("interest_rate");
                acct.interest_added = rs.getBoolean("interest_added");
                acct.balance = rs.getDouble("balance");
                acct.avg_daily_balance = rs.getDouble("avg_daily_balance");
                acct.type = rs.getString("type");
                acct.linked_acct= null;
                accts.put(acct.accountid, acct);
            }
        }
        catch (SQLException se)
        {
            se.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (stmt != null)
                {
                    conn.close();
                }
            }
            catch (SQLException se)
            {
            }
            try
            {
                if (conn != null)
                {
                    conn.close();
                }
            }
            catch (SQLException se)
            {
                se.printStackTrace();
            }
        }
        return accts;
    }

    // Check.src will be null for all checks, will require additional processing to set
    // tihs field
    public static ArrayList<Check> query_check(String query)
    {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        ArrayList<Check> checks = new ArrayList<Check>();

        try
        {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            stmt = conn.createStatement();

            rs = stmt.executeQuery(query);

            while (rs.next())
            {
                Check c = new Check();
                c.setSrc(null);
                c.setCheck_num(rs.getInt("check_num"));
                c.setDatetime(rs.getTimestamp("datetime"));
                c.setMoney(rs.getDouble("money"));
                c.setMemo(rs.getString("memo"));
                checks.add(c);
            }
        }
        catch (SQLException se)
        {
            se.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (stmt != null)
                {
                    conn.close();
                }
            }
            catch (SQLException se)
            {
            }
            try
            {
                if (conn != null)
                {
                    conn.close();
                }
            }
            catch (SQLException se)
            {
                se.printStackTrace();
            }
        }
        return checks;
    }

    public static HashMap<Integer, Customer> query_customer(String query)
    {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        HashMap<Integer, Customer> customers = new HashMap<Integer, Customer>();

        try
        {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            stmt = conn.createStatement();

            rs = stmt.executeQuery(query);

            while (rs.next())
            {
                String address = rs.getString("address");
                String pin = rs.getString("pin");
                String name = rs.getString("name");
                int taxId = rs.getInt("tax_id");
                Customer c = new Customer(taxId, pin, name, address);
                customers.put(taxId, c);
            }
        }
        catch (SQLException se)
        {
            se.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (stmt != null)
                {
                    conn.close();
                }
            }
            catch (SQLException se)
            {
            }
            try
            {
                if (conn != null)
                {
                    conn.close();
                }
            }
            catch (SQLException se)
            {
                se.printStackTrace();
            }
        }
        return customers;
    }

    // Transaction.src and Transaction.dst will bu null for all transactions, will
    // require additional processing to set these fields
    public static ArrayList<Transaction> query_transaction(String query)
    {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        ArrayList<Transaction> transactions = null;

        try
        {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            stmt = conn.createStatement();

            rs = stmt.executeQuery(query);

            while (rs.next())
            {
                Transaction t = new Transaction();
                t.setDatetime(rs.getTimestamp("datetime"));
                t.setMoney(rs.getDouble("money"));
                t.setType(rs.getString("type"));
                transactions.add(t);
            }
        }
        catch (SQLException se)
        {
            se.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (stmt != null)
                {
                    conn.close();
                }
            }
            catch (SQLException se)
            {
            }
            try
            {
                if (conn != null)
                {
                    conn.close();
                }
            }
            catch (SQLException se)
            {
                se.printStackTrace();
            }
        }
        return transactions;
    }

    public static void insert_acct(Account acct, ArrayList<Customer> owners)
    {
        return;
    }

    public static void insert_transaction(Transaction t, Customer initiatior)
    {
        return;
    }

    public static void insert_check(Check c)
    {
        return;
    }

    public static boolean update_acct(Account acct)
    {
        return false;
    }

    public static boolean update_customer(Customer c)
    {
        return false;
    }

    public static boolean delete_acct(Account acct)
    {
        return false;
    }

    public static boolean delete_customer(Customer c) {
        return false;
    }
}
