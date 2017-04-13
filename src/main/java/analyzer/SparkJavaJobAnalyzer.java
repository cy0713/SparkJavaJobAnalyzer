package main.java.analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.typesystem.Type;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import main.java.graph.FlowControlGraph;
import main.java.graph.GraphNode;
import main.java.graph.algorithms.SafeLambdaMigrationFinder;
import main.java.rules.LambdaRule;

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
public class SparkJavaJobAnalyzer {
	
	private HashMap<String, FlowControlGraph> identifiedStreams = new HashMap<String, FlowControlGraph>();
	
	private final String targetedDatasets = "(Stream|RDD|JavaRDD|JavaPairRDD)"
			+ "(\\s*?(<\\s*?\\w*\\s*?(,\\s*?\\w*\\s*?)?\\s*?>))?"; //\\s*?\\w*\\s*?=";
	
	private final String pushableIntermediateLambdas = "(map|filter|flatMap|collect|mapToPair|reduceByKey)";
	private final String pushableTerminalLambdas = "(collect|count|iterator)";
	//private final String RDDActions = "(count|cache)";
	
	private final static String migrationRulesPackage = "main.java.rules.migration.";
	private final static String modificationRulesPackage = "main.java.rules.modification.";
	private final static String srcToAnalyze = "src/"; ///resources/java8streams_jobs/";

	private static final String LAMBDA_TYPE_AND_BODY_SEPARATOR = "|";
	
	private JavaParserFacade javaParserFacade;
	
	public String analyze (String fileToAnalyze) {
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
        
        //Build the object to infer types of lambdas from source code
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(new File(srcToAnalyze)));
        javaParserFacade = JavaParserFacade.get(combinedTypeSolver);
        
        //First, get all the variables of type Stream, as they are the candidates to push down lambdas
        new StreamIdentifierVisitor().visit(cu, null);
        
        //Second, once we have the streams identified, we have to inspect each one looking for safe lambdas to push down      
        new StatementsExtractor().visit(cu, null);          
        
        //Third, we need to infer the types of the lambda functions to be compiled at the storage side
        for (String key: identifiedStreams.keySet())
        	findTypesOfLambdasInGraph(identifiedStreams.get(key));
        
