package database;

public class Query 
{
	protected static String DBname = "CNS.db";
	
	protected static String TableName_client = "CLIENT_LOG";
	protected static String createDB_client = "CREATE DATABASE " + DBname + ";";
	protected static String createTable_client = 	"CREATE TABLE if not exists " + TableName_client +
		            								" (id INTEGER not NULL," +
		            								" filepath TEXT," + 
		            								" filename TEXT," + 
		            								" size INTEGER," + 
		            								" type TEXT," + 
		            								" PRIMARY KEY ( id ))"; 
	
	protected static String TableName_server = "SERVER_LOG";
	protected static String createTable_server = 	"CREATE TABLE if not exists " + TableName_server +
													" (id INTEGER not NULL," +
													" IPaddr TEXT not NULL," + 
													" filepath TEXT," + 
													" filename TEXT," + 
													" size INTEGER," + 
													" type TEXT," + 
													" PRIMARY KEY ( id )," +
													" FOREIGN KEY ( IPaddr ) REFERENCES createTable_credentials ON DELETE CASCADE ON UPDATE CASCADE);"; 
	
	protected static String credentials = "credentials";
	protected static String createTable_credentials = "CREATE TABLE if not exists " + credentials + 
													" (IPaddr TEXT not NULL," + 
													" username TEXT not NULL," + 
													" password TEXT not NULL," + 
													" PRIMARY KEY ( IPaddr )," + 
													" UNIQUE ( username ));";
	
	
	protected static String countFromTable(String tableName)
	{
		String count = "SELECT COUNT(*) AS COUNT FROM " + tableName + ";";
		return count;
	}
	
	protected static String maxFromTable(String tableName)
	{
		String max = "SELECT MAX(id) AS MAX FROM " + tableName + ";";
		return max;
	}
	
	protected static String insertIntoTable_client(String filepath, String filename, Integer size, String type)
	{
		String insert = "INSERT INTO " + TableName_client + "(filepath, filename, size, type) VALUES(" + "\"" + filepath + "\",\"" + filename + "\"," + size + ",\"" + type + "\");";
		return insert;
	}
	
	protected static String insertIntoTable_server(String IP, String filepath, String filename, Integer size, String type)
	{
		String insert = "INSERT INTO " + TableName_server + "(IPaddr, filepath, filename, size, type) VALUES(" + "\"" + IP + "\",\"" + filepath + "\",\"" + filename + "\"," + size + ",\"" + type + "\");";
		return insert;
	}
	
	protected static String deleteFromTable_client(int identity)
	{
		String delete = "DELETE FROM " + TableName_client + " WHERE id = " + identity + ";";
		return delete;
	}
	
	protected static String deleteFromTable_server(int identity)
	{
		String delete = "DELETE FROM " + TableName_server + " WHERE id = " + identity + ";";
		return delete;
	}
	
	protected static String selectFromTable_client_byFile(String file)
	{
		String select = "SELECT id, filepath, filename, size, type FROM " + TableName_client;
		if(file == null)
		{
			// select all
			return select + ";";
		}
		select = select + " WHERE filepath = \"" + file + "\";";
		return select;
	}
	
	protected static String selectFromTable_server_byFile(String file)
	{
		String select = "SELECT id, IPaddr, filepath, filename, size, type FROM " + TableName_server;
		if(file == null)
		{
			// select all
			return select + ";";
		}
		select = select + " WHERE filepath = \"" + file + "\";";
		return select;
	}
	
	protected static String selectFromTable_server_byUser(String IPaddr)
	{
		String select = "SELECT id, IPaddr, filepath, filename, size, type FROM " + TableName_server;
		if(IPaddr == null)
		{
			// select all
			return select + ";";
		}
		select = select + " WHERE IPaddr = \"" + IPaddr + "\";";
		return select;
	}
	
	protected static String selectFromTable_server_byUserAndFile(String IPaddr, String filepath)
	{
		String select = "SELECT id, IPaddr, filepath, filename, size, type FROM " + TableName_server + " WHERE IPaddr = \"" + IPaddr + "\" AND filepath = \"" + filepath + "\";";
		return select;
	}
	
	protected static String deleteFromTable_server_byUserAndFile(String IPaddr, String filepath)
	{
		String delete = "DELETE FROM " + TableName_server + " WHERE IPaddr = \"" + IPaddr + "\" AND filepath = \"" + filepath + "\";";
		return delete;
	}

	public static String searchFromTable_server(String filename) 
	{
		String select = "SELECT * FROM " + TableName_server + " WHERE filename = \"" + filename + "\";";
		return select;
	}

	public static String verifyClient(String username, String password) 
	{
		String sql = "SELECT * FROM " + credentials + " WHERE username = \"" + username + "\" AND password = \"" + password + "\";";
		return sql;
	}
	
	public static String addClient(String username, String password, String IPaddr)
	{
		String sql = "INSERT INTO " + credentials + " VALUES (\"" + IPaddr + "\", \"" + username + "\", \"" + password + "\");";
		return sql;
	}
}
