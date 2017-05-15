package test.java;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import test.java.cases.SparkStreamingJavaHelloWorldTest;
import test.java.cases.SparkStreamingWordCountTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    	SparkStreamingJavaHelloWorldTest.class,
    	SparkStreamingWordCountTest.class
})
public class SparkStreamingJobAnalyzerTestSuite {}