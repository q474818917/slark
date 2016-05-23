package com.dwarf.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

public class MongoDBServer implements MongoOperations {
	
	private static Logger logger = Logger.getLogger(MongoDBServer.class);
	
	private static MongoDBServer _mongoDBUtils;
	
	private static Properties prop = new Properties();
	
	static {
		InputStream in = MongoDBServer.class.getClassLoader().getResourceAsStream("server.properties");
		try {
			prop.load(in);
			logger.info("server.properties loading succeed");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static MongoClient mongoClient;
	
	private MongoDBServer(){
		List<ServerAddress> addressList = new ArrayList<>();
		for(int i = 0; i < prop.getProperty("mongo_url").split(",").length; i++){
			addressList.add(new ServerAddress(prop.getProperty("mongo_url").split(",")[i],Integer.parseInt(prop.getProperty("mongo_port"))));
		};
		mongoClient = new MongoClient(addressList);
		
	}
	
	public static MongoDBServer getInstance(){
		if(_mongoDBUtils == null){
			_mongoDBUtils = new MongoDBServer();
		}
		return _mongoDBUtils;
	}

	public static void main(String[] args) {
		MongoClient mongoClient =MongoDBServer.getInstance().mongoClient;
		DB db = mongoClient.getDB("chat");
		Set<String> colls = db.getCollectionNames();
		for (String s : colls) {
		    //System.out.println(s);
		}
		DBCollection feedScoll = db.getCollection("feed");
		/*DBCursor cursor = null;
		cursor = feedScoll.find();
		while(cursor.hasNext()){
			DBObject object = cursor.next();
			System.out.println(object.get("cover"));
		}*/
		
		/*System.out.println(feedScoll.find(new BasicDBObject("uuid", 2000188)).toArray());
		
		DB db2 = mongoClient.getDB("chat");
		DBCollection feedScoll2 = db2.getCollection("feedcomment");
		
		List<DBObject> dbList = feedScoll2.find(new BasicDBObject("tid", 2001465)).toArray();
		for(DBObject object : dbList){
			System.out.println(object.get("uid"));
		}*/
		List<Long> ids = Arrays.asList(735320l,701443l,632646l,495066l,217730l,598916l,551209l,514373l,546110l,506087l,350923l,278869l,285287l,228424l,238420l,238286l,213082l,227074l,224005l,219106l,218314l,217970l,212536l,201171l,197596l,198237l,89179l,196606l,97205l,195682l,195568l,194031l,191771l,187828l,185927l,185553l,139885l,173407l,86066l,103698l,177828l,174510l,173726l,155289l,146728l,148480l,141314l,140386l,137915l,137011l,136740l,135337l,133869l,134680l,133782l,132414l,128751l,127245l,126926l,121595l,120729l,119903l,114111l,111880l,110364l,93984l,82998l,87960l,93472l,103580l,109391l,97163l,96209l,93570l);
		MongoDBServer.getInstance().updateMulti(feedScoll, ids, "succeed", true);
	}
	
	public void query(String key, String value){
		
	}
	
	public void close(){
		this.mongoClient.close();
	}
	
	public void updateMulti(DBCollection collection, List<Long> ids, String field, Object value){
		for(Long id : ids){
			BasicDBObject updateQuery = new BasicDBObject();
			updateQuery.append("$set", new BasicDBObject().append(field, value));
			
			BasicDBObject searchQuery = new BasicDBObject();
			searchQuery.append("uuid", id);

			collection.updateMulti(searchQuery, updateQuery);
		}
	}

}
