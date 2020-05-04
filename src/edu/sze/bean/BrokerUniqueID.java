package edu.sze.bean;

public class BrokerUniqueID {
	
	private String broker;
	private int id;
	
    public BrokerUniqueID(String broker, int id) {
    	this.broker = broker;
    	this.id = id;
    }
	
	public String getBroker() {
		return broker;
	}
	public void setBroker(String broker) {
		this.broker = broker;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	

}
