package test.java;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import test.java.cases.SimpleListCollectorTest;
import test.java.cases.SimpleLogAnalyzer2Test;
import test.java.cases.SimpleLogAnalyzer3Test;
import test.java.cases.SimpleLogAnalyzerTest;
import test.java.cases.SimpleMaxCollectorTest;
import test.java.cases.StreamVariableAssignmentTest;
import test.java.cases.WordCountJava8StreamsTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SimpleLogAnalyzerTest.class,
        SimpleLogAnalyzer2Test.class,
        SimpleLogAnalyzer3Test.class,
        WordCountJava8StreamsTest.class,
        SimpleListCollectorTest.class,
        SimpleMaxCollectorTest.class,
        StreamVariableAssignmentTest.class
})
public class SparkJavaJobAnalyzerTestSuite {}