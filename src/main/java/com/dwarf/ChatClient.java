package com.dwarf;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatClient {

	public static void main(String[] args) throws URISyntaxException, InterruptedException {
		Socket socket = IO.socket("http://127.0.0.1:9092/demo");
		socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

		  @Override
		  public void call(Object... args) {
		  }

		});
		
		Socket socket2 = IO.socket("http://127.0.0.1:9092");
		socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

		  @Override
		  public void call(Object... args) {
		  }

		});
		socket.connect();
		socket2.connect();
		Thread.sleep(Integer.MAX_VALUE);
	}

}
