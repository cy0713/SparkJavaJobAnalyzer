package main.java.analyzer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;

import org.json.simple.JSONObject;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;

import main.java.analyzer.visitor.StatementsExtractor;
import main.java.analyzer.visitor.StreamIdentifierVisitor;
import main.java.graph.FlowControlGraph;
import main.java.graph.GraphNode;
import main.java.graph.algorithms.SafeLambdaMigrationFinder;
import main.java.utils.Utils;

public class SparkJavaJobAnalyzer extends JavaStreamsJobAnalyzer {

	protected final String jobType = "sparkjava";

	protected static String targetedDatasets = "(JavaRDD)"
			+ "(\\s*?(<\\s*?\\w*\\s*?(,\\s*?\\w*\\s*?)?\\s*?>))?"; //\\s*?\\w*\\s*?=";
	
	protected final String pushableTransformations = "(map|filter|flatMap)";
	protected final String pushableActions = "(collect|count|iterator|reduce)";
	
	protected final String translationRulesPackage = "main.java.rules.translation." + jobType  + ".";
	
	public JSONObject analyze (String fileToAnalyze) {
		
		//Get the input stream from the job file to analyze
		FileInputStream in = null;		
		try {
			in = new FileInputStream(fileToAnalyze);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		//Parse the job file
        CompilationUnit cu = JavaParser.parse(in); 
        
        //Keep the original job code if we cannot execute lambdas due to resource constraints
        String originalJobCode = cu.toString();
        String translatedJobCode = originalJobCode;
        
        //First, get all the variables of type Stream, as they are the candidates to push down lambdas
        new StreamIdentifierVisitor(targetedDatasets, identifiedStreams).visit(cu, null);
        
        //Second, once we have the streams identified, we have to inspect each one looking for safe lambdas to push down      
        new StatementsExtractor(identifiedStreams, pushableTransformations, 
        		pushableActions, null).visit(cu, null); 
        
        //Next, we apply translation rules so the Spark job is understandable by the JavaStreams analyzer
        System.out.println(">>>>>>>>>" + translationRulesPackage);
        for (String key: identifiedStreams.keySet()){
        	applyRulesToControlFlowGraph(identifiedStreams.get(key), translationRulesPackage);
        }      
        
        //Translate specific Spark Jobs classes/RDD calls into JavaStreams classes/calls 
        for (String key: identifiedStreams.keySet()){
        	FlowControlGraph graph = identifiedStreams.get(key);
        	//FIXME: Maybe we need rules also for variables
        	translatedJobCode = translatedJobCode.replace("import org.apache.spark.api.java.JavaRDD;", 
        			"import java.util.stream.Stream;");
        	translatedJobCode = translatedJobCode.replace("JavaRDD", "Stream");
        	translatedJobCode = translatedJobCode.replace("sc.textFile", "Stream.of");
        	for (GraphNode node: identifiedStreams.get(key)){
        		//Modify the original's job code according to translation rules
    			translatedJobCode = translatedJobCode.replace(node.getLambdaSignature(), node.getCodeReplacement());
           	}
        }  
        
        System.out.println(translatedJobCode);
        	
        //Create a new file with the job translated into JavaStreams classes and functions
        String className = Paths.get(fileToAnalyze).getFileName().toString().replace(".java", "");
        translatedJobCode = translatedJobCode.replace(className, className+"JavaStreamsTranslated");
        String translatedJobPath = Paths.get(fileToAnalyze.replace(".java", "JavaStreamsTranslated.java")).toString();
        try (PrintWriter out = new PrintWriter(translatedJobPath)){
            out.println(translatedJobCode);
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        //Execute the JavaStreams analyzer on the translated job
        JavaStreamsJobAnalyzer javaStreamsAnalyzer = new JavaStreamsJobAnalyzer();
        JSONObject result = javaStreamsAnalyzer.analyze(translatedJobPath);
        //The lambdas to migrate should be Java8 Stream lambdas, as they will be executed by the Storlet
        List<SimpleEntry<String, String>> lambdasToMigrate = Utils.getLambdasToMigrate(result);
        String modifiedJobCode =  Utils.getModifiedJobCode(result);
        
        //Parse the job file
        CompilationUnit cu2 = JavaParser.parse(modifiedJobCode); 
        
        //Now we focus on the new modified job that should be translated back to Spark calls
        identifiedStreams.clear();
        
        //First, get all the variables of type Stream, as they are the candidates to push down lambdas
        new StreamIdentifierVisitor(JavaStreamsJobAnalyzer.targetedDatasets, identifiedStreams).visit(cu, null);
        
        //Second, once we have the streams identified, we have to inspect each one looking for safe lambdas to push down      
        new StatementsExtractor(identifiedStreams, JavaStreamsJobAnalyzer.pushableTransformations, 
        		JavaStreamsJobAnalyzer.pushableActions, null).visit(cu, null); 
        
        //Next, we need to update the job code in the case the flow graph has changed
        for (String key: identifiedStreams.keySet()){
        	applyRulesToControlFlowGraph(identifiedStreams.get(key), modificationRulesPackage);
        } 

        //FIXME: Maybe we need rules also for variables
    	modifiedJobCode = modifiedJobCode.replace("import java.util.stream.Stream;",
    			"import org.apache.spark.api.java.JavaRDD;");
    	modifiedJobCode = modifiedJobCode.replace("Stream.of", "sc.textFile");
    	modifiedJobCode = modifiedJobCode.replace("Stream", "JavaRDD");
    	
        for (String key: identifiedStreams.keySet()){        	
        	for (GraphNode node: identifiedStreams.get(key)){
        		//Modify the original's job code according to modification rules
        		modifiedJobCode = modifiedJobCode.replace(node.getLambdaSignature(), node.getCodeReplacement());
        	}
        }  
        System.out.println(modifiedJobCode);
        //The control plane is in Python, so the caller script will need to handle this result
        //and distinguish between the lambdas to pushdown and the code of the job to submit
        return Utils.encodeResponse(originalJobCode, modifiedJobCode, lambdasToMigrate);
	}
}