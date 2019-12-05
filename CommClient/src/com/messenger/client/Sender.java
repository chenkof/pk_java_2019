package com.messenger.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Sender extends Thread {

    final Socket s;
    final DataOutputStream dos;
    final Scanner keyboard;
    Store st;

    public Sender(Socket s, DataOutputStream dos, Store st) {
        this.s = s;
        this.dos = dos;
        this.keyboard = new Scanner(System.in);
        this.st = st;
    }

    @Override
    public void run() {
        while (true)
        {
            try {

                String input = this.keyboard.nextLine().trim();
                String value = "";
                String command = "";
                Boolean hasError = false;

                switch(input) {
                    case ">help":
                        System.out.println(Store.ANSI_CYAN_BACKGROUND + "-------------------------------------------------------------" + Store.ANSI_RESET);
                        System.out.println(Store.ANSI_CYAN_BACKGROUND + "Command list:                                                " + Store.ANSI_RESET);
                        System.out.println(Store.ANSI_CYAN_BACKGROUND + ">login           (log into existing account)                 " + Store.ANSI_RESET);
                        System.out.println(Store.ANSI_CYAN_BACKGROUND + ">logout          (logout from currently used account)        " + Store.ANSI_RESET);
                        System.out.println(Store.ANSI_CYAN_BACKGROUND + ">register        (create new account)                        " + Store.ANSI_RESET);
                        System.out.println(Store.ANSI_CYAN_BACKGROUND + ">listRooms       (show all rooms available on server)        " + Store.ANSI_RESET);
                        System.out.println(Store.ANSI_CYAN_BACKGROUND + ">listUsers       (show all online users)                     " + Store.ANSI_RESET);
                        System.out.println(Store.ANSI_CYAN_BACKGROUND + ">chooseRoom      (get history and start chatting in a room)  " + Store.ANSI_RESET);
                        System.out.println(Store.ANSI_CYAN_BACKGROUND + ">chooseUsers     (get history and start chatting with a user)" + Store.ANSI_RESET);
                        System.out.println(Store.ANSI_CYAN_BACKGROUND + ">createRoom      (create new, uniquely named room)           " + Store.ANSI_RESET);
                        System.out.println(Store.ANSI_CYAN_BACKGROUND + "-------------------------------------------------------------" + Store.ANSI_RESET);
                        break;
                    case ">listRooms":
                        if(st.isLoggedIn) {
                            command = "listRooms";
                            value = "{}";
                        }
                        break;
                    case ">listUsers":
                        if(st.isLoggedIn) {
                            command = "listUsers";
                            value = "{}";
                        }
                        break;
                    case ">chooseRoom":
                        if(st.isLoggedIn) {
                            System.out.println("Room name: ");
                            String rName = this.keyboard.nextLine();
                            if (this.st.checkRooms(rName)) {
                                command = "getHistory";
                                value = "{'type':'room','target':'" + rName + "'}";
                                this.st.setTarget(rName, Store.ROOM);
                            } else {
                                System.out.println(Store.ANSI_RED + "+++ Room \"" + rName + "\" does not exist! +++" + Store.ANSI_RESET);
                                hasError = true;
                            }
                        }
                        break;
                    case ">chooseUser":
                        if(st.isLoggedIn) {
                            System.out.println("User name: ");
                            String uName = this.keyboard.nextLine();
                            if (this.st.checkClients(uName)) {
                                command = "getHistory";
                                value = "{'type':'user','$or':[{'author':'" + uName + "','target':'" + this.st.login + "'},{'author':'" + this.st.login + "','target':'" + uName + "'}]}";
                                this.st.setTarget(uName, Store.USER);
                            } else {
                                System.out.println(Store.ANSI_RED + "+++ User \"" + uName + "\" is offline or does not exist! +++" + Store.ANSI_RESET);
                                hasError = true;
                            }
                        }
                        break;
                    case ">createRoom":
                        if(st.isLoggedIn) {
                            System.out.println("Room name: ");
                            String newRName = this.keyboard.nextLine();
                            if (!this.st.checkRooms(newRName)) {
                                command = "createRoom";
                                value = "{'name':'" + newRName + "'}";
                                //this.st.setTarget(newRName, Store.ROOM);
                            } else {
                                System.out.println(Store.ANSI_RED + "+++ Room \"" + newRName + "\" already exist! +++" + Store.ANSI_RESET);
                                hasError = true;
                            }
                        }
                        break;
                    case ">register":
                    case ">login":
                        if(!this.st.isLoggedIn) {
                            System.out.println("Login: ");
                            String login = this.keyboard.nextLine();
                            System.out.println("Password: ");
                            String passwd = this.keyboard.nextLine();
                            command = input.equals(">register") ? "register" : "login";
                            value = "{'login':'" + login + "','passwd':'" + passwd + "'}";
                            this.st.login = login;
                        } else {
                            System.out.println(Store.ANSI_RED + "+++ You are already logged in! +++" + Store.ANSI_RESET);
                            hasError = true;
                        }

                        break;
                    case ">logout":
                        if(st.isLoggedIn) {
                            command = "logout";
                            value = "{}";
                            this.st.isLoggedIn = false;
                            this.st.login = "";
                        }
                        break;
                    default:
                        if(st.isLoggedIn && input.indexOf(">") < 0) {
                            command = "sendMessage";
                            value = "{'type':'" + this.st._targetType + "','target':'" + this.st._target + "','message':'" + input + "'}";
                        }
                }

                if(!command.equals("") && !value.equals(""))
                    dos.writeUTF("{'command':'" + command + "','value':" + value + "}" );
                else if(!hasError && !input.equals(">help")) {
                    if(input.indexOf(">") >= 0)
                        System.out.println(Store.ANSI_RED + "+++ Command not found! +++" + Store.ANSI_RESET);
                    else if(input.length() > 0)
                        System.out.println(Store.ANSI_YELLOW + "+++ Please Log In First! +++" + Store.ANSI_RESET);
                }

                // local:
                // >listRooms -> get rooms from store
                // >listUsers -> get currently logged users from store
                // >chooseRoom -> switch current message target to room (also used when creating a room)
                // >chooseUser -> switch current message target to user


            } catch (Exception e) {
                try {
                    this.dos.close();
                } catch(IOException exc) {
                    exc.printStackTrace();
                }
            }
        }
    }
}
