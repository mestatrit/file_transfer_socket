package fileTransfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

public class MultiDownload extends Thread
{
	private int MAX;
	private String IP;
	private String filepath;
	private ArrayList<Integer> listOfPackets;
	private File myfile;
	private Socket socket;
	private DataInputStream clientSocketReader;
	private DataOutputStream clientSocketWriter;
	
	private boolean running = true;
	
	public MultiDownload(int MAX, String ip, String file, Socket socket, ArrayList<Integer> list, File myfile)
	{
		try 
		{
			this.MAX = MAX;
			this.IP = ip;
			this.filepath = file;
			this.myfile = myfile;
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
				clientSocketReader.readInt();
				RandomAccessFile raf = new RandomAccessFile(myfile, "rw");
				for(Iterator<Integer> iterator = listOfPackets.iterator() ; iterator.hasNext() ; )
				{
					int i = iterator.next();
					System.out.println("Requesting packet: " + i);
					clientSocketWriter.writeInt(i);
					clientSocketWriter.flush();
					int len = clientSocketReader.readInt();
					byte[] buffer = new byte[len];
					clientSocketReader.read(buffer);
					System.out.println("RAF writing at " + i);
					raf.seek(i*MAX);
					raf.write(buffer, 0, len);
					iterator.remove();
					if(!listOfPackets.isEmpty())
					{
						clientSocketWriter.writeInt(1);
						clientSocketWriter.flush();
					}
					else
					{
						clientSocketWriter.writeInt(0);
						clientSocketWriter.flush();
					}					
				}
				clientSocketWriter.writeUTF("DONE!!!");
				clientSocketWriter.flush();
				raf.close();
				clientSocketReader.close();
				clientSocketWriter.close();
				socket.close();
				return ;
			}
			catch(Exception E)
			{
				E.printStackTrace();
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
