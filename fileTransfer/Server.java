package fileTransfer;

import java.io.IOException;
import java.net.ServerSocket;

public class Server 
{
	private static ServerSocket serverSocket; 
	private int serverPORT = 5001;
	private int backlogLength = 10;
	
	public Server()
	{
		try 
		{
			serverSocket = new ServerSocket(serverPORT, backlogLength);
			
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		Server server = new Server();
		server.execute();	
	}

	private void execute() 
	{
		while(true);
	}
}
