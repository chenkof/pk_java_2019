package com.messenger.server;

import org.json.JSONArray;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MessageExchanger {

    Map<String, ClientHandler> clients;
    List<String> rooms;

    DBHandler dbController;

    public MessageExchanger(DBHandler handler) {
        this.clients = new ConcurrentHashMap<>();
        this.rooms = new CopyOnWriteArrayList<>();
        this.dbController = handler;

        this.rooms = this.dbController.getRooms();
    }

    public void addClient(ClientHandler newClient) {
        this.clients.put(newClient.getName(), newClient);
    }
    public void disconnectClient(String clientPort) {
        this.clients.remove(clientPort);
    }

    // send command; only used by a server
    public void sendCommand(String command, String value, String port) {

        try {
            this.clients.get(port).sendMessage("{'command': " + command + ", 'value': " + value + "}");
        } catch (Exception e) {
            System.out.println("ERROR: Sending command: \""+command+"\" failed!");
        }
    }

    public void loginUser(String userName) {
        for (ClientHandler client : this.clients.values()) {
            if(client.isLoggedIn && !client.userName.equals(userName))
                this.sendCommand("newClient", "{'name':'" + userName + "'}", client.getPort());
        }
    }

    public void logoutUser(String userName) {
        for (ClientHandler client : this.clients.values()) {
            if(client.isLoggedIn && !client.userName.equals(userName))
                this.sendCommand("endClient", "{'name':'" + userName + "'}", client.getPort());
        }
    }

    public void listUsers(String port) {
        JSONArray userList = new JSONArray();
        for (ClientHandler client : this.clients.values()) {
            if(client.isLoggedIn && !client.getPort().equals(port))
                userList.put(client.userName);
        }

        this.sendCommand("listUsers", "{'users':" + userList.toString() + "}", port);
    }

    public void listRooms(String port) {
        this.sendCommand("listRooms", "{'rooms':" + this.rooms.toString() + "}", port);
    }

    public void sendMessage(String mes, String type, String target, String author, String port) {
        for (ClientHandler client : this.clients.values()) {
            if(client.isLoggedIn && ( (type.equals("user") && client.userName.equals(target)) || (type.equals("room") && !client.userName.equals(author)) ) )
                this.sendCommand("sendMessage", "{'status':'ok','message':'" + mes + "','author':'" + author + "','target':'" + target + "','type':'" + type + "'}", client.getPort());

        }

    }

    public void getHistory(String value, String port) {
        JSONArray history = this.dbController.getHistory(value);
        this.sendCommand("getHistory", "{'status':'ok','history':'" + history.toString() + "'}", port);
    }
}
