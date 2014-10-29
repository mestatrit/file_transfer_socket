package fileTransfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

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
				bw.write(1);
				bw.flush();
				return ;
			}
			else
			{
				bw.write(0);
				bw.flush();
			}
			FileInputStream fis = new FileInputStream(path);
			FileChannel fc = fis.getChannel();
			long size = file.length();
			int packets = (int)Math.ceil((float)size/(float)MAX);
			System.out.println("Packets: " + packets);
			bw.writeInt(packets);
			bw.flush();
			int i = 0;
			while(i < packets)
			{
				int packet = br.readInt();
				System.out.println("packet received: " + packet + " byte buffer length: " + (int) Math.min(MAX, size-(packet*MAX)) + "total packets: " + packets);
				ByteBuffer buffer = ByteBuffer.allocate((int) Math.min(MAX, size-(packet*MAX)));
				fc.read(buffer, packet*MAX);
				System.out.println("Length written: " + buffer.capacity());
				byte[] buf = buffer.array();
				bw.write(buf);
				bw.flush();
				bw.writeInt((int) Math.min(MAX, size-(packet*MAX)));
				bw.flush();
				i ++;
			}
			
			System.out.println("Writing...");
			
			String msg = br.readUTF();
			System.out.println("Message is : " + msg);
			
			fis.close();
			fc.close();
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
