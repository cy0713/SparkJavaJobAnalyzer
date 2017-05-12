package main.java.analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONObject;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import main.java.analyzer.visitor.StatementsExtractor;
import main.java.analyzer.visitor.StreamIdentifierVisitor;
import main.java.graph.FlowControlGraph;
import main.java.graph.GraphNode;
import main.java.graph.algorithms.SafeLambdaMigrationFinder;
import main.java.rules.LambdaRule;
import main.java.utils.Utils;

/**
 * We consider Spark jobs in Java as input for this class. Given that, 
 * this class is intended to execute:
 * 1.- Extract transformations and operations from RDDs
 * 2.- Create a {@link FlowControlGraph} object per RDD
 * 3.- Analyze such graph to see if we can push down lambdas or not
 * 4.- Modify the original file in the case a pushdown operation needs it
 * 
 * The ultimate objective is to execute specific parts of the job in a stream
 * fashion at the storage side (push down lambdas) and the remaining logic (modified 
 * Spark job) as a usual job, so we obtain the same result but things like filtering,
 * sums, min/max, and so on can be partially computed at the storage side.
 * 
 * @author Raul Gracia
 *
 */
public class JavaStreamsJobAnalyzer {
	
	protected final String jobType = "javastreams";
	
	protected HashMap<String, FlowControlGraph> identifiedStreams = new HashMap<String, FlowControlGraph>();
	
	protected static String targetedDatasets = "(Stream)"
			+ "(\\s*?(<\\s*?\\w*\\s*?(,\\s*?\\w*\\s*?)?\\s*?>))?"; //\\s*?\\w*\\s*?=";
	
	/* These are the operation that we currently can detect/migrate */
	protected final static String pushableTransformations = "(map|filter|flatMap|reduce)";
	protected final static String pushableActions = "(collect|count|iterator|forEach)";	
	
	protected final String migrationRulesPackage = "main.java.rules.migration." + jobType + ".";
	protected final String modificationRulesPackage = "main.java.rules.modification." + jobType  + ".";
	
	/* ********************************************* */
	/* IMPORTANT: For tests to work, this MUST be true. For a jar release, this MUST be false.*/
	/* ********************************************* */
	protected static boolean DEBUG = false;
	protected final static String defaultSrcToAnalyze = "src/"; //For testing purposes
	
	protected JavaParserFacade javaParserFacade;
	
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
        
        //Build the type solver for extracting the types of functions
        javaParserFacade = JavaParserFacade.get(getTypeSolver(fileToAnalyze));
        
        //First, get all the variables of type Stream, as they are the candidates to push down lambdas
        new StreamIdentifierVisitor(targetedDatasets, identifiedStreams).visit(cu, null);
        
        //Second, once we have the streams identified, we have to inspect each one looking 
        //for safe lambdas to push down      
        new StatementsExtractor(identifiedStreams, pushableTransformations, 
        		pushableActions, javaParserFacade).visit(cu, null);          
        
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
	
	public JSONObject analyze (String fileToAnalyze, boolean debug) {
		DEBUG = debug;
		return analyze(fileToAnalyze);
	}
	
	protected CombinedTypeSolver getTypeSolver(String fileToAnalyze){
		//Build the object to infer types of lambdas from source code
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        
        //TODO: Here we assume that any necessary type to be resolved for the file is within the parent dir
        String sourceCodeDirToAnalyze = new File(fileToAnalyze).getParentFile().getAbsolutePath();
        if (DEBUG) sourceCodeDirToAnalyze = defaultSrcToAnalyze;
        combinedTypeSolver.add(new JavaParserTypeSolver(new File(sourceCodeDirToAnalyze)));
        return combinedTypeSolver;
	}

	protected void applyRulesToControlFlowGraph(FlowControlGraph flowControlGraph, String rulesPackage) {
        LambdaRule pushdownLambdaRule = null;
        for (GraphNode node: flowControlGraph){  
        	System.out.println(rulesPackage + ": " + node.toString());
        	String functionName = node.getFunctionName();
			try {
				//Instantiate the class that contains the rules to pushdown a given lambda
				pushdownLambdaRule = (LambdaRule) Class.forName(
					rulesPackage + new String(functionName.substring(0, 1)).toUpperCase() +
						functionName.substring(1, functionName.length())).newInstance();
				//Get whether the current lambda can be pushed down or not
				pushdownLambdaRule.applyRule(node);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				System.err.println("No rule for lambda: " + functionName + " in " + rulesPackage);
			}
        }		
	}

	protected void findTypesOfLambdasInGraph(FlowControlGraph flowControlGraph) {	
		//Here we try to fill the missing types of lambdas in the graph
		for (GraphNode node: flowControlGraph){
			//Not necessary to find types for terminal operations
			if (node.isTerminal()) continue;
			LambdaTypeParser lambdaTypeParser = new LambdaTypeParser(node.getFunctionType());
			//If the type is correctly set, go ahead
			if (lambdaTypeParser.isTypeWellDefined()) continue;
			//Otherwise, we have to check how to infer it
			node.setFunctionType(lambdaTypeParser.solveTypeFromGraph(node));
		}			
	}
}