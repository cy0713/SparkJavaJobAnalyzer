package test.java;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import test.java.cases.SparkSimpleTextAnalysisTest;
import test.java.cases.SparkWordCountTest;
import test.java.cases.SparkWordCountTest2;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    	SparkSimpleTextAnalysisTest.class,
    	SparkWordCountTest.class,
    	SparkWordCountTest2.class
})
public class SparkJobAnalyzerTestSuite {}