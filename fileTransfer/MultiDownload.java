package fileTransfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MultiDownload extends Thread
{
	private int MAX;
	private String IP;
	private String filepath;
	private ArrayList<Integer> listOfPackets;
	
	private Socket socket;
	private DataInputStream clientSocketReader;
	private DataOutputStream clientSocketWriter;
	
	private boolean running = true;
	
	public MultiDownload(int MAX, String ip, String file, Socket socket, ArrayList<Integer> list)
	{
		try 
		{
			this.MAX = MAX;
			this.IP = ip;
			this.filepath = file;
			this.listOfPackets = list;
			this.socket = socket;
			clientSocketReader = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			clientSocketWriter = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		}
		catch (UnknownHostException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public String getIP()
	{
		return IP;
	}

	public String getfilepath() 
	{
		return filepath;
	}
	
	public void run()
	{
		// download the file
		while(running)
		{
			try
			{
				clientSocketWriter.writeInt(MAX);
				clientSocketWriter.flush();
				clientSocketWriter.writeUTF(filepath);
				clientSocketWriter.flush();
				int flag = clientSocketReader.readInt();
				if(flag == 1)
				{
					// some error
					return ;
				}
			}
			catch(Exception E)
			{
				
			}
		}
	}
	
	public ArrayList<Integer> terminate()
	{
		try
		{
			running = false;
			clientSocketReader.close();
			clientSocketWriter.close();
			socket.close();
			return listOfPackets;
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		return null;
	}

}
