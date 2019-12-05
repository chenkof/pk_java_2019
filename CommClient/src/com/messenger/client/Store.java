package com.messenger.client;

import java.util.ArrayList;
import java.util.List;

public class Store {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";

    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";

    public static final String ROOM = "room";
    public static final String USER = "user";

    private List<String> clients;
    private List<String> rooms;
    public String _target;
    public String _targetType; // room or user
    public String login;
    public Boolean isLoggedIn = false;

    public Store() {
        this.clients = new ArrayList<>();
        this.rooms = new ArrayList<>();
        this.rooms.add("all");
        this._target = "all";
        this._targetType = ROOM;
    }

    //print available rooms
    public void showRooms() {
        System.out.println("Available rooms: ");
        for (String x : this.rooms) {
            System.out.println("\t- \""+ x +"\"");
        }
    }

    //print available users
    public void showClients() {
        System.out.println("Clients Online: ");
        for (String x : this.clients) {
            System.out.println("\t- \""+ x +"\"");
        }
    }

    public void clearRooms() {
        this.rooms = new ArrayList<>();
    }

    public void clearClients() {
        this.clients = new ArrayList<>();
    }

    public void addClient(String newClient) {
        this.clients.add(newClient);
    }

    public void addRoom(String newRoom) {
        this.rooms.add(newRoom);
    }

    public void removeClient(String clientName) {
        this.clients.remove(clientName);
    }

    public Boolean checkRooms(String name) {
        return this.rooms.indexOf(name) >= 0;
    }

    public Boolean checkClients(String name) {
        return this.clients.indexOf(name) >= 0;
    }

    public void removeRoom(String roomName) {
        this.clients.remove(roomName);
    }

    public void setTarget(String name, String type) {
        this._target = name;
        this._targetType = type;
    }

}
