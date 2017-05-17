package test.resources.test_jobs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import test.java.cases.TestTask;

import java.util.AbstractMap.SimpleEntry;

public class Java8ComplexMap implements TestTask{
	
	public StringBuilder doTask(String inputFile) {
		StringBuilder builder = new StringBuilder();
		try{
			Stream<String> myStream = Files.lines(Paths.get(inputFile));
			long countLines = myStream.filter(s -> s.contains("Hamlet"))
									  .map(s -> {
										  String[] parts = s.split(" ");
										  return new SimpleEntry<String, String>(parts[0], parts[1]);
									   }).count();
			builder.append(countLines);
			myStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 		
		return builder;
	}

}
