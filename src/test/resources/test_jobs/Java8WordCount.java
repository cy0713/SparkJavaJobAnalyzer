package test.resources.test_jobs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collector;
import java.util.stream.Stream;

import test.java.cases.TestTask;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
//TODO: THis import shouldnt be here, it should be added to the modified job code
import static java.util.stream.Collectors.summingLong;

public class Java8WordCount implements TestTask{
	
	public StringBuilder doTask(String inputFile) {
		StringBuilder builder = new StringBuilder();
		try{
			Stream<String> myStream = Files.lines(Paths.get(inputFile));

			Map<String, Long> myStream2 = myStream.flatMap(line -> Arrays.stream(line.trim().split(" ")))
            		.map(word -> word.replaceAll("[^a-zA-Z]", "").toLowerCase().trim())
            		.map(word -> new SimpleEntry<String, Long>(word, (long) 1))
            		.collect(groupingBy(SimpleEntry<String, Long>::getKey, counting()));	
			
			for (String key: new TreeMap<>(myStream2).keySet()){
				builder.append(key + " " + myStream2.get(key)+"\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return builder;
	}
}
