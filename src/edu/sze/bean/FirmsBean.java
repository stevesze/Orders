package edu.sze.bean;

import java.util.logging.Logger;

public class FirmsBean {
	
	private Logger logger = Logger.getLogger(FirmsBean.class.getName());
	
    private String firm;
    private int time;
    private int orders;
    
    public FirmsBean(String firm, int time, int orders) {
    	this.firm = firm;
    	this.time = time;
    	this.orders = orders;
    }

	public String getFirm() {
        return firm;
    }

    public void setFirm(String firm) {
        this.firm = firm;
    }
    
    public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getOrders() {
		return orders;
	}

	public void setOrders(int orders) {
		this.orders = orders;
	}


}
