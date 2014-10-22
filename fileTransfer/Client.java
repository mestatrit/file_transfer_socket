package fileTransfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.validator.routines.InetAddressValidator;
import database.Database_client;

public class Client 
{
	private static BufferedReader br;
	private static ObjectInputStream serversockreaderForObjects;
	private static ObjectOutputStream serversockwriterForObjects;
	private static ScheduledExecutorService timerForRechecking;
	private ClientThread ct;

	private static int serverPORT = 5003; // port on which clients connect to server
	private static int clientPORT = 5002; // port on which client peers connect amongst them

	private Socket serverSocket; // Assumption: can connect to just one server at a time
	private Socket clientSocket;

	private String downloadDir;
		
	public Client()
	{		
		try
		{
			Database_client.createDB();
			Database_client.createTable();
		}
		catch(Exception E)
		{
			E.printStackTrace();
		}
				
		timerForRechecking = Executors.newScheduledThreadPool(5);
	}
	
	class recheck implements Runnable 
	{
		public void run()
		{	
			ArrayList<HashMap<String, String>> log = Database_client.selectFromTable(null);
			for(HashMap<String, String> hm:log)
			{
				String filepath = hm.get("filepath");
				int identity = Integer.parseInt(hm.get("identity"));
				File file = new File(filepath);
				if(!file.exists())
				{
					// file no longer exists
					System.out.println("\nFile no longer exists!!!\n");
					Database_client.deleteFromTable(identity);
					//String command = "DELETE " + file.getAbsolutePath();
					//System.out.println("Issuing command : \n" + command);
					HashMap<String, String> command = new HashMap<String, String> ();
					command.put("command", "DELETE");
					command.put("arg0", file.getAbsolutePath());
					
					issueCommandToServer(command);
				}
			}
		}
	}
	
	public static void main(String[] args)
	{
		Client client = new Client();
		client.accept();
		client.execute();
	}

