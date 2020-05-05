package edu.sze.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.sze.bean.BrokerUniqueID;
import edu.sze.bean.FirmsBean;
import edu.sze.config.PropertiesFile;
import edu.sze.util.ProcessResult;

public class TradeProcessor {
	
    private final static Logger logger = LogManager.getLogger(TradeProcessor.class.getName());

    private String propFile = "order.properties";
    
    private List<String> symbolList = new ArrayList<String>();

	private List<FirmsBean> firmList = new ArrayList<FirmsBean>();
    
    private List<BrokerUniqueID> brokerList = new ArrayList<BrokerUniqueID>();
    
    static private int successful = 0;
    static private int fail_missingFields = 0;
    static private int fail_symbolsInvalid = 0;
    static private int fail_brokerLimit = 0;
    static private int fail_brokerID = 0;
    static private int total_transaction = 0;
    
    public static void main(String[] args) {

    	logger.info("****** Starting Trade Checks ******");

        TradeProcessor tp = new TradeProcessor();
        String trades = tp.getList();
        tp.runTradeCheck(trades);
        
        logger.info("Successful trades = "+successful);
        logger.info("Rejected trades for missing fields = "+fail_missingFields);
        logger.info("Rejected trades for invalid symbols = "+fail_symbolsInvalid);
        logger.info("Rejected trades for broker limit = "+fail_brokerLimit);
        logger.info("Rejected trades for broker seq number = "+fail_brokerID);
        logger.info("Total number of trades = "+total_transaction);
        
        
    }
    
    public TradeProcessor() {
    	
        String trades = getList();
        runTradeCheck(trades);
    	
    }
    
    
    private void runTradeCheck(String tradesList) {
    	boolean validTrade = true;    	
    	BufferedReader reader;
    	try {
    		reader = new BufferedReader(new FileReader(tradesList));
    		String nextLine = reader.readLine();

    		SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd_HHmmss");
        	File fileAll = new File("output/AllResult." + format.format(Calendar.getInstance().getTime()) + ".csv");
        	File fileSuccess = new File("output/SuccessTrade." + format.format(Calendar.getInstance().getTime()) + ".csv");
        	File fileFailed = new File("output/FailedTrade." + format.format(Calendar.getInstance().getTime()) + ".csv");
        	
    		while (nextLine!=null) {
    			ProcessResult result = new ProcessResult();
    			String[] nextTrade = nextLine.split(",");
    			if (nextTrade[0].equalsIgnoreCase("Time stamp")) {
    				result.output(nextLine, fileAll);
    				result.output(nextLine, fileSuccess);
    				result.output(nextLine, fileFailed);
    				nextLine = reader.readLine();
    			} else {
    				total_transaction++;
    				/** 
        			 * 1st Check : All Fields exist
        			 * 
        			 * Only orders that have values for the fields of �broker�, �symbol�, �type�, �quantity�, 
        			 * �sequence id�, �side�, and �price� should be accepted.
        			 * 
        			 */
        			validTrade = checkFields(nextTrade); 
        			/**
        			 * 
        			 * 2nd Check : Valid Symbol
        			 * 
        			 * Only orders for symbols actually traded on the exchange should be accepted
        			 * 
        			 */
        			String symbol = nextTrade[4];
        			if (validTrade) {
        				validTrade = checkSymbol(symbol);
        			}

        			
        			/**
        			 * 
        			 * 3rd Check : Only 3 orders per minute
        			 * 
        			 * Each broker may only submit three orders per minute: any additional
        			 * orders in should be rejected
        			 * 
        			 */
        			String dateStr = nextTrade[0];
        			String broker = nextTrade[1];
        			if (validTrade) {
        				validTrade = checkBrokerLimit(dateStr, broker);
        			}
        			
        			/**
        			 * 
        			 * 4th Check : Unique Broker ID 
        			 * 
        			 * Within a single broker�s trades ids must be unique. 
        			 * If ids repeat for the same broker, 
        			 * only the first message with a given id should be accepted.
        			 * 
        			 */
        			String id = nextTrade[2];
        			if (validTrade) {
        				validTrade = checkBrokerSeqID(broker, id);
        			}
        			
        			result.output(nextLine, fileAll);
    				if (validTrade) {
    					result.output(nextLine, fileSuccess);
    					successful++;
    				} else {
    					result.output(nextLine, fileFailed);
    				}
        			
        			nextLine = reader.readLine();
    			}
    			
    		}
    		reader.close();
    		
    	} catch (IOException ex) {
    		logger.error(ex.getMessage());
    	}
    }

    private boolean checkFields(String[] trade) {
    	boolean allFieldsExist = true;
    	int index = 0;
    	StringBuffer sb = new StringBuffer();
    	for (String tradeDetail : trade) {
    		sb.append(tradeDetail+" ");
    		if (tradeDetail==null || tradeDetail.trim().length()==0) {
    			allFieldsExist=false;
    			logger.info("");
    			logger.info("*******************************************************************");
    			logger.info("***** Rejected Trade due to missing Field "+trade[index]+" ********");
    			logger.info("*******************************************************************");
    			fail_missingFields++;
    			index++;
    			break;
    		} 
		}
    	sb.append("\n");
		logger.info(sb.toString());
		sb = null;
    	
    	return allFieldsExist;
    }
    
