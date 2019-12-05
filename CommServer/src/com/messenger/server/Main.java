package com.messenger.server;


/*
 * Project for "Programming in Java" Laboratories
 * Cracow Institute of Technology
 *
 * copyright Grzegorz Modzelewski
 * 2019
 */

import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import org.bson.Document;

import java.io.IOException;
import java.net.*;
import java.io.*;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        ServerSocket ss = null;
        try {
            ss = new ServerSocket(16421);
        } catch (IOException e) {
            e.printStackTrace();
        }

        DBHandler hndlr = new DBHandler();
        MessageExchanger exchanger = new MessageExchanger(hndlr);

        // Main loop
        while (true)
        {
            Socket s = null;

            try {
                // socket object to receive incoming client requests
                s = ss.accept();

                System.out.println("New client connected on: " + s);
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                //System.out.println("Assigning new thread for this client");

                // create a new thread object
                ClientHandler t = new ClientHandler(s, dis, dos, exchanger, hndlr);

                // Invoking the start() method
                t.start();
                exchanger.addClient(t);

            } catch (Exception e){
                try {
                    s.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}



/*
 * CLIENT:
 * - send message
 * - receive message
 * - get message history
 * - send photo (will be transformed in a separate thread
 * - switch between rooms
 * - create rooms
 * - browse through users
 *
 * SERVER:
 * - creates & maintains session for each logged user
 * - passes & saves message to history
 * - saves & sends images
 * -
 */

// trzeba pokazać, że bez synchronizacji dostępu do zasobu apka nie działa poprawnie
// wątki zależne:
//  - countdown latch
//  - cyclic barrier