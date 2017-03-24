package test.java;

import java.util.HashMap;

import main.java.analyzer.SparkJavaJobAnalyzer;
import main.java.compiler.JobCompiler;
import test.resources.java8streams_jobs.SimpleLogAnalyzer;

public class SimpleLogAnalyzerTest extends AbstractAnalyzerTest{	
	
	public void testAnalyze(){

		/*
		 * STEP 1: Execute the analytics task without pushdown
		 */
		executePushdownStorlet(new HashMap<>(), "test_data/storlet_output.simpleloganalyzer_normal");
		TestTask analyticsJob = new SimpleLogAnalyzer();
		//We execute the analytics job on the output of the storlet
		writeTaskOutputResult(analyticsJob.doTask("test_data/storlet_output.simpleloganalyzer_normal"), 
				"test_data/job_result.simpleloganalyzer_normal");
		//Make sure that the result of the storlet without lambdas is the same as the input
		assertTrue(compareFiles(INPUT_FILE_NAME, "test_data/storlet_output.simpleloganalyzer_normal"));
		
		/*
		 * STEP 2: Execute pushdown analysis on the analytics task
		 */     
        SparkJavaJobAnalyzer jobAnalyzer = new SparkJavaJobAnalyzer();        
        // visit and print the methods names
        String pushdownAnalysisResult = jobAnalyzer.analyze(this.TEST_PATH + 
        		"/java8streams_jobs/SimpleLogAnalyzer.java");
        //Load the results of the job analyzer (lambda map and modified code)
        loadAnalyzerResults(pushdownAnalysisResult);
        
        /*
		 * STEP 3: Execute again the analytics task and also the lambdas at the storage side
		 */
        executePushdownStorlet(lambdaMap,"test_data/storlet_output.simpleloganalyzer_pushdown");
        //Make sure that the result of the storlet with lambdas is different to the input
      	assertFalse(compareFiles(INPUT_FILE_NAME, "test_data/storlet_output.simpleloganalyzer_pushdown"));
      	System.out.println(modifiedJobCode);
		analyticsJob = (TestTask) new JobCompiler().compileFromString(analyticsJob.getClass().getSimpleName(), modifiedJobCode);
		//We execute the analytics job on the output of the storlet
		writeTaskOutputResult(analyticsJob.doTask("test_data/storlet_output.simpleloganalyzer_pushdown"), 
				"test_data/job_result.simpleloganalyzer_pushdown");
		
		/*
		 * STEP 4: Compare that the job results of pushdown and no pushdown are the same
		 */
		assertTrue(compareFiles("test_data/job_result.simpleloganalyzer_pushdown", 
				"test_data/job_result.simpleloganalyzer_normal"));		
	}
}
