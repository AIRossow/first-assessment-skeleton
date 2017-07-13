package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {

	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;
	public static Map<String, Socket> users = new HashMap<String, Socket>();

	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
	}

	public void run() {
		try {

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);

				//need to change the second param to port or thread or something
				
				switch (message.getCommand()) {
					case "connect":
						log.info("user <{}> connected", message.getUsername());
						users.put(message.getUsername(), socket);
						message.setContents(message.getUsername().toString() + " connected" );
						for (String key : users.keySet()) {;
							PrintWriter broadWrite = new PrintWriter(new OutputStreamWriter
									(users.get(key).getOutputStream()));
							
							String connected = mapper.writeValueAsString(message);
							broadWrite.write(connected);
							broadWrite.flush();
						}
						break;
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
						users.remove(message.getUsername());
						this.socket.close();
						break;
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						String response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
					case "broadcast":
						//create broadcast
						log.info("user <{}> broadcasted message <{}>", message.getUsername(), message.getContents());						
						String everything = createContents(message.getUsername(), message.getCommand(), message.getContents());
						message.setContents(everything);
						for (String key : users.keySet()) {
							PrintWriter broadWrite = new PrintWriter(new OutputStreamWriter
									(users.get(key).getOutputStream()));
							
							//String broadcaster = mapper.writeValueAsString(message.getUsername());
							String broadcast = mapper.writeValueAsString(message);
							broadWrite.write(broadcast);
							broadWrite.flush();
						}
						break;
					case "@":
						//create @
						break;
					case "users":
						log.info("user <{}> called display users", message.getUsername(), message.getContents());
							message.setContents(getUsers());
							String dispUsers = mapper.writeValueAsString(message);
							writer.write(dispUsers);
							writer.flush();
						break;
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}
	
	public String createContents(String username, String command, String contents) {
		String everything = username + " " + command + " " + contents;
		
		return everything;
	}
	
	public String getUsers() {
		String userNames = "";
		
		for(String key : users.keySet()) {
			//userNames.add(entry.getKey());
				userNames += "   " + key + "\n";
			}
		return userNames;
	}

}
