package fileTransfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

public class ConnectedClient extends Thread 
{
	private static int id = 0;
	private int thisID, MAX;
	private Socket socket;
	private String path;
	private DataInputStream br;
	private DataOutputStream bw;
	
	public ConnectedClient(Socket skt) 
	{
		socket = skt;
		thisID = id;
		id ++;
		System.out.println("Created a thread with id: " + thisID);
		MAX = 1024;
		try 
		{
			br = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			bw = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
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
			bw.writeInt(MAX);
			bw.flush();
			path = br.readUTF();
			File file = new File(path);
			if(!file.exists() || !file.isFile())
			{
				bw.write(1);
				bw.flush();
				return ;
			}
			else
			{
				bw.write(0);
				bw.flush();
			}
			byte[] buffer = new byte[MAX];
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			long size = file.length();
			int packets = (int)Math.ceil((float)size/(float)MAX);
			bw.writeInt(packets);
			bw.flush();
			int i = 0;
			while(i < packets-1)
			{
				bis.read(buffer);
				System.out.println("Length written: " + buffer.length);
				bw.write(buffer);
				bw.flush();
				bw.writeInt(MAX);
				bw.flush();
				i ++;
			}
			
			System.out.println("Writing...");
			
			bis.read(buffer);
			bw.write(buffer);
			bw.flush();
			int length = (int)size - (MAX*(packets-1));
			bw.writeInt(length);
			bw.flush();
			
			String msg = br.readUTF();
			System.out.println("Message is : " + msg);
			
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
