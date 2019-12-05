package com.messenger.client;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class Listener extends Thread {

    final Socket s;
    final DataInputStream dis;
    Store st;

    public Listener(Socket s, DataInputStream dis, Store st) {
        this.s = s;
        this.dis = dis;
        this.st = st;
    }


    @Override
    public void run() {

        System.out.println(Store.ANSI_CYAN_BACKGROUND + "Welcome to CommSenger!                                      " + Store.ANSI_RESET);
        System.out.println(Store.ANSI_CYAN_BACKGROUND + "use '>login' command to login to exsistion account,         " + Store.ANSI_RESET);
        System.out.println(Store.ANSI_CYAN_BACKGROUND + "or '>register' to create a new one.                         " + Store.ANSI_RESET);
        System.out.println(Store.ANSI_CYAN_BACKGROUND + "If you want to quit or change account, use '>logout' command" + Store.ANSI_RESET);
        System.out.println(Store.ANSI_CYAN_BACKGROUND + "                                                            " + Store.ANSI_RESET);
        System.out.println(Store.ANSI_CYAN_BACKGROUND + "For full list of commands type '>help'                      " + Store.ANSI_RESET);
        System.out.println(Store.ANSI_CYAN_BACKGROUND + "------------------------------------------------------------" + Store.ANSI_RESET);

        while (true) {
            try {

                String received = dis.readUTF();

                JSONObject obj = new JSONObject(received);

                String command = obj.getString("command");
                JSONObject value = obj.getJSONObject("value");

                switch (command) {
                    case "newClient":
                        String newClient = value.getString("name");
                        this.st.addClient(newClient);
                        System.out.println(Store.ANSI_YELLOW + "+++ \"" + newClient + "\" has just logged in! +++" + Store.ANSI_RESET);
                        break;
                    case "endClient":
                        String oldClient = value.getString("name");
                        this.st.removeClient(oldClient);
                        System.out.println(Store.ANSI_YELLOW + "+++ \"" + oldClient + "\" left! +++" + Store.ANSI_RESET);
                        break;
                    case "newRoom":
                        String newRoom = value.getString("name");
                        this.st.addRoom(newRoom);
                        System.out.println(Store.ANSI_YELLOW + "+++ New room: \"" + newRoom + "\" has just been created! +++" + Store.ANSI_RESET);
                        break;
                    case "listUsers":
                        JSONArray users = value.getJSONArray("users");
                        if(users.length() > 0) {
                            System.out.println(Store.ANSI_YELLOW + "+++ Users Online: " + users.join(", ") + " +++" + Store.ANSI_RESET);
                            this.st.clearClients();
                            for(int i=0; i<users.length(); i++) {
                                this.st.addClient(users.getString(i));
                            }
                        }
                        break;
                    case "listRooms":
                        JSONArray rooms = value.getJSONArray("rooms");
                        if(rooms.length() > 0) {
                            System.out.println(Store.ANSI_YELLOW + "+++ Rooms available: " + rooms.join(", ") + " +++" + Store.ANSI_RESET);
                            this.st.clearRooms();
                            for(int i=0; i<rooms.length(); i++) {
                                this.st.addRoom(rooms.getString(i));
                            }
                        }
                        break;
                    case "getHistory":
                        JSONArray hist = new JSONArray(value.getString("history"));

                        for (int i = 0; i < hist.length(); i++) {
                            JSONObject entry = hist.getJSONObject(i);
                            if(!entry.getString("author").equals(this.st.login))
                                System.out.println(Store.ANSI_CYAN + "\t" + entry.getString("author") + ": " + entry.getString("message") + Store.ANSI_RESET);
                            else
                                System.out.println("\tYou: " + entry.getString("message"));
                        }

                        break;
                    case "joinRoom":
                        if(value.getString("status").equals("ok")) {
                            this.st._targetType = Store.ROOM;
                            this.st._target = value.getString("name");
                            System.out.println(Store.ANSI_GREEN + "+++ Successfully joined room: " + this.st._target + " +++" + Store.ANSI_RESET);
                        } else
                            System.out.println(Store.ANSI_RED + "+++ " + value.getString("message") + " +++" + Store.ANSI_RESET);

                        break;
                    case "register":
                    case "logout":
                    case "login":
                        String COLOR = Store.ANSI_RED;
                        if(value.getString("status").equals("ok")) {
                            if(command.equals("login")) this.st.isLoggedIn = true;
                            else if(command.equals("logout")) {
                                this.st.isLoggedIn = false;
                                this.st.login = "";
                            }
                            COLOR = Store.ANSI_GREEN;
                        }
                        System.out.println(COLOR + "+++ " + value.getString("message") + " +++" + Store.ANSI_RESET);
                        break;
                    case "sendMessage":

                        String author = value.getString("author");

                        if(author.equals(this.st.login)) {
                            System.out.println("\tYou: " + value.getString("message"));
                        } else if(this.st._targetType.equals(Store.ROOM)) {
                            try {
                                String room = value.getString("target");
                                if(room.equals(this.st._target)) {
                                    System.out.println(Store.ANSI_CYAN + "\t" + author + ": " + value.getString("message") + Store.ANSI_RESET);
                                }
                            } catch(JSONException e) {}
                        } else if(author.equals(this.st._target)) {
                            System.out.println(Store.ANSI_CYAN + "\t" + author + ": " + value.getString("message") + Store.ANSI_RESET);
                        }

                        // "createRoom"     -> value: {String (roomName), Array (ports)}
                        // "joinRoom"       -> value: String (roomName)
                        // "leaveRoom"      -> value: String (roomName)
                        // "sendMessage"    -> value: {String (message), String (type: room,private,all), String (target), [String (base64)] }
                        // "getHistory"     -> value: {String (message), String (type: room,private,all), String (target), [String (base64)] }
                        // "login"          -> value: String name
                }


            } catch (Exception e) {
                try {
                    this.dis.close();
                } catch (IOException exc) {
                    exc.printStackTrace();
                }
            }
        }
    }
}