    private boolean checkSymbol(String tradeSymbol) {
    	boolean validSymbol = false;
    	
    	for (String symbol : symbolList) {
    		if (symbol.equalsIgnoreCase(tradeSymbol)) {
    			validSymbol = true;
    			break;
    		}
    	}
    	
    	if (!validSymbol) {
    		logger.info("*******************************************************************");
    		logger.info("******  REJECTED TRADE DUE TO INVALID SYMBOL "+tradeSymbol+" ******");
    		logger.info("*******************************************************************");
    		fail_symbolsInvalid++;
    	}

    	return validSymbol;
    }
    
    private boolean checkBrokerLimit(String dateTimeStr, String broker) {
    	boolean validBrokerOrder = true;
    	
    	//** No need for date with assumption that trade date is the same date bet 9:30 to 4pm
    	String[] dateTimeArray = dateTimeStr.split(" ");
    	//String[] dateArray = dateTimeArray[0].split("/");
    	String[] timeArray = dateTimeArray[1].split(":");
    	//int MON = Integer.parseInt(dateArray[0]);
    	//int DD = Integer.parseInt(dateArray[1]);
    	//int YYYY = Integer.parseInt(dateArray[2]);
    	int HH = Integer.parseInt(timeArray[0]);
    	int MM = Integer.parseInt(timeArray[1]);
    	int SS = Integer.parseInt(timeArray[2]);
    	int timeInSecs = (HH*3600)+(MM*60)+SS;
    	
    	Vector tempFirm = new Vector();
    	int orderCount = 0;
    	
    	for (FirmsBean firm : firmList) {
    		if (firm.getFirm().equalsIgnoreCase(broker)) {
				Vector temp = new Vector();
    			int time = firm.getTime();
    			int order = firm.getOrders();
    			orderCount++;
    			temp.add(broker);
    			temp.add(time);
    			temp.add(order);
    			tempFirm.add(temp);
    		}
    	}
    	
    	tempFirm.trimToSize();
    	
    	int cnt = 0;
    	boolean cleanup = false;
    	if (orderCount<3) {
    		firmList.add(new FirmsBean(broker,timeInSecs,orderCount+1));
    	} else if (orderCount>3) {
    		for (FirmsBean firm : firmList) {
    			if (firm.getFirm().equalsIgnoreCase(broker)) {
    				int initialTime = firm.getTime();
    				if (initialTime+60<timeInSecs) {
    					logger.info("*******************************************************************");
    		    		logger.info("******  REJECTED TRADE DUE TO OVER 3 ORDERS per MIN "+broker+" ******");
    		    		logger.info(initialTime+60+" < "+timeInSecs);
    		    		logger.info("*******************************************************************");
    		    		fail_brokerLimit++;
    					validBrokerOrder = false;
    					break;
    				} else if (initialTime+60>timeInSecs) {
    					firmList.remove(cnt);
    					cleanup = true;
    				}			
    			}
    			cnt++;
    		}
    	}
    	
    	if (cleanup) {
    		for (int i=0; i<tempFirm.size(); i++) {
    			Vector temp = (Vector) tempFirm.get(i);
    			int tempTime = (int) temp.get(1);
    			if (tempTime+60<timeInSecs) {
    				firmList.add(new FirmsBean(broker,tempTime,i+1));
    			}
    		}
    	}
    	

    	return validBrokerOrder;
    }
    
    private boolean checkBrokerSeqID(String broker, String idStr) {
    	boolean validBrokerID = true;
    	int id = Integer.parseInt(idStr);
    	
    	for (BrokerUniqueID firm : brokerList) {
    		if (broker.equalsIgnoreCase(firm.getBroker())) {
    			if (id==firm.getId()) {
    				logger.info("*******************************************************************");
		    		logger.info("******  REJECTED TRADE DUE TO IDENTICAL ID for "+broker+" with "+id+" ******");
		    		logger.info("*******************************************************************");
		    		fail_brokerID++;
		    		validBrokerID = false;
    			} else {
    				firm.setId(id);
    			}
    			break;
    		}
    	}
    	
    	
    	return validBrokerID;
    }
    
    
    private String getList() {
    	logger.info("****** Getting Properties ******");
        PropertiesFile config = new PropertiesFile();
        Properties configFiles = config.getProps(propFile);
        
        logger.info("****** Setting Up Initial Checks ******");

        String symbol = configFiles.getProperty("symbols");
        setDefaultList(symbol, symbolList);
        
        String trade = configFiles.getProperty("trades");
        return trade;

    }
    
    private void setDefaultList(String type, List<String> values) {
    	BufferedReader reader;
    	try {
    		reader = new BufferedReader(new FileReader(type));
    		String nextLine = reader.readLine();
    		while (nextLine!=null) {
    			values.add(nextLine);
    			nextLine = reader.readLine();
    		}
    		reader.close();
    		
    	} catch (IOException ex) {
    		logger.error(ex.getMessage());
    	}
    }
    
    public static int getSuccessful() {
		return successful;
	}


	public static int getFail_missingFields() {
		return fail_missingFields;
	}


	public static int getFail_symbolsInvalid() {
		return fail_symbolsInvalid;
	}


	public static int getFail_brokerLimit() {
		return fail_brokerLimit;
	}


	public static int getFail_brokerID() {
		return fail_brokerID;
	}


	public static int getTotal_transaction() {
		return total_transaction;
	}

}
