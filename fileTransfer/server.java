package fileTransfer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class server 
{
	public static void main(String[] args) throws IOException
	{
		ServerSocket server1 = new ServerSocket(5002);
		Socket server2 = server1.accept();
		BufferedReader in = new BufferedReader(new InputStreamReader(server2.getInputStream()));
		String inputLine = in.readLine();
		System.out.println(inputLine);
		server1.close();
	}
}
