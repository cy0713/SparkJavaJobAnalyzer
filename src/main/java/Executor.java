package main.java;

import main.java.analyzer.SparkJavaJobAnalyzer;

public class Executor {

	public static void main(String[] args) {		
        SparkJavaJobAnalyzer analyzer = new SparkJavaJobAnalyzer();
        System.out.println(analyzer.analyze(args[0]));
	}
}
