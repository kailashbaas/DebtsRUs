package com.cs174a.kbaas;

import java.sql.*;

public class Test
{
    private static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String DB_URL = "jdbc:oracle:thin:@cloud-34-133.eci.ucsb.edu:1521:XE";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    public static void main(String args[])
    {
        Connection conn = null;
        Statement stmt = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String query = "SELECT cname FROM cs174.customers";
        String insert = "INSERT INTO Test(attr) VALUES(?)";

        try
        {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            //stmt = conn.createStatement();
            Timestamp t = new Timestamp(System.currentTimeMillis());
            pstmt = conn.prepareStatement(insert);
            pstmt.setTimestamp(1, t);
            pstmt.executeUpdate();

            /*rs = stmt.executeQuery(query);
            while (rs.next())
            {
                System.out.println(rs.getString("cname") + " | " +
                        rs.getString(1));
            }*/
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

        return;
    }
}