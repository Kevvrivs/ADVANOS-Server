import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;


public class ServerThread extends Thread{
	private BlockingQueue<Socket> socketQueue;
	private Socket connection;
	private BufferedReader input;
	private PrintStream output;
	
	
	public ServerThread(BlockingQueue<Socket> queue){
		this.socketQueue = queue;
		//this.connection = s;
		this.start();
	}
	
	public void run(){
		//keep the thread running so it can get more "work" from queue 
		while(true){
			
			//try to get "work" from queue (i.e. socket)
			try {
				this.connection = socketQueue.take();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			try {
				//Initialize stream reader and writer
				input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				output = new PrintStream(new BufferedOutputStream(connection.getOutputStream()));
				
				//read client request
				String request = input.readLine();
				
				//System.out.println("Request Received: " + request);
				
				//process request
				processRequest(request);
				
				//close input and output stream
				output.flush();
				input.close();
				output.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				//close connection regardless of the outcome of the try-catch block
				try {
					connection.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void processRequest(String s){
		//split request string
		String[] temp = s.split(" ");
		
		//determine type of request
		switch(temp[0]){
			//if GET request, call getFilename function 
			case "GET":
				String filename = getFilename(temp[1]);
				
				//try to send file after it has been identified
				try {
					sendFile(filename);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			//if not a GET request
			default:
				output.println("Not a valid HTTP request");
				break;
		}
	}
	
	private String getFilename(String s){
		//if no filename specified, default is index.html
		if(s.endsWith("/")) 
			s += "index.html";

		// REMOVE EXTRA "/" FROM THE FILENAME
		while(s.indexOf("/")==0) 
			s = s.substring(1);

		// REPLACE "\" WITH "/"
		s = s.replace('/', File.separator.charAt(0)); 
		
		return s;
	}
	
	private void sendFile(String filename) throws IOException{
		FileInputStream fileStream;
		
		try {
			fileStream = new FileInputStream(filename);
			byte[] a = new byte[4096];
			int n;
			
			//some additional stuff
			String response = "HTTP/1.1 200 OK\r\n";
			response+="Date: Fri, 14 Nov 2014 18:48:26 GMT\r\n";
			response+="Server: Apache/2.4.7 (Win32) OpenSSL/1.0.1e PHP/5.5.6\r\n";
			response+="Last-Modified: Thu, 13 Nov 2014 10:19:28 GMT\r\n";
			response+="ETag: \"fff79-507bad8e8cc00\"\r\n";
			response+="Accept-Ranges: bytes\r\n";
			response+="Content-Length: 1048441\r\n";
			response+="Content-Type: text/html\r\n\r\n";
			
			output.print(response);
			
			//write file to output stream
			while((n = fileStream.read(a)) > 0) {
					output.write(a, 0, n);
			}
		} catch (FileNotFoundException e) {
			//if file not found
			output.println("HTTP/1.1 404 Not Found\r\n" + "Content-type: text/html\r\n\r\n"+ "<html><head></head><body>FILE NOT FOUND!</body></html>\n");
		}
	}
}
