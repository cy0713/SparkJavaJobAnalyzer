package main.java.analyzer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;

import org.json.simple.JSONObject;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;

import main.java.analyzer.visitor.StatementsExtractor;
import main.java.analyzer.visitor.StreamIdentifierVisitor;
import main.java.graph.GraphNode;
import main.java.graph.algorithms.SafeLambdaMigrationFinder;
import main.java.utils.Utils;

public class SparkJavaJobAnalyzer extends JavaStreamsJobAnalyzer {

	protected final String jobType = "sparkjava";

	protected String targetedDatasets = "(JavaRDD)"
			+ "(\\s*?(<\\s*?\\w*\\s*?(,\\s*?\\w*\\s*?)?\\s*?>))?"; //\\s*?\\w*\\s*?=";
	
	protected final String pushableTransformations = "(map|filter|flatMap)";
	protected final String pushableActions = "(collect|count|iterator|reduce)";
	
	
	//private final static String sparkJarLocation = "lib/spark-core_2.10-2.1.0.jar";
	//private final static String scalaJarLocation = "lib/scala-library-2.12.2.jar";
	
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
        String modifiedJobCode = originalJobCode;
        
        //First, get all the variables of type Stream, as they are the candidates to push down lambdas
        new StreamIdentifierVisitor(targetedDatasets, identifiedStreams).visit(cu, null);
        
        //Second, once we have the streams identified, we have to inspect each one looking for safe lambdas to push down      
        new StatementsExtractor(identifiedStreams, pushableTransformations, 
        		pushableActions, null).visit(cu, null); 

        //Third, we need to infer the types of the lambda functions to be compiled at the storage side
        for (String key: identifiedStreams.keySet())
        	findTypesOfLambdasInGraph(identifiedStreams.get(key));
        
        //If there are assignments of an RDD variable to another RDD variable, find the minimum set of 
        //lambdas that can be successfully executed at the storage side     
        identifiedStreams = new SafeLambdaMigrationFinder().computeMigrationGraph(identifiedStreams);
        
        System.out.println(">>>>>>>>>" + migrationRulesPackage);
        //Here, we need the intelligence to know what to pushdown   
        for (String key: identifiedStreams.keySet()){
        	applyRulesToControlFlowGraph(identifiedStreams.get(key), migrationRulesPackage);
        }      
        
        //Next, we need to update the job code in the case the flow graph has changed
        for (String key: identifiedStreams.keySet()){
        	applyRulesToControlFlowGraph(identifiedStreams.get(key), modificationRulesPackage);
        } 

        //Get all the lambdas from the graph that have been selected for pushdown
        List<SimpleEntry<String, String>> lambdasToMigrate = new ArrayList<>();
        for (String key: identifiedStreams.keySet()){
        	for (GraphNode node: identifiedStreams.get(key)){
        		if (node.getToPushdown()!=null)
        			lambdasToMigrate.add(new SimpleEntry<String, String>(node.getToPushdown(), 
        								node.getFunctionType()));
        		//Modify the original's job code according to modification rules
        		String toReplace = "";
        		if (node.getCodeReplacement()!="") toReplace =  "."+node.getCodeReplacement();
        			modifiedJobCode = modifiedJobCode.replace("."+node.getLambdaSignature(), toReplace);
        	}
        }  
        if (DEBUG) System.out.println(modifiedJobCode);
        //The control plane is in Python, so the caller script will need to handle this result
        //and distinguish between the lambdas to pushdown and the code of the job to submit
        return Utils.encodeResponse(originalJobCode, modifiedJobCode, lambdasToMigrate);
	}

}
