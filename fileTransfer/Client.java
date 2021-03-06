package fileTransfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import org.apache.commons.validator.routines.InetAddressValidator;
import database.Database_client;

public class Client 
{
	private static BufferedReader br;
	private static ObjectInputStream serversockreaderForObjects;
	private static ObjectOutputStream serversockwriterForObjects;
	private static ScheduledExecutorService timerForRechecking;
	private ClientThread ct;
	private MultiDownloadThread mdt;
	
	private String username;

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
					command.put("arg1", username);
					
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

	@SuppressWarnings("unchecked")
	private void execute() 
	{
		br = new BufferedReader(new InputStreamReader(System.in));
		boolean flag = true;
		do
		{
			try 
			{
				System.out.println("Specify the download directory: ");
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
				
				System.out.println("Are you a registered user (y/n)...");
				char ch = (char)br.read();
				br.readLine();
				if(ch == 'y' || ch == 'Y')
				{
					// yes, he is registered user...
					System.out.println("Enter your username...");
					String username = br.readLine();
					System.out.println("Enter your password...");
					String password = br.readLine();
					
					HashMap<String, String> command = new HashMap<String, String> ();
					command.put("command", "LOGIN");
					command.put("username", username);
					command.put("password", password);
					serversockwriterForObjects.writeObject(command);
					serversockwriterForObjects.flush();
					ArrayList<HashMap<String, String>> cmd = (ArrayList<HashMap<String, String>>)serversockreaderForObjects.readObject();
					if(cmd.isEmpty())
					{
						System.out.println("Some error... Try again... Aborting...");
						return ;
					}
					if(cmd.get(0).get("response").equals("SUCCESS"))
					{
						System.out.println("Successfully logged in...");
						this.username = username;
						System.out.println(this.username);						
					}
				}
				else if(ch == 'n' || ch == 'N')
				{
					// You have to register
					System.out.println("Enter your username...");
					String uname = br.readLine();
					System.out.println("Enter your password");
					String pswd = br.readLine();
					
					HashMap<String, String> command = new HashMap<String, String> ();
					command.put("command", "REGISTER");
					command.put("username", uname);
					command.put("password", pswd);
					String ip = serverSocket.getLocalAddress().toString();
					command.put("IPaddr", ip);
					serversockwriterForObjects.writeObject(command);
					serversockwriterForObjects.flush();
					ArrayList<HashMap<String, String>> cmd = (ArrayList<HashMap<String, String>>)serversockreaderForObjects.readObject();
					if(cmd.isEmpty())
					{
						System.out.println("Some error... Try again... Aborting...");
						return ;
					}
					if(cmd.get(0).get("response").equals("SUCCESS"))
					{
						System.out.println("Successfully registered...");
						this.username = uname;
					}
				}
				else
				{
					System.out.println("Wrong output...");
					return ;
				}
				
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
		System.out.println("Enter the IP address of the server:");
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
				System.out.println("1. Search for a file\n2. Share a file\n3. Unshare a file\n4. Get all online users\n5. Get a user's file list\n6. Download a file\n7. Use tracker to download a file\n8. Exit");
				
				int choice = br.read() - '0';
				br.read();

				switch(choice)
				{
					case 1: searchForFile(); 
							break;
					case 2: shareFile();
							break;
					case 3: unshare();
							break;
					case 4:	getAllUsers();
							break;
					case 5:	getFileList();
							break;	
					case 6:	downloadFile();
							break;
					case 7: useTracker();
							break;
					case 8: exitWithGrace();
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

	private void useTracker() 
	{
		try 
		{
			String file;
			System.out.println("Enter the path to the json file:");
			file = br.readLine();
			FileReader fr = new FileReader(file);
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(fr);
			
			JSONArray mapping = (JSONArray) jsonObject.get("mapping");
			String size = (String) jsonObject.get("size");
			
			int len = Integer.parseInt(size);
			
			Iterator i = mapping.iterator();
			ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>> ();
			while(i.hasNext())
			{
				JSONObject obj = (JSONObject) i.next();
				System.out.println("IP: " + obj.get("ip") + "\nFilepath is: " + obj.get("filepath") + "\nSize is: " + len);
				HashMap<String,String> hm = new HashMap<String, String> ();
				hm.put("ip", (String)obj.get("ip"));
				hm.put("filepath", (String)obj.get("filepath"));
				list.add(hm);
			}
			String[] splitPeerPath = list.get(0).get("filepath").split("/");
			String myFileName = splitPeerPath[splitPeerPath.length-1];
			File myFile = new File(downloadDir + "/" + myFileName);
			mdt = new MultiDownloadThread(len, list, myFile);
			mdt.start();
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		catch (ParseException e) 
		{
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
		System.out.println("Enter username, whose list is required:");
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

	private void unshare() throws IOException 
	{
		// send the command to the server to broadcast the message
		System.out.println("Enter the file (whole path), which you want to unshare");
		String file = br.readLine();
		ArrayList<HashMap<String, String>> hm = Database_client.selectFromTable(file);
		if(hm.isEmpty())
		{
			System.out.println("Sorry, the file is not currently hashed...");
			return ;
		}
		int id = Integer.parseInt(hm.get(0).get("identity"));
		System.out.println("Deleting from local database...");
		Database_client.deleteFromTable(id);
		HashMap<String, String> command = new HashMap<String, String> ();
		command.put("command", "DELETE");
		command.put("arg0", file);
		command.put("arg1", this.username);
		
		System.out.println("Deleting from server database...");
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
			System.out.println(this.username);
			command.put("arg4", this.username);
			
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
		 *	4. USERS - get all the currently online users 
		 *	5. LIST - get the file list of a client with a particular ip USAGE: LIST <IP address>
		 *	6. GETFILE - receive the actual file from the user USAGE : GETFILE <IP address> <full filename>
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
					System.out.println("IP address    File Name		File Path    Size    Type  ");
					for(HashMap<String, String> hm:result)
					{
						System.out.println(hm.get("IPaddr") + " | " + hm.get("filename") + " | " + hm.get("filepath") + " | " + hm.get("size") + " | " + hm.get("type"));
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
					System.out.println("IP addresses and usernames of currently online users are: ");
					
					int i = 1;
					for(HashMap<String, String> hm:result)
					{
						System.out.println(i + " ----> " + hm.get("response") + " ----> " + hm.get("arg0"));
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
				 *  2. Send the max size of a packet (MAXSIZE) 
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
				
				clientSocket = new Socket(clientIP, clientPORT);
				DataInputStream clientSocketReader = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
				DataOutputStream clientSocketWriter = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
				
				int MAXSIZE = 4096;
				clientSocketWriter.writeInt(MAXSIZE);
				clientSocketWriter.flush();
				
				clientSocketWriter.writeUTF(peerFilePath);
				clientSocketWriter.flush();
				
				int flag = clientSocketReader.readInt();
				if(flag == 1)
				{
					System.out.println("Some error occured!!!!");
					myFile.delete();
					clientSocketReader.close();
					clientSocketWriter.close();
					clientSocket.close();
					return ;
				}
				
				int transmissions = clientSocketReader.readInt();
				
				int i = 0;
				RandomAccessFile raf = new RandomAccessFile(myFile, "rw");
				while(i < transmissions)
				{
					System.out.println("Reuesting packet: " + i);
					clientSocketWriter.writeInt(i);
					clientSocketWriter.flush();
					int length = clientSocketReader.readInt();
					byte buffer[] = new byte[MAXSIZE];
					clientSocketReader.read(buffer);
					raf.seek(i*MAXSIZE);
					raf.write(buffer, 0, length);
					System.out.println("Received " + i);
					int p = 1;
					if(i == transmissions-1)
					{
						p = 0;
					}
					clientSocketWriter.writeInt(p);
					clientSocketWriter.flush();
					i ++;
				}
				clientSocketWriter.writeUTF("DONE");
				clientSocketWriter.flush();
				raf.close();
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
