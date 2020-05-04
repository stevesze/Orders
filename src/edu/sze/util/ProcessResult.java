package edu.sze.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ProcessResult {
	
	public void output(String trade, File file) {
		
		FileWriter fw = null;
		BufferedWriter br = null;
		
		try {
			
			fw = new FileWriter(file, true);
			br = new BufferedWriter(fw);
			br.write(trade+System.getProperty("line.separator"));
			
		} catch (IOException ioex) {
			System.err.println(ioex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
                br.close();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
		}
		
	}
	

}
