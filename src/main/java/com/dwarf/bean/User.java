package com.dwarf.bean;

public class User {
	
	public User(String username, String uuid){
		this.username = username;
		this.uuid = uuid;
	}
	
	private String username;
	private String uuid;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	
	
}
