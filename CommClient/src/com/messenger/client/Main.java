package com.messenger.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

// Client class
public class Main {
    public static void main(String[] args) {

        InetAddress ip;
        Socket s;
        DataInputStream dis;
        DataOutputStream dos;
        Listener listener;
        Sender sender;

        Store globalStore = new Store();

        /*globalStore.addClient("newRoom #1");
        globalStore.addRoom("newRoom #2");
        globalStore.addRoom("newRoom #3");
        globalStore.showRooms();
        globalStore.removeRoom("newRoom #2");
        globalStore.showRooms();*/

        try
        {

            ip = InetAddress.getByName("localhost");
            s = new Socket(ip, 16421);


            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());

            listener = new Listener(s, dis, globalStore);
            sender = new Sender(s, dos, globalStore);

            listener.start();
            sender.start();


        }catch(Exception e){
            e.printStackTrace();
        }

        // Main loop

    }
}