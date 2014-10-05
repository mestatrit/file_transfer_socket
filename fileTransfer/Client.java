package fileTransfer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;
import java.util.ArrayList;
import java.util.HashMap;

import database.Database;

public class Client 
{
	private static BufferedReader br;
	private static Socket socket;
	private static BufferedReader sockreader;
	private static PrintWriter sockwriter;
	private static String logFileName = "log.txt";
	private static BufferedReader brLog;
	private static PrintWriter pwLog;
	
	private File fileClient; 
	
	private ArrayList<File> toBeSynced;
	
	public Client()
	{
		fileClient = new File(logFileName);
		try 
		{
			fileClient.createNewFile();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		try 
		{
			brLog = new BufferedReader(new FileReader(fileClient));
			pwLog = new PrintWriter(new BufferedWriter(new FileWriter(fileClient, true)));
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		try
		{
			Database.createDB();
			Database.createTable();
		}
		catch(Exception E)
		{
			E.printStackTrace();
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
			brLog.close();
			brLog = new BufferedReader(new FileReader(fileClient)); // reinitialise to point to starting of the file
			toBeSynced = new ArrayList<File> ();
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
				//File[] files = file.listFiles(); 
				//for(File file1 : files)
					//System.out.println(file1.getName());
				// add all the files in this directory to the sharing list
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
		ArrayList<File> names = new ArrayList<File>();
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
		/*try
		{
			String line = brLog.readLine();
			while(line != null)
			{
				if(file.getAbsolutePath().compareTo(line) == 0)
				{
					return; // file already in log
				}
				brLog.readLine();
				brLog.read();
				line = brLog.readLine();
			}
			if(line == null)
			{
				pwLog.println(file.getAbsolutePath());
				pwLog.println(file.length());
				pwLog.flush();
				toBeSynced.add(file);
			}
		}
		catch(Exception E)
		{
			System.out.println("abc");
			E.printStackTrace();
		}*/
		String filepath = file.getAbsolutePath();
		Integer size = (int) file.length();
		String type = null;
		//String separator = File.separator;
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
			System.out.println("File already hashed!!!");
		}
		else
		{
			Database.insertIntoTable(filepath, size, type);
			reSyncWithServerLog();
		}
	}

	private void reSyncWithServerLog() 
	{
		// TODO: Add code to resync newly added files with the server log
	}
	
	private void recheckLog()
	{
		// rechecking of the log file for checking the validity of the files
		BufferedReader br1; 
		try 
		{
			br1 = new BufferedReader(new FileReader(fileClient)); // reinitialise to point to the starting of the file
			String line = br1.readLine();
			while(line != null)
			{
				File file = new File(line);
				if(!file.exists())
				{
					deleteFileFromLog(file);
				}
								
				int size = Integer.parseInt(br1.readLine());
				if(file.exists() && size != file.length())
				{
					
				}
				br1.read();
				line = br.readLine();
			}
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	private void deleteFileFromLog(File file) {
		// TODO Auto-generated method stub
		
	}

	private void searchForFile() 
	{
		System.out.println("Enter the filename to search for:");
		try 
		{
			String searchForThis = br.readLine();
			System.out.println("Your command: search for " + searchForThis + " has been issued");
			String command = "search " + searchForThis;
			issueCommand(command);
		} 
		catch (IOException e) 
		{
			System.out.println("Input reading error!!!");
			e.printStackTrace();
		}
	}

	private void issueCommand(String command) 
	{
		sockwriter.write(command);
	}
}
