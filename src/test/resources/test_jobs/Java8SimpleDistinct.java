package test.resources.test_jobs;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import test.java.cases.TestTask;

public class Java8SimpleDistinct implements TestTask{
	
	public StringBuilder doTask(String inputFile) {
		StringBuilder builder = new StringBuilder();
		try {
			Stream<String> myStream = Files.lines(Paths.get(inputFile));
			
			long countLines = myStream.distinct().filter(s -> s.contains("Hamlet")).count();
			
			builder.append(countLines);
			myStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
 		
		return builder;
	}

}
