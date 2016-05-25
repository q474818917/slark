package com.dwarf.common.mongo;

import com.mongodb.client.MongoDatabase;

public interface MongoOperations {
	
	MongoDatabase getDatabase(String dbName);
	
	void close();
	
}
