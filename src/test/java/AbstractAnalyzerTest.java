package test.java;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import junit.framework.TestCase;
import main.java.analyzer.JavaStreamsJobAnalyzer;
import main.java.compiler.JobCompiler;
import test.java.cases.TestTask;

public abstract class AbstractAnalyzerTest extends TestCase{
	
	protected final String TEST_PATH = Paths.get("").toAbsolutePath().toString()+
			"/src/test/resources/";
	
	protected String outputStorletFileNormal = "test_data/storlet_output." + 
				this.getClass().getName() + "_normal";
	protected String outputStorletFilePushdown = "test_data/storlet_output." + 
				this.getClass().getName() + "_pushdown";
	protected String jobResultNormal = "test_data/job_result." + this.getClass().getName() + 
			"_normal";
	protected String jobResultPushdown = "test_data/job_result." + this.getClass().getName() + 
			"_pushdown";
	
	//These  fields are required to be initialized by every tests that extends this class
	protected TestTask analyticsJob;
	protected String jobToAnalyze;	
	protected String inputStorletFile; //meter_gen.csv;
	
	//These fields will be filled after the job analysis result
	protected String modifiedJobCode;
	protected HashMap<String, String> lambdaMap = new HashMap<>();	
	
	public void testAnalyze(){

		/*
		 * STEP 1: Execute the analytics task without pushdown
		 */
		TestUtils testUtils = new TestUtils();
		testUtils.executePushdownStorlet(new HashMap<>(), inputStorletFile, outputStorletFileNormal);
		
		//We execute the analytics job on the output of the storlet
		testUtils.writeTaskOutputResult(analyticsJob.doTask(outputStorletFileNormal), jobResultNormal);
		//Make sure that the result of the storlet without lambdas is the same as the input
		assertTrue(testUtils.compareFiles(inputStorletFile, outputStorletFileNormal));
		
		/*
		 * STEP 2: Execute pushdown analysis on the analytics task
		 */     
        JavaStreamsJobAnalyzer jobAnalyzer = new JavaStreamsJobAnalyzer();        
        // visit and print the methods names
        String pushdownAnalysisResult = jobAnalyzer.analyze(this.TEST_PATH + jobToAnalyze, true).toString();
        //Load the results of the job analyzer (lambda map and modified code)
        loadAnalyzerResults(pushdownAnalysisResult);
        
        /*
		 * STEP 3: Execute again the analytics task and also the lambdas at the storage side
		 */
        testUtils.executePushdownStorlet(lambdaMap, inputStorletFile, outputStorletFilePushdown);
        //Make sure that the result of the storlet with lambdas is different to the input
        if (lambdaMap.isEmpty()) 
        	assertTrue(testUtils.compareFiles(inputStorletFile, outputStorletFilePushdown));
        else assertFalse(testUtils.compareFiles(inputStorletFile, outputStorletFilePushdown));

		analyticsJob = (TestTask) new JobCompiler().compileFromString(analyticsJob.getClass()
												   .getSimpleName(), modifiedJobCode);
		//We execute the analytics job on the output of the storlet
		testUtils.writeTaskOutputResult(analyticsJob.doTask(outputStorletFilePushdown), jobResultPushdown);
		
		/*
		 * STEP 4: Compare that the job results of pushdown and no pushdown are the same
		 */
		assertTrue(testUtils.compareFiles(jobResultPushdown, jobResultNormal));		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	protected void loadAnalyzerResults (String jobAnalyzerOutput) {
		JSONParser parser = new JSONParser();
		JSONObject jsonObj = null;
		try {
			jsonObj = (JSONObject) parser.parse(jobAnalyzerOutput);
			@SuppressWarnings("unchecked")
			Iterator<JSONObject> lambdas =  ((JSONArray) jsonObj.get("lambdas")).iterator();
			int index = 0;
			while (lambdas.hasNext()){
				JSONObject jlambda = lambdas.next();
				lambdaMap.put(index+"-lambda", (String)jlambda.get("lambda-type-and-body"));
				index++;
			}
			modifiedJobCode = (String) jsonObj.get("pushdown-job-code");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}