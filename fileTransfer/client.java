package fileTransfer;

import java.net.*; 
import java.io.*; 
import org.json.JSONObject;

public class client 
{
	public static void main (String [] args ) throws IOException 
	{
		Socket client1 = new Socket("127.0.0.1", 5002);
		JSONObject obj = new JSONObject();
		obj.put("name", "foo");
      	obj.put("num", new Integer(100));
	    obj.put("balance", new Double(1000.21));
	    obj.put("is_vip", new Boolean(true));
	    System.out.println(obj);
	    PrintWriter out = new PrintWriter(client1.getOutputStream());
		out.print(obj);
		out.flush();
		out.close();
		client1.close();
	}
} 

