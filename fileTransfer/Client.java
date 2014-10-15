package fileTransfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.validator.routines.InetAddressValidator;

import database.Database;

public class Client 
{
	private static BufferedReader br;
	private static BufferedReader serversockreader;
	private static ObjectInputStream serversockreaderForObjects;
	private static BufferedWriter serversockwriter;
	private static Timer timerForRechecking;
	private static int recheckinterval = 15; // seconds

	private static int serverPORT = 5001; // port on which clients connect to server
	private static int clientPORT = 5002; // port on which client peers connect amongst them

	private Socket serverSocket; // Assumption: can connect to just one server at a time
	private Socket clientSocket;

	private String downloadDir;
		
	public Client()
	{		
		try
		{
			Database.createDB();
			Database.createTable();
		}
		catch(Exception E)
		{
			E.printStackTrace();
		}
				
		timerForRechecking = new Timer();
		timerForRechecking.scheduleAtFixedRate(new recheck(), 0, recheckinterval*1000);
	}
	
	class recheck extends TimerTask
	{
		@Override
		public void run()
		{
			ArrayList<HashMap<String, String>> log = Database.selectFromTable(null);
			for(HashMap<String, String> hm:log)
			{
				String filepath = hm.get("filepath");
				int identity = Integer.parseInt(hm.get("identity"));
				File file = new File(filepath);
				if(!file.exists())
				{
					// file no longer exists
					System.out.println("\nFile no longer exists!!!\n");
					Database.deleteFromTable(identity);
					String command = "DELETE " + file.getAbsolutePath();
					System.out.println("Issueing command : \n" + command);
					issueCommandToServer(command);
				}
			}
		}
	}
	
	public static void main(String[] args)
	{
		Client client = new Client();
		client.execute();
	}

	private void execute() 
	{
		br = new BufferedReader(new InputStreamReader(System.in));
		boolean flag = true;
		do
		{
			System.out.println("Specify the download directory: ");
		
			try 
			{
				File file = new File(downloadDir);
				if(!file.exists() || !file.isDirectory())
				{
					System.out.println("Please enter a valid download directory.");
					downloadDir = null;			
					return ;
				}
				connectToServer();
				showChoices();
				flag = false;
			}
			catch(Exception E)
			{
				E.printStackTrace();
			}
		}while(flag);
	}

	private void connectToServer() 
	{
		System.out.println("Enter the IP address of the server: \n");
		try 
		{
			String servaddr = br.readLine();
			serverSocket = new Socket(servaddr, serverPORT);
			serversockreader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
			serversockwriter = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()));
			serversockreaderForObjects = new ObjectInputStream(serverSocket.getInputStream());
			System.out.println("Client socket has been created!!!");
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	// show choices to the user
	private void showChoices() 
	{
		System.out.println("Enter your choices:\n");
		System.out.println("1. Search for a file\n2. Share a file\n3. Send a message\n4. Get all online users\n5. Get a user's file list\n6. Download a file");
		try 
		{
			while(true)
			{
				int choice = br.read() - '0';
				
				switch(choice)
				{
					case 1: searchForFile(); 
							break;
					case 2: shareFile();
							break;
					case 3: serverBroadcastMsg();
							break;
					case 4:	getAllUsers();
							break;
					case 5:	getFileList();
							break;	
					case 6:	downloadFile();
							break;
					default:
							break;
				}
			}
		} 
		catch (IOException e) 
		{
			System.out.println("Sorry, input error!!!");
			e.printStackTrace();
		}
	}

	private void downloadFile() throws IOException 
	{
		System.out.println("Enter the IP address of the peer: ");
		String IPaddr = br.readLine();
		InetAddressValidator IPvalidator = new InetAddressValidator();
		if(!IPvalidator.isValid(IPaddr))
		{
			System.out.println("Please check the IP address that you have provided.");
			return ;
		}
		System.out.println("Enter the whole file path, that you wish to download from the peer");
		String fileName = br.readLine();
		String command = "GETFILE " + IPaddr + " " + fileName;
		issueCommandToServer(command);
	}

