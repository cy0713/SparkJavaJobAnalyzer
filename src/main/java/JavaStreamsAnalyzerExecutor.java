package main.java;

import java.io.IOException;

import org.json.simple.JSONObject;

import main.java.analyzer.JavaStreamsJobAnalyzer;

public class JavaStreamsAnalyzerExecutor {

	public static void main(String[] args) throws IOException {		
        JavaStreamsJobAnalyzer analyzer = new JavaStreamsJobAnalyzer();
        JSONObject result = analyzer.analyze(args[0]);
        System.out.println(result.toString());
	}
}