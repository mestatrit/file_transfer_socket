package fileTransfer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ServerThread extends Thread 
{
	private Socket socket;
	private BufferedReader serversockreader;
	private BufferedWriter serversockwriter;
	
	public ServerThread(Socket socket)
	{
		this.socket = socket;
		try 
		{
			serversockreader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			serversockwriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		//TODO: act on client commands
		
		/*
		 * 	It has to check if the client's share of files exceeds a certain amount
		 * 	Commands accepted:
		 * 	1. ADD - add a new file to the server log USAGE: ADD <filename> <path> <size>
		 * 		if added successfully, return "addition done"
		 * 
		 *	2. SEARCH - search for a file in server log USAGE: SEARCH <filename>
		 *		return arraylist of hasmaps mapping info of files : 	<IP: ip address>	<filename: name of file>	<filesize: size of file>	<filetype: type of file>
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
		
		try 
		{
			String command = serversockreader.readLine();
			String[] cmdSplit = command.split(" "); 
			
			if(cmdSplit[0].equals("ADD"))
			{
				
			}
			else if(cmdSplit[0].equals("SEARCH"))
			{
				
			}
			else if(cmdSplit[0].equals("DELETE"))
			{
				
			}
			else if(cmdSplit[0].equals("BROADCAST"))
			{
				
			}
			else if(cmdSplit[0].equals("USERS"))
			{
				
			}
			else if(cmdSplit[0].equals("LIST"))
			{
				
			}
			else 
			{
				System.out.println("Unknown Command!!!");
			}
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
