package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

import database.Query;

public class Database_server
{
	private static String JDBC_DRIVER = "org.sqlite.JDBC";
	private static String DB_URL_BASE = "jdbc:sqlite:";
	private static String DB_URL = DB_URL_BASE + Query.DBname;
	
	public static int createDB()
	{
		Connection conn = null;
		Statement stmt = null;
		
		int result = 1;
		try
		{
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(DB_URL);
			stmt = conn.createStatement();
			result = 0;
		}
		catch(SQLException se)
		{
			if(se.getErrorCode() == 1007)
			{
				System.out.println("Database already exists!!!");
			}
			else
			{
				se.printStackTrace();
			}
		}
		catch(Exception e)
		{	
	    	e.printStackTrace();
		}
		finally
		{
			try
			{
				if(stmt!=null)
					stmt.close();
			}
			catch(SQLException se2)
			{
				result = 2;
			}
			try
			{
				if(conn!=null)
					conn.close();
			}
			catch(SQLException se)
			{
				se.printStackTrace();
				result = 2;
			}
		}
		
		return result;
	}
	
	public static int createTable() 
	{
		Connection conn = null;
		Statement stmt = null;
		
		int result = 1;
		try
		{
		    Class.forName(JDBC_DRIVER);
		    conn = DriverManager.getConnection(DB_URL);
		    stmt = conn.createStatement();
		    String sql = Query.createTable_server; 
		    stmt.executeUpdate(sql);
		    result = 0;		    
		}
		catch(SQLException se)
		{
			if(se.getErrorCode() == 1050)
			{
				System.out.println("Table already exixts!!!");
			}
			else
			{
				se.printStackTrace();
			}
		}
		catch(Exception e)
		{
		    e.printStackTrace();
		}
		finally
		{
		    try
		    {
		    	if(stmt!=null)
		    		conn.close();
		    }
		    catch(SQLException se)
		    {
		    	result = 2;
		    }
		    try
		    {
		    	if(conn!=null)
		    		conn.close();
		    }
		    catch(SQLException se)
		    {
		    	se.printStackTrace();
		    	result = 2;
		    }		    
		}
		
		return result;
	}
	
