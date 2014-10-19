package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

import database.Query;

public class Database_client 
{
	private static String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static String DB_URL_BASE = "jdbc:mysql://localhost/";
	private static String DB_URL = DB_URL_BASE + Query.DBname;
	
	private static String USER = "root";
	private static String PASS = "";
	
	public static void createDB()
	{
		   Connection conn = null;
		   Statement stmt = null;
		   try
		   {
			   //STEP 2: Register JDBC driver
			   Class.forName(JDBC_DRIVER);

			   //STEP 3: Open a connection
			   //System.out.println("Connecting to database...");
			   conn = DriverManager.getConnection(DB_URL, USER, PASS);

			   //STEP 4: Execute a query
			   //System.out.println("Creating database...");
			   stmt = conn.createStatement();
  
			   String sql = Query.createDB_client;
			   stmt.executeUpdate(sql);
			   //System.out.println("Database created successfully...");
			   //Database.DB_URL = DB_URL_BASE + Query.DBname;
		   }
		   catch(SQLException se)
		   {
			   //Handle errors for JDBC
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
			   	//Handle errors for Class.forName
		      	e.printStackTrace();
		   }
		   finally
		   {
			   //finally block used to close resources
			   try
			   {
				   if(stmt!=null)
					   stmt.close();
			   }
			   catch(SQLException se2)
			   {
				   
			   }// nothing we can do
			   try
			   {
				   if(conn!=null)
					   conn.close();
			   }
			   catch(SQLException se)
			   {
				   se.printStackTrace();
			   }//end finally try
		   }//end try
		   //System.out.println("Goodbye!");
	}//end createDB
	
	public static void createTable() 
	{
		Connection conn = null;
		Statement stmt = null;
		try
		{
		    //STEP 2: Register JDBC driver
		    Class.forName(JDBC_DRIVER);

		    //STEP 3: Open a connection
		   // System.out.println("Connecting to a selected database..." + DB_URL);
		    conn = DriverManager.getConnection(DB_URL, USER, PASS);
		    //System.out.println("Connected database successfully...");
		    
		    //STEP 4: Execute a query
		    //System.out.println("Creating table in given database...");
		    stmt = conn.createStatement();
		    
		    String sql = Query.createTable_client; 

		    stmt.executeUpdate(sql);
		    //System.out.println("Created table in given database...");
		}
		catch(SQLException se)
		{
		    //Handle errors for JDBC
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
		    //Handle errors for Class.forName
		    e.printStackTrace();
		}
		finally
		{
			//finally block used to close resources
		    try
		    {
		    	if(stmt!=null)
		    		conn.close();
		    }
		    catch(SQLException se)
		    {
		    
		    }// do nothing
		    try
		    {
		    	if(conn!=null)
		    		conn.close();
		    }
		    catch(SQLException se)
		    {
		    	se.printStackTrace();
		    }//end finally try
		}//end try
		//System.out.println("Goodbye!");
	}//end createTable
	
	public static void insertIntoTable(String filepath, String filename, Integer size, String type) 
	{
		Connection conn = null;
		Statement stmt = null;
		try
		{
			//STEP 2: Register JDBC driver
			Class.forName(JDBC_DRIVER);
		
			//STEP 3: Open a connection
			//System.out.println("Connecting to a selected database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			//System.out.println("Connected database successfully...");
			
			//STEP 4: Execute a query
			//System.out.println("Inserting records into the table...");
			stmt = conn.createStatement();
			
			String sql = Query.insertIntoTable_client(filepath, filename, size, type);
			//System.out.println("Query is : " + sql);
			stmt.executeUpdate(sql);
			
			//System.out.println("Inserted records into the table...");
		}
		catch(SQLException se)
		{
			//Handle errors for JDBC
			se.printStackTrace();
		}
		catch(Exception e)
		{
			//Handle errors for Class.forName
			e.printStackTrace();
		}
		finally
		{
			//finally block used to close resources
			try
			{
				if(stmt!=null)
					conn.close();
			}
			catch(SQLException se)
			{
			
			}// do nothing
			try
			{
				if(conn!=null)
					conn.close();
			}
			catch(SQLException se)
			{
				se.printStackTrace();
			}//end finally try
	    }//end try
		//System.out.println("Goodbye!");
	}//end insertIntoTable
	
	public static void deleteFromTable(Integer identity) 
	{
		Connection conn = null;
		Statement stmt = null;
		try
		{
			//STEP 2: Register JDBC driver
			Class.forName(JDBC_DRIVER);
		
			//STEP 3: Open a connection
			//System.out.println("Connecting to a selected database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			//System.out.println("Connected database successfully...");
			
			//STEP 4: Execute a query
			//System.out.println("Inserting records into the table...");
			stmt = conn.createStatement();
			
			String sql = Query.deleteFromTable_client(identity);
			stmt.executeUpdate(sql);
			
			//System.out.println("Inserted records into the table...");
		}
		catch(SQLException se)
		{
			//Handle errors for JDBC
			se.printStackTrace();
		}
		catch(Exception e)
		{
			//Handle errors for Class.forName
			e.printStackTrace();
		}
		finally
		{
			//finally block used to close resources
			try
			{
				if(stmt!=null)
					conn.close();
			}
			catch(SQLException se)
			{
			
			}// do nothing
			try
			{
				if(conn!=null)
					conn.close();
			}
			catch(SQLException se)
			{
				se.printStackTrace();
			}//end finally try
	    }//end try
		//System.out.println("Goodbye!");
	}//end deleteFromTable
	
	public static ArrayList<HashMap<String, String>> selectFromTable(String file) 
	{
		Connection conn = null;
		Statement stmt = null;
		try
		{
			//STEP 2: Register JDBC driver
			Class.forName(JDBC_DRIVER);
			
			//STEP 3: Open a connection
		    //System.out.println("Connecting to a selected database...");
		    conn = DriverManager.getConnection(DB_URL, USER, PASS);
		    //System.out.println("Connected database successfully...");
		      
		    //STEP 4: Execute a query
		    //System.out.println("Creating statement...");
		    stmt = conn.createStatement();

		    String sql = Query.selectFromTable_client_byFile(file);
		    //System.out.println("Query: " + sql);
		    ResultSet rs = stmt.executeQuery(sql);
		    //STEP 5: Extract data from result set
		    ArrayList<HashMap<String, String>> selectionList = new ArrayList<HashMap<String, String>> ();
		    while(rs.next())
		    {
		    	//Retrieve by column name
		    	HashMap<String, String> hm = new HashMap<String, String> ();
		    	hm.put("identity", Integer.toString(rs.getInt("id")));
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
			//Handle errors for JDBC
			se.printStackTrace();
		}
		catch(Exception e)
		{
			//Handle errors for Class.forName
			e.printStackTrace();
		}
		finally
		{
			//finally block used to close resources
			try
			{
				if(stmt!=null)
					conn.close();
		    }
			catch(SQLException se)
			{
		    
			}// do nothing
		    try
		    {
		    	if(conn!=null)
		    		conn.close();
		    }
		    catch(SQLException se)
		    {
		    	se.printStackTrace();
		    }//end finally try
		}//end try
		//System.out.println("Goodbye!");
		return null;
	}//end selectFromTable
	
}