	private void getFileList() throws IOException 
	{
		String IP = br.readLine();
		String command = "LIST " + IP;
		issueCommandToServer(command);
	}

	private void getAllUsers() 
	{
		String command = "USERS";
		issueCommandToServer(command);
	}

	private void serverBroadcastMsg() throws IOException 
	{
		// send the command to the server to broadcast the message
		String msg = br.readLine();
		String cmd = "BROADCAST " + msg;
		
		// issue command to the server
		issueCommandToServer(cmd);
	}

	private void shareFile() 
	{		
		System.out.println("Enter the full path of the file of the corresponding directory that you want to share: ");
		try 
		{
			br.read();
			String shareThis = br.readLine();
			File file = new File(shareThis);
			
			if(!file.exists())
			{
				System.out.println("Sorry, the file with the particular path does not exists...");
				return ;
			}
			else if(file.isDirectory())
			{
				addToLogDirectory(file);
			}
			else if(file.isFile())
			{
				// add this file to the sharing list
				addToLogFile(file);
			}
			else
			{
				System.out.println("Unknown error!!!");
			}
				
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	private void addToLogDirectory(File file)
	{
		File[] files = file.listFiles();
		for(File file1 : files)
		{
			if(file1.isFile())
			{
				//System.out.println(file1.getName());
				addToLogFile(file1);
			}
			else
			{
				//System.out.println(file1.getName());
				addToLogDirectory(file1);
			}
		}
	}
	
	private void addToLogFile(File file)
	{
		System.out.println(file.getAbsolutePath());
		String filepath = file.getAbsolutePath();
		Integer size = (int) file.length();
		String type = null;

		try 
		{
			type = Files.probeContentType(file.toPath());
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		ArrayList<HashMap<String, String>> result = Database.selectFromTable(filepath);
		if(result.size() > 0)
		{
			System.out.println("File " + filepath + " already hashed!!!");
		}
		else
		{
			// new file added to the log
			Database.insertIntoTable(filepath, size, type);
			String command = "ADD " + filepath + " " + size + " " + type;
			issueCommandToServer(command);
		}
	}
	
	private void searchForFile() 
	{
		System.out.println("Enter the filename to search for:");
		try 
		{
			br.read();
			String searchForThis = br.readLine();
			System.out.println("Your command: search for " + searchForThis + " has been issued");
			String command = "SEARCH " + searchForThis;
			issueCommandToServer(command);
		} 
		catch (IOException e) 
		{
			System.out.println("Input reading error!!!");
			e.printStackTrace();
		}
	}
	
	private String issueCommandToServer(String command) 
	{
		/*	valid commands:
		 *	1. ADD - add a new file to the server log USAGE: ADD <filename> <path> <size>
		 *	2. SEARCH - search for a file in server log USAGE: SEARCH <filename>
		 *	3. DELETE - delete a file from server log USAGE: DELETE <file name>
		 *	4. BROADCAST - broadcast a message to all users 
		 *	5. USERS - get all the currently online users 
		 *	6. LIST - get the file list of a client with a particular ip USAGE: LIST <IP address>
		 *	7. GETFILE - receive the actual file from the user USAGE : GETFILE <IP address> <full filename>
		 * */
		
		String[] split = command.split(" "); 
		String keyword = split[0];
		String result = null;
		try
		{
			if(keyword.equals("ADD"))
			{
				System.out.println("New file is being added to the server log...");
				serversockwriter.write(command + "\n");
				serversockwriter.flush();
				result = serversockreader.readLine();
				if(result.equalsIgnoreCase("addition done"))
				{
					System.out.println("Addition has been done.");
				}
				else
				{
					System.out.println("Sorry, server error. Please try again later...");
					result = "addition error";
				}
			}
			else if(keyword.equals("SEARCH"))
			{
				System.out.println("Searching for a file in the server log...");
				serversockwriter.write(command + "\n");
				serversockwriter.flush();
				@SuppressWarnings("unchecked")
				ArrayList<HashMap<String, String>> resultForSearch = (ArrayList<HashMap<String,String>>) serversockreaderForObjects.readObject();
				System.out.println("Results for your search query are: ");
				System.out.println("IP address    File Name    Size    Type  ");
				for(HashMap<String, String> hm:resultForSearch)
				{
					System.out.println(hm.get("IP") + " | " + hm.get("filename") + " | " + hm.get("filesize") + " | " + hm.get("filetype"));
				}				
			}
			else if(keyword.equals("DELETE"))
			{
				System.out.println("Deleting a file in the server log...");
				serversockwriter.write(command + "\n");
				serversockwriter.flush();
				result = serversockreader.readLine();
				if(result.equalsIgnoreCase("deletion done"))
				{
					System.out.println("Deletion has been done...");
				}
				else
				{
					System.out.println("Sorry, server error. Please try again later...");
					result = "deletion error";
				}
			}
			else if(keyword.equals("BROADCAST"))
			{
				System.out.println("Broadcasting the message...");
				serversockwriter.write(command + "\n");
				serversockwriter.flush();
				result = serversockreader.readLine();
				if(result.equalsIgnoreCase("broadcast done"))
				{
					System.out.println("Your message has been sent...");
				}
				else
				{
					System.out.println("Sorry, server error. Please try again later...");
					result = "broadcast error";
				}
			}
			else if(keyword.equals("USERS"))
			{
				System.out.println("Getting all the currently online users...");
				serversockwriter.write(command + "\n");
				serversockwriter.flush();
				
				// returns an arraylist of ip addresses of currently online users
				@SuppressWarnings("unchecked")
				ArrayList<String> serverOnlineUsers = (ArrayList<String>) serversockreaderForObjects.readObject();
				
				System.out.println("IP addresses of currently online users are: ");
				
				int i = 1;
				for(String str:serverOnlineUsers)
				{
					System.out.println(i + " ----> " + str);
					i ++;
				}	
			}
			else if(keyword.equals("LIST"))
			{
				String IP = split[1];
				
				System.out.println("Getting the file list of " + IP);
				serversockwriter.write(command + "\n");
				serversockwriter.flush();
				
				// returns file paths of the user specified
				@SuppressWarnings("unchecked")
				ArrayList<String> fileListUser = (ArrayList<String>) serversockreaderForObjects.readObject();
				
				System.out.println("Files shared by the user " + IP + " are: ");
				
				int i = 1;
				for(String str:fileListUser)
				{
					System.out.println(i + " ----> " + str);
					i ++;
				}	
			}
			else if(keyword.equals("GETFILE"))
			{
				/*
				 *  To get file from the user:
				 *  1. Create a connection to the user (clientsocket)
				 *  2. Receive the max size of a packet (MAXSIZE) 
				 *  3. Receive number of transmissions (packets)
				 *  4. Open the required file in reading and binary mode
				 *  5. Loop for this number of times and receive packets in chunk
				 *  6. Save chunk into the file
				 *  
				 *  And that's how its done....
				 */
				String clientIP = split[1]; // ip of client
				
				String peerName = split[2]; // file name
				String[] splitPeerName = peerName.split("/");
				String myFileName = splitPeerName[splitPeerName.length-1];
				File myFile = new File(downloadDir + "/" + myFileName);
				BufferedOutputStream myFileWriter = new BufferedOutputStream(new FileOutputStream(myFile));

				clientSocket = new Socket(clientIP, clientPORT);
				BufferedInputStream clientSocketReader = new BufferedInputStream(clientSocket.getInputStream());
				
				int MAXSIZE = clientSocketReader.read();
				int transmissions = clientSocketReader.read();
				
				int i = 0;
				byte buffer[] = new byte[MAXSIZE];
				while(i < transmissions)
				{
					clientSocketReader.read(buffer);
					System.out.println("Received " + i);
					myFileWriter.write(buffer);
					myFileWriter.flush();
				}
				clientSocketReader.close();
				
				myFileWriter.close();
			}
			else
			{
				System.out.println("Sorry, unknown command");
			}
		}
		catch(Exception E)
		{
			E.printStackTrace();
		}
		return result;
	}
}
