package de.embl.cba.plateviewer.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public abstract class AbstractRepository {
    private final MongoCollection<Document> collection;

    public AbstractRepository(MongoClient mongoClient, String database, String collection) {
        this.collection = mongoClient.getDatabase(database).getCollection(collection);
    }


    public MongoCollection<Document> getCollection() {
        return collection;
    }
}
