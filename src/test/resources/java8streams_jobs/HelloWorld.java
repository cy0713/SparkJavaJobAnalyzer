package test.resources.java8streams_jobs;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class HelloWorld {
	
	public static void main(String[] args) {
		
		List<String> myList = Arrays.asList("a1", "a2", "b1", "c2", "c1");
		
		Stream<String> myStream = myList.stream();
				
		myStream.map(s -> s + ":)")
				.filter(s -> s.length()>1)
				.flatMap(s -> Arrays.stream(s.split("")))
				.map(s -> new SimpleEntry<String, Integer>(":)",1))
				.map(x -> x.toString())
				.filter(x -> x.length() > 10);
	}

}
