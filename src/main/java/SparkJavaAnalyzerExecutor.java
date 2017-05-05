package main.java;

import java.io.IOException;

import org.json.simple.JSONObject;

import main.java.analyzer.JavaStreamsJobAnalyzer;
import main.java.analyzer.SparkJavaJobAnalyzer;

public class SparkJavaAnalyzerExecutor {

	public static void main(String[] args) throws IOException {		
        SparkJavaJobAnalyzer analyzer = new SparkJavaJobAnalyzer();
        JSONObject result = analyzer.analyze(args[0]);
        System.out.println(result.toString());
	}
}