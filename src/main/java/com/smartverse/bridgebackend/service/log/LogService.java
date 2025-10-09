package com.smartverse.bridgebackend.service.log;

import com.mongodb.client.MongoCollection;
import com.smartverse.bridgebackend.config.mongo.ConnectionMongoDb;
import com.smartverse.bridgebackend.model.LogFilter;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
public class LogService {

    public void saveLog(LogFilter log){
        var connection = ConnectionMongoDb.getInstance();
        var database = connection.getDatabase();

        MongoCollection<Document> collection = database.getCollection("log_request");

        var map = new HashMap<String, Object>();

        map.put("path", log.getPath());
        map.put("method", log.getMethod());
        map.put("status", log.getStatus());
        map.put("duration", log.getDuration());
        map.put("timestamp", log.getTimestamp());

        collection.insertOne(new Document(map));
    }

    public List<LogFilter> getAllLogs(){
        return Collections.emptyList();
    }
}
