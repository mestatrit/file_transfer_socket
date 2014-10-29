package fileTransfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.io.RandomAccessFile;

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
			MAX = br.readInt();
			path = br.readUTF();
			File file = new File(path);
			if(!file.exists() || !file.isFile())
			{
				bw.writeInt(1);
				bw.flush();
				return ;
			}
			else
			{
				bw.writeInt(0);
				bw.flush();
			}
			long size = file.length();
			int packets = (int)Math.ceil((float)size/(float)MAX);
			System.out.println("Packets: " + packets);
			bw.writeInt(packets);
			bw.flush();
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			int i = 0;
			while(i < packets)
			{
				int packet = br.readInt();
				int len = (int) Math.min(MAX, size-(packet*MAX));
				System.out.println("packet received: " + packet + " length to be read: " + len + "total packets: " + packets);
				byte[] buffer = new byte[len];
				raf.seek(packet*MAX);
				raf.read(buffer);
				System.out.println("Now length is being written..." + len);
				bw.writeInt(len);
				bw.flush();
				System.out.println("Length written: " + len);
				bw.write(buffer);
				bw.flush();
				i ++;
			}
			
			System.out.println("Writing...");
			
			String msg = br.readUTF();
			System.out.println("Message is : " + msg);
			
			//fis.close();
			//fc.close();
			raf.close();
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
