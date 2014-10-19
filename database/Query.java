package database;

public class Query 
{
	protected static String DBname = "CNS";
	
	protected static String TableName_client = "CLIENT_LOG";
	protected static String createDB_client = "CREATE DATABASE " + DBname + ";";
	protected static String createTable_client = 	"CREATE TABLE " + TableName_client +
		            								" (id INTEGER not NULL AUTO_INCREMENT," +
		            								" filepath TEXT," + 
		            								" filename TEXT," + 
		            								" size INTEGER," + 
		            								" type TEXT," + 
		            								" PRIMARY KEY ( id ));"; 
	
	protected static String TableName_server = "SERVER_LOG";
	protected static String createTable_server = 	"CREATE TABLE " + TableName_server +
													" (id INTEGER not NULL AUTO_INCREMENT," +
													" IPaddr TEXT not NULL," + 
													" filepath TEXT," + 
													" filename TEXT," + 
													" size INTEGER," + 
													" type TEXT," + 
													" PRIMARY KEY ( id ));"; 
	
	
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
	
	protected static String selectFromTable_sever_byFile(String file)
	{
		String select = "SELECT id, filepath, filename, size, type FROM " + TableName_server;
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
		String select = "SELECT id, filepath, filename, size, type FROM " + TableName_server;
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
		String select = "SELECT id, filepath, filename, size, type FROM " + TableName_server + " WHERE IPaddr = \"" + IPaddr + "\" AND filepath = \"" + filepath + "\";";
		return select;
	}
	
	protected static String deleteFromTable_server_byUserAndFile(String IPaddr, String filepath)
	{
		String delete = "DELETE FROM " + TableName_server + " WHERE IPaddr = \"" + IPaddr + "\" AND filepath = \"" + filepath + "\";";
		return delete;
	}

	
}
