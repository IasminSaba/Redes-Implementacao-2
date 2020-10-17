package ufc.crateus.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;

public class HTTPRequest implements Runnable {

	final static String CRLF = "\r\n";
	Socket socket;
	
	
	
	public HTTPRequest(Socket socket) throws Exception
	{
		this.socket = socket;
	}
	
	@Override
	public void run() {
		
		try {
			processRequest();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	
	  private void processRequest() throws Exception {
	        
	        InputStream is = socket.getInputStream();
	        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
	        BufferedReader br = new BufferedReader(new InputStreamReader(is));

	       
	        String requestLine = br.readLine(); //linha request
	        
	        StringTokenizer tokens = new StringTokenizer(requestLine);
	        tokens.nextToken(); 
	        String file= tokens.nextToken();

	        file = "." + file; //Arquivo no diretório	        
	        FileInputStream f = null;
	        boolean fileExists = true;
	        try {
	            f = new FileInputStream(file);
	        } catch (FileNotFoundException e) {
	            fileExists = false;
	        }

	        
	        System.out.println("Nova requisição.");
	        System.out.println(requestLine);
	        String headerLine = null;
	        while ((headerLine = br.readLine()).length() != 0) {
	            System.out.println(headerLine);
	        }

	        String status = null;
	        String content= null;
	        String body = null;
	        if (fileExists) {
	            status= "HTTP/1.0 200 " + CRLF;
	            content= "Content: "
	                    + content(file) + CRLF;
	        } else {
	            status= "HTTP/1.0 404 Not Found" + CRLF;
	            content= "Content-Type: index/html" + CRLF;
	            body= "<HTML>"
	                    + "<HEAD><TITLE>Not Found</TITLE></HEAD>"
	                    + "<body>\n"
	                    + "<div align=\"center\"><p><img alt=\"notFound\" src=\"../notFound.png\"\n"
	                    + "width=\"350\"></p></div>\n"
	                    + "</body></HTML>";
	        }
	        

	        os.writeBytes(status);
	        os.writeBytes(content);

	        //Indicar o fim de header
	        os.writeBytes(CRLF);

	        //Se o arquivo existir
	        if (fileExists) {
	            sendBytes(f, os);
	            f.close();
	        } else {
	            os.writeBytes(body);
	        }

	       
	        os.close();
	        br.close();
	        socket.close();
	    }

	    private static void sendBytes(FileInputStream fis,
	            OutputStream os) throws Exception {
	      
	        byte[] buffer = new byte[1024];
	        int bytes = 0;

	        while ((bytes = fis.read(buffer)) != -1) {
	            os.write(buffer, 0, bytes);
	        }
	    }

	    private static String content(String file) {
	        if (file.endsWith(".html")) {
	            return "index/html";
	        }
	        
	        return "application/octet-stream";
	    }
}









