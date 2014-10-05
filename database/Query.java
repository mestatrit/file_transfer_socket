package database;

public class Query 
{
	protected static String DBname = "FILE_INFO";
	protected static String TableName = "REGISTRATION";
	
	protected static String createDB = "CREATE DATABASE " + DBname;
	protected static String createTable = 	"CREATE TABLE REGISTRATION " +
            								"(id INTEGER not NULL, " +
            								" filepath TEXT, " + 
            								" size INTEGER, " + 
            								" type TEXT, " + 
            								" PRIMARY KEY ( id ))"; 
	
	private static Integer id = 1;
	
	protected static String insertIntoTable(String file, Integer size, String type)
	{
		String insert = "INSERT INTO " + TableName + " VALUES(" + id + ",\"" + file + "\"," + size + ",\"" + type + "\")";
		id ++;
		return insert;
		
	}
	
	protected static String deleteFromTable(int identity)
	{
		String delete = "DELETE FROM " + TableName + " WHERE id = " + identity;
		return delete;
	}
	
	protected static String selectFromTable(String file)
	{
		String select = "SELECT id, filepath, size, type FROM " + TableName;
		if(file == null)
		{
			// select all
			return select;
		}
		select = select + " WHERE filepath = \"" + file + "\"";
		return select;
	}
	
	
}
