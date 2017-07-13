package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
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
	private String timeStamp;

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
								
				switch (message.getCommand()) {
					case "connect":
						log.info("user <{}> connected", message.getUsername());
						users.put(message.getUsername(), socket);
						timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
						message.setContents(timeStamp + " " + message.getUsername().toString() + " connected" );
						for (String key : users.keySet()) {;
							PrintWriter connectAlert = new PrintWriter(new OutputStreamWriter
									(users.get(key).getOutputStream()));
							
							String connected = mapper.writeValueAsString(message);
							connectAlert.write(connected);
							connectAlert.flush();
						}
						break;
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
						users.remove(message.getUsername());
						timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
						message.setContents(timeStamp + " " + message.getUsername().toString() + " disconnected" );
						for (String key : users.keySet()) {;
							PrintWriter disconnectAlert = new PrintWriter(new OutputStreamWriter
									(users.get(key).getOutputStream()));
							
							String disconnected = mapper.writeValueAsString(message);
							disconnectAlert.write(disconnected);
							disconnectAlert.flush();
						}
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
						timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
						log.info("user <{}> broadcasted message <{}>", message.getUsername(), message.getContents());						
						String everything = createContents(message.getUsername(), timeStamp, message.getCommand(), message.getContents());
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
//					case "@":
//						log.info("user <{}> direct messaged <{}>", message.getUsername(), message.getCommand().substring(1));
//						String directMessage = mapper.writeValueAsString(message);
//						PrintWriter dmWrite = new PrintWriter(new OutputStreamWriter
//								(users.get(message.getCommand().substring(1)).getOutputStream()));
//						dmWrite.write(directMessage);
//						dmWrite.flush();
//						break;
					case "users":
						log.info("user <{}> called display users", message.getUsername(), message.getContents());
						timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
							message.setContents(timeStamp + "\n" + getUsers());
							String dispUsers = mapper.writeValueAsString(message);
							writer.write(dispUsers);
							writer.flush();
						break;
					default:
						for (String key : users.keySet()){
							timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
							if(message.getCommand().compareTo(key) == 0) {
								log.info("user <{}> direct messaged <{}>", message.getUsername(), message.getCommand());
								String everything1 = createContents(message.getUsername(), timeStamp, "whispered", message.getContents());
								message.setContents(everything1);
								String directMessage = mapper.writeValueAsString(message);
								PrintWriter dmWrite = new PrintWriter(new OutputStreamWriter
									(users.get(message.getCommand()).getOutputStream()));
								dmWrite.write(directMessage);
								dmWrite.flush();
							}
						}
						break;
					}
				}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}
	
	public String createContents(String username, String timeStamp, String command, String contents) {
		String everything = timeStamp + " " + username + " " + command + " " + contents;
		
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
