package com.cs174a.kbaas;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseAccessor
{
    private static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String DB_URL = "jdbc:oracle:thin:@cloud-34-133.eci.ucsb.edu:1521:XE";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    public static ResultSet query_db(String query)
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

    public static boolean delete_customer(Customer c)
    {
        return false;
    }

}
