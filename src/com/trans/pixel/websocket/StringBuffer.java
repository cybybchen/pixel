package com.trans.pixel.websocket;

public class StringBuffer {
	private static final int BUFFERSIZE = 20;
	private static String[] buffer = new String[BUFFERSIZE];
	
	public StringBuffer() {	
		for(int i = 0; i < BUFFERSIZE; i++) {
			buffer[i] = " ";
		}
	}
	
	private boolean isFull() {
		for(String s : buffer) {
			if (" ".equals(s)) {
				return false;
			}
		}
		return true;
	}

	private void clean() {
		for(int i=0; i < BUFFERSIZE;i++){
			buffer[i]=" ";
		}
	}
	
	public String[] getBuffer() {
		return buffer;
	}
	
	public void put(String stringToAdd) {
		if(isFull()) {
			clean();
		}
		for(int i=0;i<10;i++) {
			if(" ".equals(buffer[i])) {
				buffer[i] = stringToAdd;
				break;
			}
		}
	}

}
