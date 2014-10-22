package fileTransfer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import database.Database_server;

public class Server 
{
	private static ServerSocket serverSocket; 
	
	private static ScheduledExecutorService timer;
	
	private int serverPORT = 5003;
	private int backlogLength = 10;
	
	protected HashMap<ServerThread, Socket> connectedUsers = new HashMap<ServerThread, Socket> ();
	protected int index = -1;
	protected int recheckinterval = 15;
	
	public Server()
	{
		try 
		{
			serverSocket = new ServerSocket(serverPORT, backlogLength);
			timer = Executors.newScheduledThreadPool(1);
			timer.scheduleWithFixedDelay(new checkClient(), 5, 15, TimeUnit.SECONDS);
			
			Database_server.createDB();
			Database_server.createTable();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public class checkClient implements Runnable
	{
		public void run() 
		{
			for(Map.Entry<ServerThread, Socket> entry : connectedUsers.entrySet())
			{
				ServerThread st = entry.getKey();
				Socket sock = entry.getValue();
				if(System.currentTimeMillis() - st.getTime() > 30000)
				{
					connectedUsers.remove(st);
					st.terminate();
				}
			}
		}
		
	}
	
	public static void main(String[] args)
	{
		Server server = new Server();
		server.execute();	
	}

	private void execute() 
	{
		while(true)
		{
			try 
			{
				Socket socket = serverSocket.accept();
				System.out.println("Server connected!!!");
				//System.out.println("remote address: " + connectedUsers[index]); 
				ServerThread st = new ServerThread(socket);
				st.start();
				connectedUsers.put(st, socket);
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
}
