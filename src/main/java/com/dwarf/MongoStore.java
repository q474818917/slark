package com.dwarf;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoStore {
	
	MongoDatabase db;
	
	public MongoStore(String dbName){
		db = MongoDBServer.getInstance().getDatabase(dbName);
	}
	
	public void insert(Document document, String collectionName){
		MongoCollection<Document> collection = db.getCollection(collectionName);
		collection.insertOne(document);
	}
	
	public Document findFirst(String collectionName){
		MongoCollection<Document> collection = db.getCollection(collectionName);
		return collection.find().first();
	}
	
	public Document findFilter(Bson filter, String collectionName){
		MongoCollection<Document> collection = db.getCollection(collectionName);
		return collection.find(filter).first();
	}
	
	public List<Document> findAll(String collectionName){
		MongoCollection<Document> collection = db.getCollection(collectionName);
		List<Document> docList = new ArrayList<>();
		Block<Document> printDocumentBlock = new Block<Document>() {
		    @Override
		    public void apply(final Document document) {
		        System.out.println(document.toJson());
		    }
		};
		collection.find().forEach(printDocumentBlock);
		return docList;
	}
	
	public void batchInsert(){
		
	}
	
	public static void main(String[] args) {
		MongoStore store = new MongoStore("chat");
		/*Document document = new Document("id", 2).append("name", "巧克力");
		store.insert(document);*/
	}

}
