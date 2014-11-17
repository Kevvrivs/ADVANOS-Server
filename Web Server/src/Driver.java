import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Driver {
	public static void main(String[] args) {
		
		try {
			ServerSocket server = new ServerSocket(8080);
			ThreadPool pool = new ThreadPool(16);
			System.out.println("Server has started");
			System.out.println("Waiting for connections");
			
			while(true){
				Socket s = server.accept();
				
				//add socket to queue
				pool.addTask(s);
				//ServerThread temp = new ServerThread(s);
				//System.out.println("Connection Established");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
