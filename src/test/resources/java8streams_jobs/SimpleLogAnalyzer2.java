package test.resources.java8streams_jobs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import test.java.TestTask;

public class SimpleLogAnalyzer2 implements TestTask{
	
	public StringBuilder doTask(String inputFile) {
		StringBuilder builder = new StringBuilder();
		try{
			Stream<String> myStream = Files.lines(Paths.get(inputFile));
			long lines = myStream
					.filter(s -> s.contains("Hamlet"))
					.map(l -> new Integer(l.length()))
					.filter(s -> s > 15)
					.count();
			builder.append(lines);
			myStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 		
		System.out.println(builder.toString());
		return builder;
	}
}