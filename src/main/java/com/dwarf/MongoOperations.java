package com.dwarf;

import com.mongodb.client.MongoDatabase;

public interface MongoOperations {
	
	MongoDatabase getDatabase(String dbName);
	
	void close();
	
}
