/****************************************************************************
 *
 * Copyright (c) 2008
 * eTalent Corporation, ALL RIGHTS RESERVED.
 *
 * Contact: team9@uml.cs.uga.edu
 * Module:
 * $Id: DatabaseTool.java,v 1.6 2008/12/05 19:40:04 zhu Exp $
 * $Author: zhu $
 * $Log: DatabaseTool.java,v $
 * Revision 1.6  2008/12/05 19:40:04  zhu
 * no message
 *
 * Revision 1.5  2008/12/05 08:32:43  zhu
 * no message
 *
 * Revision 1.4  2008/12/04 21:55:06  zhu
 * no message
 *
 * Revision 1.3  2008/12/03 06:13:11  zhu
 * *** empty log message ***
 *
 * Revision 1.2  2008/12/02 08:43:19  zhu
 * *** empty log message ***
 *
 * Revision 1.1  2008/12/01 19:13:05  lodge
 * Added basic DatabaseTool.java.
 *
 ****************************************************************************/


import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;



public class DatabaseTool {
    private Connection con;
    public boolean exec(String sql) { //Need a safe execute using a PreparedStatement!
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String dbURL = "jdbc:mysql://128.192.141.217:3306/test";
            String username = "root";
            String password = "whatmine";
            con = DriverManager.getConnection(dbURL, username, password);
            Statement stm = con.createStatement();
            stm.executeUpdate(sql);
            con.close();
            stm.close();
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseTool.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(DatabaseTool.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        return true;
      }
    public ResultSet getResults(String sql) {
        try{
            Class.forName("com.mysql.jdbc.Driver");
            String dbURL = "jdbc:mysql://128.192.141.217:3306/test";
            String username = "root";
            String password = "whatmine";
            con = DriverManager.getConnection(dbURL, username, password);
            Statement stm = con.createStatement();
            ResultSet rs=stm.executeQuery(sql);
            con.close();
            stm.close();
            return rs;
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseTool.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(DatabaseTool.class.getName()).log(Level.SEVERE, null, ex);
            }
        return null;
    }
    
    /**
     * dj modified
     */
    
    public static Connection openConnection() 
                throws Exception
        {
            Class.forName("org.gjt.mm.mysql.Driver").newInstance();
            return DriverManager.getConnection("jdbc:mysql://128.192.141.217:3306/test", "root", "whatmine");
        }
        
        public static void closeConnection(Connection conn) throws Exception
        {
            if ( conn != null )
            {
                conn.close();
            }
        }

        public static int executeUpdate(String sql) throws Exception
        {
           int count = 0;
            
            Connection conn = null;
            Statement  stmt = null;
            try
            {
                conn = openConnection();
                stmt = conn.createStatement();
            
                count = stmt.executeUpdate(sql);
            }
            catch ( Exception e )
            {
                throw e;
            }
            finally
            {
                closeConnection(conn);
            }
            return count;
        }
        
        public static List executeQuery(String sql) throws Exception
        {
            List list = new ArrayList();            
            Connection conn = null;
            Statement  stmt = null;
            ResultSet  rs   = null;           
            try
            {
                conn = openConnection();
                stmt = conn.createStatement();
                rs   = stmt.executeQuery(sql);
                ResultSetMetaData rsmd = rs.getMetaData();
            
                while ( rs.next() )
                {
                    Map map = new HashMap();
                    
                    for ( int i = 1; i <= rsmd.getColumnCount(); i++ )
                    {
                        map.put(rsmd.getColumnName(i), rs.getObject(i));
                    }                   
                    list.add(map);
                }
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
            finally
            {
                if ( rs != null ) rs.close();
                closeConnection(conn);
            }            
            return list;
        }


        public static ResultSet executeQueryReturnRS(String sql) throws Exception
        {
            Connection conn = null;
            Statement  stmt = null;
            ResultSet  rs   = null;
            try
            {
                conn = openConnection();
                stmt = conn.createStatement();
                rs   = stmt.executeQuery(sql);
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
            finally
            {
                if ( rs != null ) rs.close();
                closeConnection(conn);
            }
            return rs;
        }   
}
