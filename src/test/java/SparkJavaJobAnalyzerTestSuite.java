package test.java;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SimpleLogAnalyzerTest.class,
        SimpleLogAnalyzer2Test.class,
        SimpleLogAnalyzer3Test.class,
        WordCountJava8StreamsTest.class,
        SimpleListCollectorTest.class
})
public class SparkJavaJobAnalyzerTestSuite {}