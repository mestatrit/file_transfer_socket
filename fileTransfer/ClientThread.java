package fileTransfer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientThread extends Thread
{
	private ServerSocket serverSocket;
	private int clientPort = 5002;
	private int backlogLength = 10;
	private HashMap<ConnectedClient, Socket> connectedUsers;
	private ScheduledExecutorService timer;
	private boolean running;
	
	public ClientThread()
	{
		running = true;
		try 
		{
			serverSocket = new ServerSocket(clientPort, backlogLength);
			connectedUsers = new HashMap<ConnectedClient, Socket> ();
			timer = Executors.newScheduledThreadPool(1);
			timer.scheduleWithFixedDelay(new check(), 5, 15, TimeUnit.SECONDS);
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public class check implements Runnable 
	{
		public void run() 
		{
			for(Map.Entry<ConnectedClient, Socket> entry : connectedUsers.entrySet())
			{
				ConnectedClient cc = entry.getKey();
				if(!cc.isAlive())
				{
					connectedUsers.remove(cc);
				}
			}
		}
	}
	
	public void run()
	{
		while(running)
		{
			Socket skt;
			try 
			{
				skt = serverSocket.accept();
				System.out.println("Connected...");
				ConnectedClient cc = new ConnectedClient(skt);
				cc.start();
				connectedUsers.put(cc,skt);
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	public void terminate()
	{
		running = false;
		try 
		{
			for(Map.Entry<ConnectedClient, Socket> entry : connectedUsers.entrySet())
			{
				ConnectedClient cc = entry.getKey();
				cc.terminate();
			}
			serverSocket.close();
			
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		System.out.println("I am closing");
	}
}
