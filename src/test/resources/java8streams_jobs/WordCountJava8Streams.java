package test.resources.java8streams_jobs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.stream.Stream;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import test.java.TestTask;

public class WordCountJava8Streams implements TestTask{
	
	public StringBuilder doTask(String inputFile) {
		StringBuilder builder = new StringBuilder();
		try{
			Stream<String> myStream = Files.lines(Paths.get(inputFile));
			//Map<String, Long> wordCount = myStream

			//Map<String, Long> myStream2 = 
			myStream.flatMap(line -> Arrays.stream(line.trim().split(" ")))
            		.map(word -> word.replaceAll("[^a-zA-Z]", "").toLowerCase().trim())
            		.map(word -> new SimpleEntry<String, Long>(word, (long) 1))
            		.collect(groupingBy(SimpleEntry::getKey, counting()));			
			//myStream.close();
			//for (String key: myStream2.keySet()){
			//	System.out.println(key + " " + myStream2.get(key));
			//}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return builder;
	}
}
