package test.resources.java8streams_jobs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import test.java.TestTask;

public class SimpleMaxCollector implements TestTask{
	
	public StringBuilder doTask(String inputFile) {
		StringBuilder builder = new StringBuilder();
		try{
			Stream<String> myStream = Files.lines(Paths.get(inputFile));
			String maxLine = myStream.filter(s -> s.contains("Hamlet"))
								 .map(l -> l.toString())
								 .filter(s -> s.length()>10)
								 .collect(Collectors.maxBy(String::compareTo)).get();
			builder.append(maxLine);
			myStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 		
		return builder;
	}

}
