package edu.sze.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Enumeration;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


public class PropertiesFile {

    private final Logger logger = LogManager.getLogger(PropertiesFile.class);
    
    public Properties getProps(String fileName) {

        Properties props = new Properties();
        // check properties file
        try {
        	
        	
            File f = new File(getClass().getResource(fileName).getFile());
            String fullPath = getClass().getResource(fileName).getPath();
            String path = fullPath.substring(0, fullPath.indexOf(fileName)-1);
            
            if (f.exists()) {
            	FileInputStream fis = new FileInputStream(f);
                props.load(fis);
                @SuppressWarnings("unchecked")
				Enumeration<String> enums = (Enumeration<String>) props.propertyNames();
                while (enums.hasMoreElements()) {
                	String key = enums.nextElement();
                	props.setProperty(key, path+"/"+props.getProperty(key));
                	String value = props.getProperty(key);
                	logger.info(value);
                }
                fis.close();
            }
            
        } catch (FileNotFoundException fex) {
            logger.error(fex.getMessage());
        } catch (IOException ioex) {
            logger.error(ioex.getMessage());
        }

        return props;
    }
}
