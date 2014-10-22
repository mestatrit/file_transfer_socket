package fileTransfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ConnectedClient extends Thread 
{
	private static int id = 0;
	private int thisID, MAX;
	private Socket socket;
	private String path;
	private BufferedReader br;
	private BufferedOutputStream bw;
	
	public ConnectedClient(Socket skt) 
	{
		socket = skt;
		thisID = id;
		id ++;
		System.out.println("Created a thread with id: " + thisID);
		MAX = 1024;
		try 
		{
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			bw = new BufferedOutputStream(socket.getOutputStream());
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
	}

	public void run() 
	{
		try 
		{
			bw.write(MAX);
			bw.flush();
			path = br.readLine();
			File file = new File(path);
			if(!file.exists() || !file.isFile())
			{
				bw.write(1);
				bw.flush();
				return ;
			}
			byte[] buffer = new byte[MAX];
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			long size = file.length();
			int packets = (int)Math.ceil(size/MAX);
			bw.write(packets);
			bw.flush();
			int i = 0;
			while(i < packets)
			{
				bis.read(buffer);
				bw.write(buffer);
				bw.flush();
				i ++;
			}
			bis.close();
			terminate();
			socket.close();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public void terminate()
	{
		try 
		{
			br.close();
			bw.close();
			socket.close();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		System.out.println("Terminating the thread: " + thisID);
	}
}
