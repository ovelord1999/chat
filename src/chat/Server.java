package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;

public class Server implements Runnable {
	private SocketAddress addr;
	private final SocketAddress defaultAddr = new InetSocketAddress("127.0.0.1", 0);
	
	public Server(String ipAddress, int port) {
		this.addr = new InetSocketAddress(ipAddress, port);
	}
	
	public Server() {
		this.addr = defaultAddr;
	}
	
	public void run() {
		ServerSocket srvSocket = null;
		try {
			srvSocket = new ServerSocket();
			srvSocket.bind(addr);
			System.out.println("Server successfully started");
			System.out.println("Using IP address " + srvSocket.getInetAddress());
			System.out.println("Listening on port " + srvSocket.getLocalPort());
			while (true) {
				Socket socket = srvSocket.accept();
				System.out.println("got connection");
				new ClientHandler(socket).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				srvSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	
	public void start() {
		new Thread(this).start();
	}
	
	static class ClientHandler implements Runnable {
		static Set<ClientHandler> handlers = new HashSet<>();
		
		static boolean isFree(String username) {
			for (ClientHandler handler : handlers) {
				if (handler.getUsername().equals(username)) {
					return false;
				}
			}
			return true;
		}
		
		static void broadcast(String senderUsername, String message) {
			for (ClientHandler handler : handlers) {
				if (!handler.getUsername().equals(senderUsername)) {
					handler.sendMessage(senderUsername + ": " + message);
				}
			}
		}
		
		PrintWriter out;
		BufferedReader in;
		String username;
		
		public ClientHandler(Socket socket) {
			try {
				out = new PrintWriter(socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void sendMessage(String message) {
			out.println(message);
		}
		
		public String readMessage() throws IOException {
			return in.readLine();
		}
		
		String getUsername() {
			return this.username;
		}
		
		public void run() {
			try {
				sendMessage("Give your username");
				String username = readMessage();
				while (!isFree(username)) {
					sendMessage("This username is already used! Please choose another one");
					username = readMessage();
				}
				this.username = username;
				handlers.add(this);
				
				// now start the listening loop				
				String received = "";
				while (true) {
					received = readMessage();
					broadcast(getUsername(), received);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void start() {
			new Thread(this).start();
		}
	}
	
	public static void main(String[] args) {
		new Server().start();
	}
}
