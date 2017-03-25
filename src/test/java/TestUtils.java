package test.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.ibm.storlet.common.StorletInputStream;
import com.ibm.storlet.common.StorletLogger;
import com.ibm.storlet.common.StorletObjectOutputStream;
import com.ibm.storlet.common.StorletOutputStream;

import test.java.storlet.LambdaPushdownStorlet;

public class TestUtils {
	
	protected final String OUTPUT_MD_FILE_NAME = "test_data/output_record_md.txt";
	protected final String LOGGER_FILE_NAME = "test_data/logger";	
	
	public void executePushdownStorlet(HashMap<String, String> lambdaMap, String inputFile, String outputFile){
		
		try {
			
			FileInputStream infile = new FileInputStream(inputFile);
			FileOutputStream outfile = new FileOutputStream(outputFile);
			FileOutputStream outfile_md = new FileOutputStream(OUTPUT_MD_FILE_NAME);
	
			HashMap<String, String> md = new HashMap<String, String>();
			StorletInputStream inputStream1 = new StorletInputStream(infile.getFD(), md);
	        StorletObjectOutputStream outStream = new StorletObjectOutputStream(outfile.getFD(), md, outfile_md.getFD());
	        
	        ArrayList<StorletInputStream> inputStreams = new ArrayList<StorletInputStream>();
	        inputStreams.add(inputStream1);	        
	        ArrayList<StorletOutputStream> outStreams = new ArrayList<StorletOutputStream>();
	        outStreams.add(outStream);
	        
	        LambdaPushdownStorlet storlet = new LambdaPushdownStorlet();
	        
			FileOutputStream loggerFile = new FileOutputStream(LOGGER_FILE_NAME);					
			StorletLogger logger = new StorletLogger(loggerFile.getFD());	
			
			System.out.println("before storlet");
			storlet.invoke(inputStreams, outStreams, lambdaMap, logger);
			System.out.println("after storlet");			
			
			infile.close();
			outfile.close();
			outfile_md.close();
			loggerFile.close();
			
		}catch (Exception e) {
			System.out.println("Exception executing LambdaPushdown Storlet!");
			e.printStackTrace();
		}		
	}
	
	public boolean compareFiles(String file1, String file2){		
		try {
			byte[] f1 = Files.readAllBytes(Paths.get(file1));
			byte[] f2 = Files.readAllBytes(Paths.get(file2));
			System.out.println("Size of file " + file1 + ": " + f1.length);
			System.out.println("Size of file " + file2 + ": " + f2.length);
			System.out.println("Relative size difference between files: " + 
					new Double(f1.length-f2.length)/new Double(f1.length) + "%");
			return Arrays.equals(f1, f2);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/*
	 * Simple way to write expected small outputs into a file for later comparison
	 */
	protected void writeTaskOutputResult(StringBuilder builder, String outputFile){
		FileWriter outputStream;
		try {
			outputStream = new FileWriter(new File(outputFile));
			outputStream.write(builder.toString());
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
