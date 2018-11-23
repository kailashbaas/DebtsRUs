package com.cs174a.kbaas;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseAccessor
{
    private static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String DB_URL = "jdbc:oracle:thin:@cloud-34-133.eci.ucsb.edu:1521:XE";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    public static ArrayList<Account> query_acct(String query)
    {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        ArrayList<Account> accts = null;

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

    public static ArrayList<Check> query_check(String query)
    {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

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
        return rs;
    }
    public static ArrayList<Customer> query_customer(String query)
    {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try
        {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            stmt = conn.createStatement();

            rs = stmt.executeQuery(query);

            while (rs.next())
            {
                Customer c = new Customer();
                c.setAddress(rs.getString("address"));
                c.setPin(rs.getString("pin"));
                c.setName(rs.getString("name"));
                c.setTaxId(rs.getInt("tax_id"));
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
        return rs;
    }

    public static ArrayList<Transaction> query_transaction(String query)
    {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try
        {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            stmt = conn.createStatement();

            rs = stmt.executeQuery(query);
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
        return rs;
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
