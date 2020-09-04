package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Scanner;

public class Client implements Runnable {
	private SocketAddress srvAddr;
	private SocketAddress myAddr;
	private static final String defaultIpAddr = "127.0.0.1";
	
	public Client(String srvIpAddress, int srvPort) {
		this(defaultIpAddr, srvIpAddress, srvPort);
	}
	
	public Client(String myIpAddress, String srvIpAddress, int srvPort) {
		this.srvAddr = new InetSocketAddress(srvIpAddress, srvPort);
		this.myAddr = new InetSocketAddress(myIpAddress, 0);
	}

	public Client(int port) {
		this(defaultIpAddr, defaultIpAddr, port);
	}
	
	public void run() {
		Socket socket = null;
		try {
			socket = new Socket();
			socket.bind(myAddr);
			socket.connect(srvAddr);
			
			// start a thread that reads the input of the client and sends it to the server
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			new Reader(socket.getOutputStream()).start();
			
			// read and print out received messages from server
			String received = "";
			while (true) {
				received = in.readLine();
				System.out.println(received);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	
	public void start() {
		new Thread(this).start();
	}
	
	static class Reader implements Runnable {
		PrintWriter out;
		Scanner in;
		
		public Reader(OutputStream out) {
			this.out = new PrintWriter(out, true);
			this.in = new Scanner(System.in);
		}
		
		public void sendMessage(String message) {
			out.println(message);
		}
		
		public void run() {
			while (true) {
				String toSend = in.nextLine();
				sendMessage(toSend);
			}
		}
		
		public void start() {
			new Thread(this).start();
		}
	}
	
	public static void main(String[] args) {
		int port = 52443;
		String ip = "8.8.8.8";
		String myIp = "192.168.1.53";
		new Client(port).start();
	}
}