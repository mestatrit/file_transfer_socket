package fileTransfer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import database.Database_server;

public class ServerThread extends Thread 
{
	private Socket socket;
	private String myIP, clientIP;
	private ObjectInputStream serversockreaderForObjects;
	private ObjectOutputStream serversockwriterForObjects;
	
	public ServerThread(Socket socket)
	{
		this.socket = socket;
		this.myIP = socket.getLocalAddress().toString();
		this.clientIP = socket.getInetAddress().toString();
		
		try 
		{
			serversockwriterForObjects = new ObjectOutputStream(socket.getOutputStream());
			serversockwriterForObjects.flush();
			serversockreaderForObjects = new ObjectInputStream(socket.getInputStream());
			Database_server.createDB();
			Database_server.createTable();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		
		/*
		 * 	It has to check if the client's share of files exceeds a certain amount
		 * 	Commands accepted:
		 * 	1. ADD - add a new file to the server log USAGE: ADD <filepath> <filename> <size> <type>
		 * 		if added successfully, return "addition done"
		 * 
		 *	2. SEARCH - search for a file in server log USAGE: SEARCH <filename>
		 *		return arraylist of hashmaps mapping info of files : 	<IP: ip address>	<filename: name of file>	<filesize: size of file>	<filetype: type of file>
		 *
		 *	3. DELETE - delete a file from server log USAGE: DELETE <file name>
		 *		if deleted successfully, return "deletion done"
		 *
		 *	4. BROADCAST - broadcast a message to all users
		 *		if broadcast done successfully, return "broadcast done"
		 * 
		 *	5. USERS - get all the currently online users 
		 *		return arraylist of online users
		 *
		 *	6. LIST - get the file list of a client with a particular ip USAGE: LIST <IP address>
		 *		return arraylist of hasmaps mapping info:		<filename>		<filesize>		<filetype>
		 *	
		 *	
		 */
		while(true)
		{
			try 
			{
				@SuppressWarnings("unchecked")
				HashMap<String, String> command = (HashMap<String, String>) serversockreaderForObjects.readObject();
				String cmd = command.get("command"); 
				
				System.out.println(command.toString());
				
				if(cmd.equals("ADD"))
				{
					String filepath = command.get("arg0");
					String filename = command.get("arg1");
					int size = Integer.parseInt(command.get("arg2"));
					String type = command.get("arg3");
					
					ArrayList<HashMap<String, String>> selectionList = Database_server.selectFromTable_byUserAndFile(clientIP, filepath);
					if(selectionList.isEmpty())
					{
						serversockwriterForObjects.writeUTF("file already hashed at server side by this user!!!");
						serversockwriterForObjects.flush();
						continue;
					}
					
					int result = Database_server.insertIntoTable(clientIP, filepath, filename, size, type);
					if(result == 0)
					{
						System.out.println("hello1");
						serversockwriterForObjects.writeUTF("addition done");
						serversockwriterForObjects.flush();
					}
					else if(result == 2)
					{
						System.out.println("hello2");
						serversockwriterForObjects.writeUTF("addition was done but the stmt/conn could not be closed");
						serversockwriterForObjects.flush();
					}
					else
					{
						System.out.println("hello3");
						serversockwriterForObjects.writeUTF("addition could not be done");
						serversockwriterForObjects.flush();
					}
				}
				else if(cmd.equals("SEARCH"))
				{
					String filename_toBeSearched = command.get("arg0");
					ArrayList<HashMap<String, String>> result = Database_server.searchFromTable(filename_toBeSearched);
					
					serversockwriterForObjects.writeObject(result);
					serversockwriterForObjects.flush();
				}
				else if(cmd.equals("DELETE"))
				{
					String filepath_toBeDeleted = command.get("arg0");
					
					int result = Database_server.deleteFromTable_byUserAndFile(clientIP, filepath_toBeDeleted);
					
					if(result == 0)
					{
						serversockwriterForObjects.writeUTF("deletion done");
						serversockwriterForObjects.flush();
					}
					else if(result == 2)
					{
						serversockwriterForObjects.writeUTF("deletion was done but stmt/conn could not be closed");
						serversockwriterForObjects.flush();
					}
					else
					{
						serversockwriterForObjects.writeUTF("could not delete");
						serversockwriterForObjects.flush();
					}
				}
				else if(cmd.equals("BROADCAST"))
				{
					String msg = command.get("arg0");
				}
				else if(cmd.equals("USERS"))
				{
					 
				}
				else if(cmd.equals("LIST"))
				{
					String IPaddr = command.get("arg0");
					System.out.println("IP address: " + IPaddr);
					ArrayList<HashMap<String, String>> result = Database_server.selectFromTable_byUser(IPaddr);
					serversockwriterForObjects.writeObject(result);
				}
				else 
				{
					System.out.println("Unknown Command!!!");
				}
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
	}
}