        //TODO: Big Challenge: if there are assignments of an RDD variable to another RDD variable, 
        //find the minimum set of lambdas that can be successfully executed at the storage side     
        identifiedStreams = new SafeLambdaMigrationFinder().computeMigrationGraph(identifiedStreams);
        
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
        		System.out.println("AQUI TIENE QUE ESTAAR: " + node.toString());
        		if (node.getToPushdown()!=null)
        			lambdasToMigrate.add(new SimpleEntry<String, String>(node.getToPushdown(), 
        								node.getFunctionType()));
        		//Modify the original's job code according to modification rules
        		String toReplace = "";
        		if (node.getCodeReplacement()!="") toReplace =  "."+node.getCodeReplacement();
        			modifiedJobCode = modifiedJobCode.replace("."+node.getLambdaSignature(), toReplace);
        	}
        }  
        System.out.println(modifiedJobCode);
        //The control plane is in Python, so the caller script will need to handle this result
        //and distinguish between the lambdas to pushdown and the code of the job to submit
        return encodeResponse(originalJobCode, modifiedJobCode, lambdasToMigrate);
	}

	private void applyRulesToControlFlowGraph(FlowControlGraph flowControlGraph, String rulesPackage) {
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

	private void findTypesOfLambdasInGraph(FlowControlGraph flowControlGraph) {	
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

	/**
	 * This class is intended to identify the variables (e.g., RDDs, Streams)
	 * that will be object of optimization by sending some of the operations
	 * executed on them to the storage.
	 *
	 */
	private class StreamIdentifierVisitor extends ModifierVisitor<Void> {
		
		private Pattern datasetsPattern = Pattern.compile(targetedDatasets);
		
		@Override
	    public Node visit(VariableDeclarator declarator, Void args) {	
			//TODO: Limitation here, we need a variable declared to find it, so this
			//does not work with an anonymous declaration like createStream().stream().lambdas...
			Matcher matcher = datasetsPattern.matcher(declarator.getType().toString());
			//Check if we found and in memory data structure like an RDD
	     	if (matcher.find()){
	     		String streamVariable = declarator.getChildNodes().get(0).toString();
	     		FlowControlGraph graph = new FlowControlGraph(streamVariable);
	     		identifiedStreams.put(streamVariable, graph);
	     		String name = declarator.getChildNodes().get(1).toString().trim();
	     		//Maybe there is an even simpler way of doing this
	     		Optional<String> referencedRDD = Arrays.stream(name.split("\\."))
	     											.filter(s -> identifiedStreams.containsKey(s))
	     											.findFirst();
	     		//Here we note that this RDD comes from another one
	     		if (referencedRDD.isPresent())
	     			graph.setOiriginRDD(referencedRDD.get());
	     	}	 
			return declarator;
		 }
	}	
	
	/**
	 * This class performs the actual work of extracting the lambdas and operations
	 * executed on the desired variables, and organize them as a control flow graph
	 * to infer the operations that can be safely executed at the storage side.
	 *
	 */
	private class StatementsExtractor extends VoidVisitorAdapter<Object> {
		@Override
        public void visit(MethodCallExpr methodExpression, Object arg) {				
			System.out.println("methodExpression: " + methodExpression);	        
			//Check if the current expression is related to any stream of interest
			boolean isExpressionOnStream = false;
			String streamKeyString = "";
			for (String streamKey: identifiedStreams.keySet()){
				isExpressionOnStream = methodExpression.toString().contains(streamKey+".".toString());
				if (isExpressionOnStream) {
					streamKeyString = streamKey;
					break;
				}
			}
			//If this line is not interesting to us, just skip
			if (!isExpressionOnStream) return;		

			//If the current RDD comes from another one, link it to the graph of the original
			//This is necessary to compute afterwards what computations to migrate without impacting the results
			FlowControlGraph flowGraph = identifiedStreams.get(streamKeyString);
			if (flowGraph.getOiriginRDD()!=null && !flowGraph.isLinked()){
				identifiedStreams.get(flowGraph.getOiriginRDD()).getLastNode().getAssignedRDDs().add(flowGraph);
				flowGraph.setLinked(true);
			}
			
			//Leave only the expression that is interesting to us, on the stream variable. We need this
			//as the expression can be within a System.out.print() method, for example
			Expression innerLambdaCall = null;
			boolean foundCorrectExpression = methodExpression.toString().startsWith(streamKeyString);
			int expressionIndex = 0;
			while (!foundCorrectExpression){
				innerLambdaCall = methodExpression.getArgument(expressionIndex);
				foundCorrectExpression = innerLambdaCall.toString().startsWith(streamKeyString);
			}

			//Find the lambdas in the (hopefully) clean expression
			List<Node> lambdas = new LinkedList<>();			
			String expressionString = "";
			if (innerLambdaCall != null){
				innerLambdaCall.accept(new LambdaExtractor(), lambdas);				
				expressionString = innerLambdaCall.toString();
			}else{
				methodExpression.accept(new LambdaExtractor(), lambdas);
				expressionString = methodExpression.toString();
			}			
			//Store the lambdas in the correct order, as they are executed
			Collections.reverse(lambdas);
			
			//Get the entire intermediate lambda functions that can be pushed down
			int lastLambdaIndex = 0;
			for (Node n: lambdas){    		
				System.out.println("->>>" + n);
				//Take advantage of this pass to try to infer the types of the lambdas
				//Anyway, this will require a further process later on
				String lambdaType = getLambdaTypeFromNode(n);							
				Pattern pattern = Pattern.compile("\\." + pushableIntermediateLambdas + 
									"+\\(?\\S+" + Pattern.quote(n.toString()) + "\\)");
		        Matcher matcher = pattern.matcher(expressionString);
		        //Add these lambda calls to the list of calls for the particular stream
		        try {
		        	matcher.find();
			        String matchedLambda = expressionString.substring(matcher.start()+1, matcher.end());
			        identifiedStreams.get(streamKeyString).appendOperationToRDD(matchedLambda, lambdaType, false);
			        lastLambdaIndex = matcher.end();
		        }catch(IllegalStateException e) {
		        	System.err.println("Error parsing the lambda. Probably you need to add how to "
		        			+ "treat the following function in this code: " + expressionString);
		        	e.printStackTrace();
		        }
			}			
			
			Pattern pattern = Pattern.compile("\\." + pushableTerminalLambdas);
			Matcher matcher = pattern.matcher(expressionString.substring(lastLambdaIndex));
			//There is only a single terminal operation in the expression
			if (matcher.find()){
				int pos = lastLambdaIndex+matcher.end()+1;
				int openBr = 1;
				while (openBr!=0) {
					if (expressionString.charAt(pos)=='(') openBr++;
					if (expressionString.charAt(pos)==')') openBr--;
					pos++;
				}
				String matchedAction = expressionString.substring(lastLambdaIndex+1, pos);
				lastLambdaIndex = pos;
				//At the moment, we do not need to know the collector type parameterization
				identifiedStreams.get(streamKeyString).appendOperationToRDD(matchedAction, "Collector", true);
			}
    	}

		private String getLambdaTypeFromNode(Node n) {
			try {
				Type type = javaParserFacade.getType(n, true);
				//Clean the raw input information coming from JSS
				String typeString = type.describe().replace(" ? extends ? super ", "")	
												   .replace("? super ", "")
												   .replace("? extends ", "")
												   .replace(" int", " java.lang.Integer");
				System.out.println("Type found by JSS: " + typeString);
				return typeString;
			}catch(RuntimeException e){
				System.err.println("Unable to find type for lambda: " + n);
			}	
			return null;
		}     	
    }
	
	/**
	 * This class is intended to visit all the lambda functions of a code file.
	 * With this visitor, we can extract easily the candidate functions to be 
	 * migrated to the storage.
	 *
	 */
	@SuppressWarnings("unchecked")
	private class LambdaExtractor extends VoidVisitorAdapter<Object> {		
		@Override 
		public void visit(LambdaExpr n, Object arg){
			List<Node> lambdas = (List<Node>) arg;
			lambdas.add(n);	
		}
	}
	
	/**
	 * This method is intended to return to an external program a JSON String response with
	 * both the lambdas to send to the storage and the final version of the job to execute
	 * at the Spark cluster.
	 * 
	 * @param lambdasToMigrate
	 * @param cu
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String encodeResponse(String originalJob, String modifiedJob, 
									List<SimpleEntry<String, String>> lambdasToMigrate) {
		JSONObject obj = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		
		for (SimpleEntry<String, String> lambda: lambdasToMigrate){
			System.out.println(lambda);
			JSONObject lambdaObj = new JSONObject();
			lambdaObj.put("lambda-type-and-body", lambda.getValue() + 
					LAMBDA_TYPE_AND_BODY_SEPARATOR + lambda.getKey());
			jsonArray.add(lambdaObj); 
		}
		//Separator between lambdas and the job source code
		obj.put("original-job-code", originalJob);	
		obj.put("pushdown-job-code", modifiedJob);	
		obj.put("lambdas", jsonArray);
		return obj.toString();
	}
}