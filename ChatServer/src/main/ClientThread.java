package main;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import model.Client;

public class ClientThread extends Thread implements IClientThread {
	private final Socket socket;
	private final Client who;
	private IClientListener listener;
	private PrintWriter out;
	private BufferedReader in;
	private int id;
	
	public ClientThread (Socket socket, int id, IClientListener listener){
		this.socket = socket;
		this.listener = listener;
		this.id = id;
		this.who = new Client();
		this.who.setName(String.format("User: %d", id));
	}
	
	@Override
	public void run(){
		
		try {
			
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			// Tell client that you have connected
			String probe = String.format("%s has connected", who.getName());
			System.out.println(probe);
			listener.sendMessage(probe, id);
			
			while(!socket.isClosed()){
				
				String message = in.readLine();
				
				if (message != null && !message.isEmpty()){
					message = String.format("%s: %s", who.getName(), message);
					if (listener != null) listener.sendMessage(message, id);
				}
			}
			
		} catch (IOException e) {
		}finally{
			try {
				exitChat();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void exitChat() throws IOException {
		in.close();
		out.flush();
		out.close();
		socket.close();
	
		String msg = String.format("%s has disconnected", who.getName());
		listener.sendMessage(msg, id);
		listener.removeClient(id);
		System.out.println(msg);
	}

	@Override
	public void sendMessageToSocket(String message) {
		if (!Util.isNullOrEmpty(message)){
			out.println(message);
		}
	}
	
	@Override
	public int getClientId(){
		return this.id;
	}

	@Override
	public void startThread() {
		this.start();
	}

}
