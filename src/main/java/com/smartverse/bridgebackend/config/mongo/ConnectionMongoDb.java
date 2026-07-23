package com.smartverse.bridgebackend.config.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.smartverse.bridgebackend.config.context.EnumConfigContext;
import jakarta.inject.Singleton;
import lombok.Getter;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Singleton
public class ConnectionMongoDb {

    private static ConnectionMongoDb instance;

    @Getter
    private MongoClient mongoClient;
    @Getter
    private MongoDatabase database;


    private ConnectionMongoDb() {
        this.mongoClient = MongoClients.create(getStringConnection());

        this.database = mongoClient.getDatabase(getEnvironment("MONGO_DATABASE", "logapi"));
    }

    public static ConnectionMongoDb getInstance() {
        if (instance == null) {
            synchronized (ConnectionMongoDb.class) {
                if (instance == null) {
                    instance = new ConnectionMongoDb();
                }
            }
        }
        return instance;
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    private String getStringConnection() {
        String mongoUri = System.getenv(EnumConfigContext.MONGO_URI.name());
        if (mongoUri != null && !mongoUri.isBlank()) {
            return mongoUri;
        }

        String user = System.getenv(EnumConfigContext.MONGO_USER.name());
        String password = System.getenv(EnumConfigContext.MONGO_PASSWORD.name());
        String host = getEnvironment(EnumConfigContext.MONGO_HOST.name(), "localhost");
        String port = getEnvironment(EnumConfigContext.MONGO_PORT.name(), "27017");

        if (user == null || password == null) {
            return String.format("mongodb://%s:%s", host, port);
        }

        return String.format("mongodb://%s:%s@%s:%s",
                encode(user), encode(password), host, port);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String getEnvironment(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

}
