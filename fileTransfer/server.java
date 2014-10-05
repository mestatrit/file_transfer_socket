package fileTransfer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;


public class server 
{
	public static void main(String[] args) throws IOException
	{
		ServerSocket server1 = new ServerSocket(5001);
		Socket server2 = server1.accept();
		System.out.println("Connection established!!!");
		BufferedReader in = new BufferedReader(new InputStreamReader(server2.getInputStream()));
		ObjectOutputStream oos = new ObjectOutputStream(server2.getOutputStream());
		String inputLine = in.readLine();
		System.out.println("blah blah: " + inputLine);
		System.out.println("Object output stream has been created...");
		ArrayList<HashMap<String, String>> obj = new ArrayList<HashMap<String,String>> ();
		HashMap<String, String> hm = new HashMap<String, String> ();
		hm.put("IP", "127.0.0.1");
		hm.put("filename", "blah");
		hm.put("filesize", "100");
		hm.put("filetype", "eureka");
		obj.add(hm);
		HashMap<String, String> hm1 = new HashMap<String, String> ();
		hm1.put("IP", "127.0.0.2");
		hm1.put("filename", "bla11h");
		hm1.put("filesize", "10011");
		hm1.put("filetype", "eure111ka");
		obj.add(hm1);
		oos.writeObject(obj);
		oos.flush();
		server1.close();
	}
}