	private void accept() 
	{
		ct = new ClientThread();
		ct.start();
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
				downloadDir = br.readLine();
				File file = new File(downloadDir);
				if(!file.exists() || !file.isDirectory())
				{
					System.out.println("Please enter a valid download directory.");
					downloadDir = null;			
					continue ;
				}
				connectToServer();
				System.out.println("hello");

				timerForRechecking.scheduleWithFixedDelay(new recheck(), 5, 15, TimeUnit.SECONDS);

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
			serversockwriterForObjects = new ObjectOutputStream(serverSocket.getOutputStream());
			serversockwriterForObjects.flush();
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
		try 
		{
			while(true)
			{
				System.out.println("Enter your choices:\n");
				System.out.println("1. Search for a file\n2. Share a file\n3. Send a message\n4. Get all online users\n5. Get a user's file list\n6. Download a file\n7. Exit");
				
				int choice = br.read() - '0';
				br.read();

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
					case 7: exitWithGrace();
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

	private void exitWithGrace() 
	{
		try
		{
			serversockwriterForObjects.close();
			serversockreaderForObjects.close();
			serverSocket.close();
			System.exit(0);
		}
		catch(Exception E)
		{
			E.printStackTrace();
			System.out.println("Exception is raised in client");
			System.exit(0);
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
		String filePath = br.readLine();
		//String command = "GETFILE " + IPaddr + " " + fileName;
		HashMap<String, String> command = new HashMap<String, String> ();
		command.put("command", "GETFILE");
		command.put("arg0", IPaddr);
		command.put("arg1", filePath);
		issueCommandToServer(command);
	}

	private void getFileList() throws IOException 
	{
		System.out.println("Enter IP address of the user, whose list is required:");
		String IP = br.readLine();
		//String command = "LIST " + IP;
		HashMap<String, String> command = new HashMap<String, String> ();
		command.put("command", "LIST");
		command.put("arg0", IP);
		issueCommandToServer(command);
	}

	private void getAllUsers() 
	{
		//String command = "USERS";
		HashMap<String, String> command = new HashMap<String, String> ();
		command.put("command", "USERS");
		
		issueCommandToServer(command);
	}

	private void serverBroadcastMsg() throws IOException 
	{
		// send the command to the server to broadcast the message
		String msg = br.readLine();
		//String cmd = "BROADCAST " + msg;
		HashMap<String, String> command = new HashMap<String, String> ();
		command.put("command", "BROADCAST");
		command.put("arg0", msg);
		
		issueCommandToServer(command);
	}

	private void shareFile() 
	{		
		System.out.println("Enter the full path of the file of the corresponding directory that you want to share: ");
		try 
		{
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
		String filename = file.getName();
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
		ArrayList<HashMap<String, String>> result = Database_client.selectFromTable(filepath);
		if(result.size() > 0)
		{
			System.out.println("File " + filepath + " already hashed!!!");
		}
		else
		{
			// new file added to the log
			Database_client.insertIntoTable(filepath, filename, size, type);
			//String command = "ADD " + filepath + " " + filename + " " + size + " " + type;
			HashMap<String, String> command = new HashMap<String, String> ();
			command.put("command", "ADD");
			command.put("arg0", filepath);
			command.put("arg1", filename);
			command.put("arg2", size.toString());
			command.put("arg3", type);
			
			issueCommandToServer(command);
		}
	}
	
	private void searchForFile() 
	{
		System.out.println("Enter the filename to search for:");
		try 
		{
			String searchForThis = br.readLine();
			System.out.println("Your command: search for " + searchForThis + " has been issued");
			//String command = "SEARCH " + searchForThis;
			HashMap<String, String> command = new HashMap<String, String> ();
			command.put("command", "SEARCH");
			command.put("arg0", searchForThis);
			
			issueCommandToServer(command);
		} 
		catch (IOException e) 
		{
			System.out.println("Input reading error!!!");
			e.printStackTrace();
		}
	}
	
	private void issueCommandToServer(HashMap<String, String> command) 
	{
		/*	valid commands:
		 *	1. ADD - add a new file to the server log USAGE: ADD <filepath> <filename> <size> <type>
		 *	2. SEARCH - search for a file in server log USAGE: SEARCH <filename>
		 *	3. DELETE - delete a file from server log USAGE: DELETE <filename>
		 *	4. BROADCAST - broadcast a message to all users 
		 *	5. USERS - get all the currently online users 
		 *	6. LIST - get the file list of a client with a particular ip USAGE: LIST <IP address>
		 *	7. GETFILE - receive the actual file from the user USAGE : GETFILE <IP address> <full filename>
		 * */
		
		String keyword = command.get("command");
		ArrayList<HashMap<String, String>> result = null;
		try
		{
			if(keyword.equals("ADD"))
			{
				System.out.println("New file is being added to the server log...");
				serversockwriterForObjects.writeObject(command);
				serversockwriterForObjects.flush();
				result = readFromServer();
				//result = serversockreaderForObjects.readUTF();
				if(result == null || !result.get(0).get("response").equals("addition done"))
				{
					System.out.println("Sorry, server error. Please try again later...");
					return;
				}
				else
				{
					System.out.println("Addition has been done.");
					return ;
				}
			}
			else if(keyword.equals("SEARCH"))
			{
				System.out.println("Searching for a file in the server log...");
				serversockwriterForObjects.writeObject(command);
				serversockwriterForObjects.flush();
				result = readFromServer();
				
				if(result == null)
				{
					System.out.println("Sorry, server error. Please try again later...");
					return ;
				}
				else
				{
					System.out.println("Results for your search query are: ");
					System.out.println("IP address    File Name    Size    Type  ");
					for(HashMap<String, String> hm:result)
					{
						System.out.println(hm.get("IPaddr") + " | " + hm.get("filename") + " | " + hm.get("size") + " | " + hm.get("type"));
					}		
					return ;
				}
			}
			else if(keyword.equals("DELETE"))
			{
				System.out.println("Deleting a file in the server log...");
				serversockwriterForObjects.writeObject(command);
				serversockwriterForObjects.flush();
				result = readFromServer();
				if(result == null || !result.get(0).get("response").equals("deletion done"))
				{
					System.out.println("Sorry, server error. Please try again later...");
					return ;
				}
				else
				{
					System.out.println("Deletion has been done...");
					return;
				}
			}
			else if(keyword.equals("BROADCAST"))
			{
				System.out.println("Broadcasting the message...");
				serversockwriterForObjects.writeObject(command);
				serversockwriterForObjects.flush();
				result = readFromServer();
				if(result == null || !result.get(0).get("response").equals("broadcast done"))
				{
					System.out.println("Sorry, server error. Please try again later...");
					return ;
				}
				else
				{
					System.out.println("Your message has been sent...");
					return ;
				}
			}
			else if(keyword.equals("USERS"))
			{
				System.out.println("Getting all the currently online users...");
				serversockwriterForObjects.writeObject(command);
				serversockwriterForObjects.flush();
				
				// returns an arraylist of ip addresses of currently online users
				result = readFromServer();
				
				if(result == null)
				{
					System.out.println("Sorry, server error. Please try again later...");
					return ;
				}
				else
				{
					System.out.println("IP addresses of currently online users are: ");
					
					int i = 1;
					for(HashMap<String, String> hm:result)
					{
						System.out.println(i + " ----> " + hm.get("response"));
						i ++;
					}
					
					return ;
				}
			}
			else if(keyword.equals("LIST"))
			{
				String IP = command.get("arg0");
				
				System.out.println("Getting the file list of " + IP);
				serversockwriterForObjects.writeObject(command);
				serversockwriterForObjects.flush();
				
				// returns file paths of the user specified
				result = readFromServer();
				
				if(result == null)
				{
					System.out.println("Sorry, server error. Please try again later...");
					return ;
				}
				else
				{
					System.out.println("Files shared by the user " + IP + " are: ");
					System.out.println("File Name    Size    Type  \n");
					
					for(HashMap<String, String> hm:result)
					{
						System.out.println(hm.get("filename") + " | " + hm.get("size") + " | " + hm.get("type"));
					}
					
					return ;
				}
			}
			else if(keyword.equals("GETFILE"))
			{
				/*
				 *  To get file from the user:
				 *  1. Create a connection to the user (clientsocket)
				 *  2. Receive the max size of a packet (MAXSIZE) 
				 *  3. Send the filename you wish to download
				 *  4. Receive number of transmissions (packets)
				 *  5. Open the required file in reading and binary mode
				 *  6. Loop for this number of times and receive packets in chunk
				 *  7. Save chunk into the file
				 *  
				 *  And that's how its done....
				 */
				String clientIP = command.get("arg0"); // ip of client
				
				String peerFilePath = command.get("arg1"); // file name
				String[] splitPeerPath = peerFilePath.split("/");
				String myFileName = splitPeerPath[splitPeerPath.length-1];
				File myFile = new File(downloadDir + "/" + myFileName);
				if(!myFile.exists())
				{
					myFile.createNewFile();
				}
				
				BufferedOutputStream myFileWriter = new BufferedOutputStream(new FileOutputStream(myFile));

				clientSocket = new Socket(clientIP, clientPORT);
				DataInputStream clientSocketReader = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
				DataOutputStream clientSocketWriter = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
				
				int MAXSIZE = clientSocketReader.readInt();
				clientSocketWriter.writeUTF(peerFilePath);
				clientSocketWriter.flush();
				
				int flag = clientSocketReader.read();
				if(flag == 1)
				{
					System.out.println("Some error occured!!!!");
					myFileWriter.close();
					myFile.delete();
					clientSocketReader.close();
					clientSocketWriter.close();
					clientSocket.close();
					return ;
				}
				
				int transmissions = clientSocketReader.readInt();
				
				int i = 0;
				byte buffer[] = new byte[MAXSIZE];
				System.out.println("Hello!!! happy reading");
				while(i < transmissions)
				{
					clientSocketReader.read(buffer);
					int length = clientSocketReader.readInt();
					System.out.println("Received " + i);
					myFileWriter.write(buffer, 0, length);
					myFileWriter.flush();
					i ++;
				}
				clientSocketWriter.writeUTF("DONE");
				clientSocketWriter.flush();
				myFileWriter.close();
				clientSocketReader.close();
				clientSocketWriter.close();
				clientSocket.close();
			}
			else
			{
				System.out.println("Sorry, unknown command");
				return ;
			}
		}
		catch(Exception E)
		{
			E.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private ArrayList<HashMap<String, String>> readFromServer() 
	{
		try 
		{
			ArrayList<HashMap<String, String>> hm = (ArrayList<HashMap<String, String>>) serversockreaderForObjects.readObject();
			return hm;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
		return null;
	}
}
