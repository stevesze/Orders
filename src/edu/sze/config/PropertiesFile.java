package edu.sze.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;



public class PropertiesFile {

    private Logger logger = Logger.getLogger(PropertiesFile.class.getName());
    
    private FileHandler fh = null;   

    public Properties getProps(String fileName) {

        Properties props = new Properties();
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd HHmmss");
        // check properties file
        try {
        	
        	
            File f = new File(getClass().getResource(fileName).getFile());
            String fullPath = getClass().getResource(fileName).getPath();
            String path = fullPath.substring(0, fullPath.indexOf(fileName)-1);
            
            
            fh = new FileHandler("logs/TradeProcess" + format.format(Calendar.getInstance().getTime()) + ".log");
            
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            
            if (f.exists()) {
            	FileInputStream fis = new FileInputStream(f);
                props.load(fis);
                @SuppressWarnings("unchecked")
				Enumeration<String> enums = (Enumeration<String>) props.propertyNames();
                while (enums.hasMoreElements()) {
                	String key = enums.nextElement();
                	props.setProperty(key, path+"/"+props.getProperty(key));
                	String value = props.getProperty(key);
                	System.out.println(value);
                }
                fis.close();
            }
            
        } catch (FileNotFoundException fex) {
            logger.log(Level.SEVERE, fex.getMessage());
        } catch (IOException ioex) {
            logger.log(Level.SEVERE, ioex.getMessage());
        }

        return props;
    }
}
