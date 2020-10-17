package ufc.crateus.server;

import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {
	
	public static void main (String args[] ) throws Exception{
		int port = 3000;
		
		@SuppressWarnings("resource")
		ServerSocket socket = new ServerSocket(port);
		
		while(true) {
			Socket conne = socket.accept();
			
			HTTPRequest request = new HTTPRequest(conne);
	
			
			Thread thread = new Thread(request);
			thread.start();
		}
	}
}
