package com.cs174a.kbaas;

import java.sql.*;

public class Test
{
    public static void main(String args[])
    {
        String query = "SELECT cname FROM cs174.customers";
        try
        {
            ResultSet rs = DatabaseAccessor.query_db(query);
            System.out.println("here");

            while (rs.next())
            {
                System.out.println(rs.getString("cname") + " | " +
                        rs.getString(1));
            }
        }
        catch (SQLException se)
        {
            System.out.println(se.getMessage());
        }

        return;
    }
}
