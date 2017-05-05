package test.java.cases;

import java.nio.file.Paths;

import junit.framework.TestCase;
import main.java.analyzer.JavaStreamsJobAnalyzer;
import main.java.analyzer.SparkJavaJobAnalyzer;
import test.java.AbstractAnalyzerTest;

public class SimpleTextAnalysisSparkTest extends TestCase{	
	
	protected final String TEST_PATH = Paths.get("").toAbsolutePath().toString()+
			"/src/test/resources/";
	
	public void testJob() {
		/*
		 * STEP 2: Execute pushdown analysis on the analytics task
		 */     
        SparkJavaJobAnalyzer jobAnalyzer = new SparkJavaJobAnalyzer();        
        // visit and print the methods names
        String pushdownAnalysisResult = jobAnalyzer.analyze(
        		this.TEST_PATH + "/test_jobs/SparkJavaSimpleTextAnalysis.java").toString();
	}	
}
