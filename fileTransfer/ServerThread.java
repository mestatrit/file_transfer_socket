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
		//TODO: act on client command
	}
}
