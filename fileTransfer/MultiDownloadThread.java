package fileTransfer;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MultiDownloadThread extends Thread
{
	private int clientPort = 5002;
	private int size, MAX = 4096; // size of file
	private ArrayList<HashMap<String, String>> list; // mapping of ip addresses and filepaths on that system
	private File file; // file on my system
	private ScheduledExecutorService timer;
	private ArrayList<Integer> listOfPackets = new ArrayList<Integer> (); // stores the packets still needed to be assigned to individual downloading threads (MultiDownload)
	private ArrayList<MultiDownload> mdThreadsList; // stores the list of active threads downloading parts of file
	private ArrayList<HashMap<String, String>> nonActiveIP;
	
	public MultiDownloadThread(int size, ArrayList<HashMap<String, String>> list, File file)
	{
		this.size = size;
		this.list = list;
		this.file = file;
		if(!this.file.exists())
		{
			try 
			{
				this.file.createNewFile();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
				System.out.println("Can't create this file");
			}
		}
		int packets = this.size/MAX;
		for(int i = 0 ; i < packets ; i ++)
		{
			listOfPackets.add(i);
		}
	}
	
	public class check2 implements Runnable 
	{
		public void run() 
		{
			if(!listOfPackets.isEmpty())
			{
				for(HashMap<String, String> hm : nonActiveIP) // if a non active user becomes active assign tasks for them
				{
					String ip = hm.get("ip");
					String filepath = hm.get("filepath");
					try
					{
						Socket socket = new Socket(ip, clientPort);
						nonActiveIP.remove(hm);
						ArrayList<Integer> list = new ArrayList<Integer> ();
						list.addAll(listOfPackets.subList(0, Math.min(10, listOfPackets.size())));
						listOfPackets.removeAll(list);
						MultiDownload md = new MultiDownload(MAX, ip, filepath, socket, list);
						md.start();
						mdThreadsList.add(md);
					}
					catch(IOException ioe)
					{
						ioe.printStackTrace();
					}
				}
			}
			
			for(MultiDownload md : mdThreadsList) // check if any downloading thread becomes inactive ----> retrieve the lists of packets, they failed to download ----> add it to listOfPackets 
			{
				if(!md.isAlive())
				{
					ArrayList<Integer> remainingPackets = md.terminate();
					listOfPackets.addAll(remainingPackets);
					HashMap<String, String> hm = new HashMap<String, String> ();
					hm.put(md.getIP(), md.getfilepath());
					mdThreadsList.remove(md);
					nonActiveIP.add(hm);
				}
			}
		}
	}
	
	public void run()
	{
		int distribution = Math.max(listOfPackets.size()/list.size(), 1);
		int flag = 0;
		for(HashMap<String, String> hm : list)
		{
			if(flag == 1)
			{
				nonActiveIP.add(hm);
				continue;
			}
			String ip = hm.get("ip");
			String filepath = hm.get("filepath");
			try
			{
				Socket socket = new Socket(ip, clientPort);
				ArrayList<Integer> dist = new ArrayList<Integer> ();
				dist.addAll(listOfPackets.subList(0, distribution));
				if(dist.isEmpty())
				{
					// no more distribution is required...
					// add all the other IPs to non active list
					// break the loop
					flag = 1;
					socket.close();
					continue;
				}
				listOfPackets.removeAll(dist);
				MultiDownload md = new MultiDownload(MAX, ip, filepath, socket, dist);
				md.start();
				mdThreadsList.add(md);
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
				nonActiveIP.add(hm);
			}
		}
		
		timer = Executors.newScheduledThreadPool(1);
		timer.scheduleWithFixedDelay(new check2(), 0, 15, TimeUnit.SECONDS);
		
		while(!mdThreadsList.isEmpty() && !listOfPackets.isEmpty())
		{
			
		}
		
	}
}
