package test.resources.test_jobs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import test.java.cases.TestTask;

public class Java8StreamVariableAssignments implements TestTask{

	@Override
	public StringBuilder doTask(String inputFile) {
		StringBuilder builder = new StringBuilder();
		try{
			Stream<String> myStream = Files.lines(Paths.get(inputFile));
			Stream<String> myStream2 = myStream.filter(s -> s.contains("Hamlet"));
			builder.append(myStream2.filter(s -> s.length() > 10).count());
		}catch(IOException e){
			e.printStackTrace();
		}
		return builder;
	}
	
	

}
