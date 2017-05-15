package test.resources.test_jobs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import test.java.cases.TestTask;

public class Java8SimpleReduceWithMathUse implements TestTask{
	
	public StringBuilder doTask(String inputFile) {
		StringBuilder builder = new StringBuilder();
		try{
			Stream<String> myStream = Files.lines(Paths.get(inputFile));
			//FIXME: Now we only support reduce->optional version
			int countLines = myStream.filter(s -> s.contains("Hamlet"))
								 .map(l -> l.length())
								 .reduce((a, b) -> a*b).get();
			builder.append(countLines);
			myStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 		
		return builder;
	}

}