	public static int insertIntoTable(String IPaddr, String filepath, String filename, Integer size, String type) 
	{
		Connection conn = null;
		Statement stmt = null;
		
		int result = 1;		
		try
		{
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(DB_URL);
			stmt = conn.createStatement();
			
			String sql = Query.insertIntoTable_server(IPaddr, filepath, filename, size, type);
			stmt.executeUpdate(sql);
			result = 0;
		}
		catch(SQLException se)
		{
			se.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(stmt!=null)
					conn.close();
			}
			catch(SQLException se)
			{
				result = 2;
			}
			try
			{
				if(conn!=null)
					conn.close();
			}
			catch(SQLException se)
			{
				se.printStackTrace();
				result = 2;
			}
	    }
		return result;
		
	}
	
	public static int deleteFromTable(Integer identity) 
	{
		Connection conn = null;
		Statement stmt = null;
		
		int result = 1;
		try
		{
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(DB_URL);
			stmt = conn.createStatement();
		
			String sql = Query.deleteFromTable_server(identity);
			stmt.executeUpdate(sql);
			result = 0;
		}
		catch(SQLException se)
		{
			se.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(stmt!=null)
					conn.close();
			}
			catch(SQLException se)
			{
				result = 2;
			}
			try
			{
				if(conn!=null)
					conn.close();
			}
			catch(SQLException se)
			{
				se.printStackTrace();
				result = 2;
			}
	    }
		return result;
		
	}
	
	public static ArrayList<HashMap<String, String>> selectFromTable_byFile(String file) 
	{
		Connection conn = null;
		Statement stmt = null;
		try
		{
			Class.forName(JDBC_DRIVER);
		    conn = DriverManager.getConnection(DB_URL);
		    stmt = conn.createStatement();
		    String sql = Query.selectFromTable_server_byFile(file);
		    ResultSet rs = stmt.executeQuery(sql);
		    ArrayList<HashMap<String, String>> selectionList = new ArrayList<HashMap<String, String>> ();
		    while(rs.next())
		    {
		    	HashMap<String, String> hm = new HashMap<String, String> ();
		    	hm.put("identity", Integer.toString(rs.getInt("id")));
		    	hm.put("IPaddr", rs.getString("IPaddr"));
		    	hm.put("filepath", rs.getString("filepath"));
		    	hm.put("filename", rs.getString("filename"));
		    	hm.put("type", rs.getString("type"));
		    	hm.put("size", Integer.toString(rs.getInt("size")));
		    	selectionList.add(hm);
		    }
		    rs.close();
		    return selectionList;
		}
		catch(SQLException se)
		{
			se.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(stmt!=null)
					conn.close();
		    }
			catch(SQLException se)
			{
		    
			}
		    try
		    {
		    	if(conn!=null)
		    		conn.close();
		    }
		    catch(SQLException se)
		    {
		    	se.printStackTrace();
		    }
		}
		return null;
	}
	
	public static ArrayList<HashMap<String, String>> selectFromTable_byUser(String IPaddr) 
	{
		Connection conn = null;
		Statement stmt = null;
		try
		{
			Class.forName(JDBC_DRIVER);
		    conn = DriverManager.getConnection(DB_URL);
		    stmt = conn.createStatement();

		    String sql = Query.selectFromTable_server_byUser(IPaddr);
		    System.out.println("Query: " + sql);
		    ResultSet rs = stmt.executeQuery(sql);
		    ArrayList<HashMap<String, String>> selectionList = new ArrayList<HashMap<String, String>> ();
		    while(rs.next())
		    {
		    	HashMap<String, String> hm = new HashMap<String, String> ();
		    	hm.put("identity", Integer.toString(rs.getInt("id")));
		    	hm.put("IPaddr", rs.getString("IPaddr"));
		    	hm.put("filepath", rs.getString("filepath"));
		    	hm.put("filename", rs.getString("filename"));
		    	hm.put("type", rs.getString("type"));
		    	hm.put("size", Integer.toString(rs.getInt("size")));
		    	selectionList.add(hm);
		    }
		    rs.close();
		    return selectionList;
		}
		catch(SQLException se)
		{
			se.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(stmt!=null)
					conn.close();
		    }
			catch(SQLException se)
			{
		    
			}
		    try
		    {
		    	if(conn!=null)
		    		conn.close();
		    }
		    catch(SQLException se)
		    {
		    	se.printStackTrace();
		    }
		}
		return null;
	}
	
	public static ArrayList<HashMap<String, String>> selectFromTable_byUserAndFile(String IPaddr, String filepath)
	{
		Connection conn = null;
		Statement stmt = null;
		try
		{
			Class.forName(JDBC_DRIVER);
		    conn = DriverManager.getConnection(DB_URL);
		    stmt = conn.createStatement();

		    String sql = Query.selectFromTable_server_byUserAndFile(IPaddr, filepath);
		    ResultSet rs = stmt.executeQuery(sql);
		    ArrayList<HashMap<String, String>> selectionList = new ArrayList<HashMap<String, String>> ();
		    while(rs.next())
		    {
		    	HashMap<String, String> hm = new HashMap<String, String> ();
		    	hm.put("identity", Integer.toString(rs.getInt("id")));
		    	hm.put("IPaddr", rs.getString("IPaddr"));
		    	hm.put("filepath", rs.getString("filepath"));
		    	hm.put("filename", rs.getString("filename"));
		    	hm.put("type", rs.getString("type"));
		    	hm.put("size", Integer.toString(rs.getInt("size")));
		    	selectionList.add(hm);
		    }
		    rs.close();
		    return selectionList;
		}
		catch(SQLException se)
		{
			se.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(stmt!=null)
					conn.close();
		    }
			catch(SQLException se)
			{
		    
			}
		    try
		    {
		    	if(conn!=null)
		    		conn.close();
		    }
		    catch(SQLException se)
		    {
		    	se.printStackTrace();
		    }
		}
		return null;
	}
	
	public static int deleteFromTable_byUserAndFile(String IPaddr, String filepath) 
	{
		Connection conn = null;
		Statement stmt = null;
		
		int result = 1;
		try
		{
			Class.forName(JDBC_DRIVER);
		    conn = DriverManager.getConnection(DB_URL);
		    stmt = conn.createStatement();

		    String sql = Query.deleteFromTable_server_byUserAndFile(IPaddr, filepath);
		    stmt.executeUpdate(sql);
		    result = 0;
		}
		catch(SQLException se)
		{
			se.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(stmt!=null)
					conn.close();
		    }
			catch(SQLException se)
			{
				result = 2;
			}
		    try
		    {
		    	if(conn!=null)
		    		conn.close();
		    }
		    catch(SQLException se)
		    {
		    	se.printStackTrace();
		    	result = 2;
		    }
		}
		return result;
	}
	
	public static ArrayList<HashMap<String, String>> searchFromTable(String filename) 
	{
		Connection conn = null;
		Statement stmt = null;
		
		ArrayList<HashMap<String, String>> selectionList = new ArrayList<HashMap<String,String>> ();
		
		try
		{
			Class.forName(JDBC_DRIVER);
		    conn = DriverManager.getConnection(DB_URL);
		    stmt = conn.createStatement();

		    String sql = Query.searchFromTable_server(filename);
		    System.out.println("Query: " + sql);
		    ResultSet rs = stmt.executeQuery(sql);
		    while(rs.next())
		    {
		    	HashMap<String, String> hm = new HashMap<String, String> ();
		    	hm.put("identity", Integer.toString(rs.getInt("id")));
		    	hm.put("IPaddr", rs.getString("IPaddr"));
		    	hm.put("filepath", rs.getString("filepath"));
		    	hm.put("filename", rs.getString("filename"));
		    	hm.put("type", rs.getString("type"));
		    	hm.put("size", Integer.toString(rs.getInt("size")));
		    	selectionList.add(hm);
		    }
		    rs.close();
		    
		}
		catch(SQLException se)
		{
			se.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(stmt!=null)
					conn.close();
		    }
			catch(SQLException se)
			{
			}
		    try
		    {
		    	if(conn!=null)
		    		conn.close();
		    }
		    catch(SQLException se)
		    {
		    	se.printStackTrace();
		    }
		}
		return selectionList;
	}	
}
