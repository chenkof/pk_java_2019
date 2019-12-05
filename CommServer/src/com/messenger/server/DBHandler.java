package com.messenger.server;

import com.mongodb.Block;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.print.Doc;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

public class DBHandler {
    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> userCollection;
    MongoCollection<Document> messagesCollection;
    MongoCollection<Document> roomsCollection;


    Block<Document> printBlock = document -> System.out.println(document.toJson());

    SingleResultCallback<Void> callbackWhenFinished = new SingleResultCallback<Void>() {
        @Override
        public void onResult(final Void result, final Throwable t) {
            System.out.println("Operation Finished: " + result + t) ;
        }
    };
    SingleResultCallback<Document> callbackPrintDocuments = new SingleResultCallback<Document>() {
        @Override
        public void onResult(final Document document, final Throwable t) {
            System.out.println(document.toJson());
        }
    };



    public DBHandler() {
        // connect to db
        this.mongoClient = MongoClients.create();
        this.database = mongoClient.getDatabase("comm");

        // get collections
        this.userCollection = database.getCollection("users");
        this.messagesCollection = database.getCollection("messages");
        this.roomsCollection = database.getCollection("rooms");

    }

    public Document createUserDocument(String login, String passwd) {
        return new Document("login", login)
                .append("passwd", passwd);
    }

    synchronized private Boolean insert(String collection, Document doc) {
        
        final MongoCollection coll;
        switch(collection) {
            case "users": coll = this.userCollection; break;
            case "messages": coll = this.messagesCollection; break;
            case "rooms": coll = this.roomsCollection; break;
            default:
                return false;
        }
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Boolean[] status = {false};
            coll.insertOne(doc, new SingleResultCallback<Void>() {
                @Override
                public void onResult(final Void result, final Throwable t) {
                    status[0] = true;
                    latch.countDown();
                }
            });
            latch.await();
            return status[0];
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    synchronized public Boolean login(Document doc) {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Boolean[] status = {false};
            this.userCollection.find(doc).first(new SingleResultCallback<Document>() {
                @Override
                public void onResult(final Document document, final Throwable t) {
                    try {
                        System.out.println(document.toJson());
                        status[0] = true;
                        latch.countDown();
                    } catch(NullPointerException e) {
                        latch.countDown();
                    }
                }
            });
            latch.await();
            return status[0];
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean register(Document doc) {
        return this.insert("users", doc);
    }

    public Boolean logMessage(String json) {
        return this.insert("messages", Document.parse(json));
    }

    synchronized public JSONArray getHistory(String query) {
        final JSONArray results = new JSONArray();
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Boolean[] status = {false};
            this.messagesCollection.find(Document.parse(query)).forEach(
                new Block<Document>() {
                    @Override
                    public void apply(final Document document) {
                        results.put(new JSONObject(document.toJson()));
                    }
                },
                new SingleResultCallback<Void>() {
                    @Override
                    public void onResult(final Void result, final Throwable t) {
                        status[0] = true;
                        latch.countDown();
                    }
                }
            );
            latch.await();
            return results;
        } catch(Exception e) {
            e.printStackTrace();
            return results;
        }
    }

    synchronized public CopyOnWriteArrayList getRooms() {
        final CopyOnWriteArrayList<String> results = new CopyOnWriteArrayList<>();
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Boolean[] status = {false};
            results.add("all");
            this.roomsCollection.find().forEach(
                new Block<Document>() {
                    @Override
                    public void apply(final Document document) {
                        results.add(document.getString("name"));
                    }
                },
                new SingleResultCallback<Void>() {
                    @Override
                    public void onResult(final Void result, final Throwable t) {
                        status[0] = true;
                        latch.countDown();
                    }
                }
            );
            latch.await();
            return results;
        } catch(Exception e) {
            e.printStackTrace();
            return results;
        }
    }

    public Boolean createRoom(String name) {
        return this.insert("room", new Document("name", name));
    }
}
