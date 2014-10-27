package fileTransfer;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import database.Database_server;

public class ServerThread extends Thread 
{
	private Socket socket;
	private static int id = 0;
	private int thisID;
	private String myIP, clientIP;
	private ObjectInputStream serversockreaderForObjects;
	private ObjectOutputStream serversockwriterForObjects;
	private long time; 
	private boolean running = true;
	
	public long getTime()
	{
		return time;
	}
	
	public ServerThread(Socket socket)
	{
		thisID = id;
		System.out.println("Thfread with id : " + thisID + " is constructed...");
		id ++;
		
		this.socket = socket;
		this.myIP = socket.getLocalAddress().toString();
		this.clientIP = socket.getInetAddress().toString();
		
		try 
		{
			serversockwriterForObjects = new ObjectOutputStream(socket.getOutputStream());
			serversockwriterForObjects.flush();
			serversockreaderForObjects = new ObjectInputStream(socket.getInputStream());
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
		 *	4. USERS - get all the currently online users 
		 *		return arraylist of online users
		 *
		 *	5. LIST - get the file list of a client with a particular ip USAGE: LIST <IP address>
		 *		return arraylist of hasmaps mapping info:		<filename>		<filesize>		<filetype>
		 *	
		 *	
		 */
		while(running)
		{
			try 
			{
				HashMap<String, String> command = readFromClient();
				if(!running)
				{
					break;
				}
				
				String cmd = command.get("command"); 
				
				System.out.println(command.toString());
				
				if(cmd.equals("PING"))
				{
					continue;
				}
				
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
					System.out.println("deleting");
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
				else if(cmd.equals("USERS"))
				{
					 ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String,String>> (); 
					 for(Map.Entry<ServerThread, Socket> entry: Server.connectedUsers.entrySet())
					 {
						 HashMap<String, String> hm = new HashMap<String, String> ();
						 hm.put("response", entry.getValue().getInetAddress().toString());
						 result.add(hm);
					 }
					 serversockwriterForObjects.writeObject(result);
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
				System.out.println("exception raised... closing the thread!!!");
				running = false;
			}
		}
		try 
		{
			serversockreaderForObjects.close();
			serversockwriterForObjects.close();
			socket.close();
			return ;
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			System.out.println("exception raised again!!! ");
			return ;
		}
		
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, String> readFromClient() 
	{
		HashMap<String, String> cmd = null;
		try 
		{
			cmd = (HashMap<String, String>) serversockreaderForObjects.readObject();
		}
		catch(EOFException e)
		{
			terminate();
			System.out.println("hello3!");
		}
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}	
		
		return cmd;
	}

	public void terminate() 
	{
		running = false;
		System.out.println("This thread is going to be closed....ID: " + thisID);
	}
}
