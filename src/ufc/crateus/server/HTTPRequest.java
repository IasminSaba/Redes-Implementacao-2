package ufc.crateus.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;

public class HTTPRequest implements Runnable {

	final static String CRLF = "\r\n";
	Socket socket;

	public HTTPRequest(Socket socket) throws Exception {
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
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		String requestLine = br.readLine(); // linha request

		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken();
		String file = tokens.nextToken();

		System.out.println("Nova requisicao...");
		System.out.println(requestLine);
		String headerLine = null;
		while ((headerLine = br.readLine()).length() != 0) {
			System.out.println(headerLine);
		}

		getResourse(file, br);

	}

	private void getResourse(String file, BufferedReader br) throws Exception {
		
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		boolean fileExists = true;
		FileInputStream f = null;
		
		file = "." + file;

		try {
			f = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			fileExists = false;
		}
		
		String status = null;
		String content = null;
		String body = null;
		
		if(hasParams(file)) {
			String res[] =  file.split("\\?");
			String params[] = res[1].split("&");
			
			if(res[0].equals("./msg")) {
				status = "HTTP/1.0 404 Not Found" + CRLF;
				content = "Content-Type: text/html" + CRLF;
				body = codeHtmlWithParams("." + File.separator + "resposta.html", params[0], params[1]);
			}
		} else {
			
			if (fileExists && file != "./error.html") {
				status = "HTTP/1.0 200 OK" + CRLF;
				content = "Content: " + getContent(file) + CRLF;
			} else {
				status = "HTTP/1.0 404 Not Found" + CRLF;
				content = "Content-Type: text/html" + CRLF;
				body = codeHtml("." + File.separator + "error.html");
			}
		}

		os.writeBytes(status);
		os.writeBytes(content);

		// Indicar o fim de header
		os.writeBytes(CRLF);

		// Se o arquivo existir
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

	private String codeHtmlWithParams(String file, String ... str) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String linha, result = "";
		while((linha = br.readLine()) != null) {
			if(linha.contains("<div>")) {
				result += linha + "\n";
				for (String param : str) {
					String values[] = param.split("=");
					result += "<h3>" + values[0] + "</h2>";
					result += "<p class=\"h5\">" + values[1].
							replaceAll("\\+", " "). // espaco
							replaceAll("%2C", ",") // virgula
							+ "</p>";
				}
			} else {
				result += linha + "\n";
			}
		}
			
		br.close();
		return result;
	}

	private boolean hasParams(String file) {
		String res[] =  file.split("\\?");
		
		if(res.length > 1) return true;
		return false;
	}

	private String codeHtml(String file) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String linha, result = "";
		while((linha = br.readLine()) != null)
			result += linha + "\n";
		br.close();
		return result;
	}

	private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception {

		byte[] buffer = new byte[1024];
		int bytes = 0;

		while ((bytes = fis.read(buffer)) != -1) {
			os.write(buffer, 0, bytes);
		}
	}

	private static String getContent(String file) {
		if (file.endsWith(".html")) {
			return "text/html";
		}

		return "application/octet-stream";
	}
}
