import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;

public class ProcessMonitor implements Runnable
{
	private HashMap curProcesses;
	
	public static void main(String[] args)
	{
		ProcessMonitor myMonitor = new ProcessMonitor();
		Thread curThread = new Thread(myMonitor);
		curThread.start();
	}
	
	public ProcessMonitor()
	{
		
	}

	@Override
	public void run()
	{
		curProcesses = new HashMap();
		boolean first = true;
		while(true)
		{
			HashMap nextMap = new HashMap();
			HashMap clonedCur = (HashMap) curProcesses.clone();
			try
			{
				String line;
				int lineNum = 0;
				Process p = Runtime.getRuntime().exec("ps -efaux");
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String delims = "[ ]+";
				String[] colNames = null;
				while ((line = input.readLine()) != null)
				{
					//System.out.println(lineNum);
				    //System.out.println(line); //<-- Parse data here.
				    if(lineNum != 0)
				    {
				    	//System.out.println(lineNum);
				    	HashMap keyMap = new HashMap();
				    	int x=0;
				    	int z = 0;
				    	for(int y=0; y<10; y++)
				    	{
					    	String tmpString = "";
					    	while(line.charAt(x) != ' ')
					    	{
					    		tmpString += line.charAt(x);
					    		x++;
					    	}
					    	while(line.charAt(x) == ' ')
					    	{
					    		x++;
					    	}
					    	keyMap.put(colNames[y], tmpString);
					    	//System.out.println(tmpString);
					    	z = y+1;
				    	}
				    	String lastString = "";
				    	while(x<line.length())
				    	{
				    		lastString += line.charAt(x);
				    		x++;
				    	}
				    	lastString = lastString.trim();
				    	keyMap.put(colNames[z], lastString);
				    	//System.out.println(keyMap);
				    	nextMap.put(keyMap, null);
				    }
				    else
				    {
				    	colNames = line.split(delims);
				    }
				    lineNum++;
				}
				if(first)
				{
					first = false;
				}
				Iterator iter = nextMap.entrySet().iterator();
				HashMap toSend = new HashMap();
				while(iter.hasNext())
				{
					Entry pair = (Entry) iter.next();
					if(clonedCur.containsKey(pair.getKey()))
					{
						clonedCur.remove(pair.getKey());
					}
					else
					{
						//System.out.println("New: "+pair.getKey());
						curProcesses.put(pair.getKey(), null);
						HashMap tmpMap = (HashMap) ((HashMap)pair.getKey()).clone();
						tmpMap.put("statusChange", "start");
						toSend.put(tmpMap, null);
					}
				}
				iter = clonedCur.entrySet().iterator();
				while(iter.hasNext())
				{
					Entry pair = (Entry) iter.next();
					//System.out.println("Old: "+pair.getKey());
					HashMap tmpMap = (HashMap) ((HashMap)pair.getKey()).clone();
					tmpMap.put("statusChange", "end");
					toSend.put(tmpMap, null);
				}
				//System.out.println(toSend);
				input.close();
				//URL url = new URL("http://processlogging:8080/ProcessMonitorServer/ProcessMonitorServer");
				URL url = new URL("http://localhost:8080/ProcessMonitorServer/ProcessMonitorServer");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.setRequestMethod("PUT");
				//conn.getOutputStream().write(toSend.size());
				//OutputStreamWriter numWriter = new OutputStreamWriter(conn.getOutputStream());
				//numWriter.write(URLEncoder.encode(((Integer)toSend.size()).toString(), "utf-8"));
				//numWriter.write(toSend.size());
				//numWriter.flush();
				//Integer numToSend = toSend.size();
				System.out.println("Sending "+toSend.size());
				ObjectOutputStream objOut = new ObjectOutputStream(conn.getOutputStream());
				Integer toSendInt = toSend.size();
				objOut.writeObject(toSendInt);
				Iterator nextIter = toSend.entrySet().iterator();
				while(nextIter.hasNext())
				{
					Entry pair = (Entry) nextIter.next();
					objOut.writeObject(pair.getKey());
				}
				objOut.flush();
				
				Scanner s = new Scanner(conn.getInputStream()).useDelimiter("\\A");
				String result = s.hasNext() ? s.next() : "";
				System.out.println(result);
				//s = new Scanner(conn.getErrorStream()).useDelimiter("\\A");
				//result = s.hasNext() ? s.next() : "";
				//System.out.println(result);
				
				Thread.currentThread().sleep(100000);
			}
			catch(Exception err)
			{
				err.printStackTrace();
				curProcesses = new HashMap();
				try
				{
					Thread.currentThread().sleep(100000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				continue;
			}
		}
	}
	
	public ArrayList readProcesses()
	{
		return null;
		
	}

}
