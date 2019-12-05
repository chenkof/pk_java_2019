package com.messenger.server;
import java.io.*;
import java.net.*;

import org.bson.Document;
import org.json.*;

import javax.print.Doc;

public class ClientHandler extends Thread {

    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    MessageExchanger exchanger;
    DBHandler dbController;

    public Boolean isLoggedIn = false;
    public String userName;

    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos, MessageExchanger exchanger, DBHandler dbHandler) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
        this.exchanger = exchanger;
        this.dbController = dbHandler;

        this.setName( String.valueOf(s.getPort()) );
    }

    @Override
    public void run() {

        // send available rooms
        // send meesage history

        while (true) {

            try {

                // receive the answer from client
                String received = this.dis.readUTF();
                JSONObject obj = new JSONObject(received);

                String command = obj.getString("command");
                JSONObject value = obj.getJSONObject("value");

                // logger for testing purposes:
                System.out.println(command + " | " + value);

                Boolean status = false;
                String reply = "";

                switch (command) {
                    case "createRoom":
                        String name = value.getString("name");
                        status = this.dbController.createRoom(name);
                        if (status) {
                            reply = "{'status':'ok','command':'joinRoom','value':{'name':'" + name + "'}}";
                            JSONArray nRHistory = this.dbController.getHistory(value.toString());
                            this.exchanger.sendCommand("getHistory", "{'status':'ok','history':'" + nRHistory.toString() + "'}", this.getPort());
                        } else
                            reply = "{'status':'error','command':'joinRoom','value':{'message':'Room: \"" + name + "\" already exists!'}}";

                        break;
                    case "listRooms":
                        this.exchanger.listRooms(this.getPort());
                        break;
                    case "listUsers":
                        this.exchanger.listUsers(this.getPort());
                        break;
                    case "sendMessage":
                        String mes = value.getString("message");
                        String type = value.getString("type"); // "user" or "room"
                        String target = value.getString("target"); // userName or roomName
                        this.exchanger.sendMessage(mes, type, target, this.userName, this.getPort());

                        //send response to user:
                        reply = "{'status':'ok','message':'" + mes + "','author':'" + this.userName + "','target':'" + target + "','type':'" + type + "'}";
                        this.dbController.logMessage(reply);
                        break;
                    case "getHistory":
                        this.exchanger.getHistory(value.toString(), this.getPort());
                        break;
                    case "register":
                    case "login":
                        Document user = dbController.createUserDocument(value.getString("login"), value.getString("passwd"));
                        status = dbController.login(user);
                        if(status) {
                            if(command.equals("login")) {
                                this.login(value.getString("login"));
                                reply = "{'status':'ok','message':'Successfully logged in to existing account!'}";
                            }
                            else reply = "{'status':'error','message':'User with name: \"" + userName + "\" already exists!'}";
                        }
                        else if(command.equals("register")) {
                            status = dbController.register(user);
                            if(status) {
                                this.login(value.getString("login"));
                                reply = "{'status':'ok','message':'Successfully registered and logged in!'}";
                            }
                        } else
                            reply = "{'status':'error','message':'Login or password is invalid!'}";
                        break;
                    case "logout":
                        this.exchanger.logoutUser(userName);
                        this.isLoggedIn = false;
                        this.userName = null;
                        reply = "{'status':'ok','message':'Successfully logged out!'}";
                    // "createRoom"     -> value: {String (roomName), Array (ports)}
                    // "joinRoom"       -> value: String (roomName)
                    // "leaveRoom"      -> value: String (roomName)
                    // "sendMessage"    -> value: {String (message), String (type: room,private,all), String (target), [String (base64)] }
                    // "getHistory"     -> value: {String (message), String (type: room,private,all), String (target), [String (base64)] }
                    // "login"          -> value: String name
                }

                if(!reply.equals("")) {
                    this.exchanger.sendCommand(command, reply, this.getPort());
                    if (status && (command.equals("login") || command.equals("register")) )
                        this.exchanger.getHistory("{'type':'room','target':'all'}", this.getPort());
                }

            } catch (IOException e) {
                break;
            }
        }

        try {
            System.out.println("CLIENT DISCONNECTED: " + this.s);
            this.exchanger.disconnectClient(this.getName());
            this.dis.close();
            this.dos.close();
            this.s.close();

        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) throws IOException {
        this.dos.writeUTF(message);
    }

    public String getPort() {
        return String.valueOf(this.s.getPort());
    }

    private void login(String name) {
        this.isLoggedIn = true;
        this.userName = name;
        this.exchanger.loginUser(name);
        this.exchanger.listUsers(this.getPort());
        this.exchanger.listRooms(this.getPort());
    }
}