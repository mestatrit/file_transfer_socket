package fileTransfer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import database.Database;

public class Client 
{
	private static BufferedReader br;
	private static Socket socket;
	private static BufferedReader sockreader;
	private static PrintWriter sockwriter;
	private static Timer timerForRechecking;
	private static int recheckinterval = 15; // seconds
	
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
					String command = "DELETE FILE " + file.getAbsolutePath();
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
		//Socket socket = connectToServer();
		showChoices();
	}

	private Socket connectToServer() 
	{
		System.out.println("Enter the IP address of the server: \n");
		try 
		{
			String servaddr = br.readLine();
			socket = new Socket(servaddr, 15123);
			sockreader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			sockwriter = new PrintWriter(socket.getOutputStream());
			System.out.println("Client socket has been created!!!");
			return socket;
		} 
		catch (IOException e) 
		{
			
			e.printStackTrace();
			return null;
		}
	}
	
	private void showChoices() 
	{
		System.out.println("Enter your choices:\n");
		System.out.println("1. Search for a file\n2. Share a file\n3. Send a message");
		try 
		{
			int choice = br.read() - '0';
			
			switch(choice)
			{
				case 1: searchForFile(); 
						break;
				case 2: shareFile();
						break;
				case 3:
						break;
				default:
						break;
			}
		} 
		catch (IOException e) 
		{
			System.out.println("Sorry, input error!!!");
			e.printStackTrace();
		}
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
			String command = "NEW FILE " + filepath;
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
			String command = "SEARCH FILE " + searchForThis;
			issueCommandToServer(command);
		} 
		catch (IOException e) 
		{
			System.out.println("Input reading error!!!");
			e.printStackTrace();
		}
	}

	private void issueCommandToServer(String command) 
	{
		String keyword = command.split(" ")[0];
		if(keyword.equals("ADD"))
		{
			System.out.println("New file is to be added to the server log");
		}
		else if(keyword.equals("SEARCH"))
		{
			System.out.println("Searching for a file in the server log");
		}
		else if(keyword.equals("DELETE"))
		{
			System.out.println("Deleting a file in the server log");
		}
		else
		{
			System.out.println("Sorry, unknown command");
		}
		//sockwriter.write(command);
	}
}
