package main.java.analyzer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

import main.java.graph.FlowControlGraph;
import main.java.graph.GraphNode;
import main.java.migration_rules.IPushableTransformation;

//1.- Extract transformations and operations from RDDs
//2.- Create a Control Flow Graph
//3.- Analyze such graph to see if we can push down lambdas or not
//4.- Modify the original file in the case a pushdown operation needs it

public class SparkJavaJobAnalyzer {
	
	private HashMap<String, FlowControlGraph> identifiedStreams = new HashMap<String, FlowControlGraph>();
	
	private final String targetedDatasets = "(Stream|RDD|JavaRDD|JavaPairRDD)"
			+ "(\\s*?(<\\s*?\\w*\\s*?(,\\s*?\\w*\\s*?)?\\s*?>))?"; //\\s*?\\w*\\s*?=";
	private final String pushableLambdas = "(map|filter|flatMap|mapToPair|reduceByKey)";
	private final String RDDActions = "(count|cache)";
	
	private final static String migrationRulesPackage = "main.java.migration_rules.";
	
	public String analyze (String fileToAnalyze) {
		FileInputStream in = null;
		
		try {
			in = new FileInputStream(fileToAnalyze);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		//Parse the file
        CompilationUnit cu = JavaParser.parse(in);  
        
        //First, get all the variables of type Stream, as they are the candidates to push down lambdas
        StreamIdentifierVisitor streamIdentifierVisitor = new StreamIdentifierVisitor();
        streamIdentifierVisitor.visit(cu, null);
        
        //Once we have the streams identified, we have to inspect each one looking for safe lambdas to push down      
        new StatementsExtractor().visit(cu, null);          
        
        //Here, we need the intelligence to know what to pushdown 
        IPushableTransformation pushdownLambdaRule = null;        
        List<String> lambdasToMigrate = new ArrayList<>();
        for (String key: identifiedStreams.keySet()){
        	for (GraphNode node: identifiedStreams.get(key)){   		
        		
        		String functionName = node.getFunctionName();
        		System.out.println("PARSED FUNCTION: " + node.getToExecute());
        		String executionResult = null;
        		try {
        			//Instantiate the class that contains the rules to pushdown a given lambda
					pushdownLambdaRule = (IPushableTransformation) Class.forName(
						migrationRulesPackage + new String(functionName.substring(0, 1)).toUpperCase() +
							functionName.substring(1, functionName.length())).newInstance();
					//Get whether the current lambda can be pushed down or not
					executionResult = pushdownLambdaRule.pushdown(node);
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
					System.err.println("No migration rule for lambda: " + functionName);
				}
    			if (executionResult!=null) 
    				lambdasToMigrate.add(executionResult);        		
        	}
        }      
        
        //TODO: Big Challenge: if there are assignments of an RDD variable to another RDD variable, 
        //find the minimum set of lambdas that can be successfully executed at the storage side
        
        //Next, we need to update the job code in the case the flow graph has changed
        
        //The control plane is in Python, so the caller script will need to handle this result
        //and distinguish between the lambdas to pushdown and the code of the job to submit
        return encodeResponse(lambdasToMigrate, cu);
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
			Matcher matcher = datasetsPattern.matcher(declarator.getType().toString());
	     	if (matcher.find()){
	     		String streamVariable = declarator.getChildNodes().get(0).toString();
	     		System.out.println("Adding variable to CFG: " +
	     				declarator.getType().toString() + " " + streamVariable);
	     		identifiedStreams.put(streamVariable, new FlowControlGraph(streamVariable));
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
			//Check if the current expression is related to any stream
			boolean isExpressionOnStream = false;
			String streamKeyString = "";
			for (String streamKey: identifiedStreams.keySet()){
				//FIXME: This should be done with nodes, not with strings but is not working...
				isExpressionOnStream = methodExpression.toString().contains(streamKey+".".toString());
				if (isExpressionOnStream) {
					streamKeyString = streamKey;
					break;
				}
			}
			//If this line is not interesting to us, just skip
			if (!isExpressionOnStream) return;			
			
			//Leave only the expression that is interesting to us, on the stream variable
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
			
			List<String> parsedLambdas = new ArrayList<>();
			
			//Get the entire lambda functions that can be offloaded
			for (Node n: lambdas){        		
				Pattern pattern = Pattern.compile("\\." + pushableLambdas + "+\\(?\\S+" + 
										Pattern.quote(n.toString()) + "?\\S+\\)");
		        Matcher matcher = pattern.matcher(expressionString);

		        //Add these lambda calls to the list of calls for the particular stream
		        try {
		        	matcher.find();
			        String matchedLambda = expressionString.substring(matcher.start()+1, matcher.end());
			        parsedLambdas.add(matchedLambda);	
		        }catch(IllegalStateException e) {
		        	System.err.println("Error parsing the lambda. Probably you need to add how to "
		        			+ "treat the following function in this code: " + expressionString);
		        	e.printStackTrace();
		        }
			}			
			//Store the lambdas in the correct order, as they are executed
			Collections.reverse(parsedLambdas);
			Pattern pattern = Pattern.compile("\\." + RDDActions + "+\\(\\)");
			//Add the found lambdas and actions to the flow control graph
			for (String theLambda: parsedLambdas){
				Matcher matcher = pattern.matcher(theLambda);
		        if (matcher.find()){
		        	String matchedAction = theLambda.substring(matcher.start()+1, matcher.end());
		        	identifiedStreams.get(streamKeyString).appendOperationToRDD(theLambda.substring(0, matcher.start()), true);
		        	identifiedStreams.get(streamKeyString).appendOperationToRDD(matchedAction, false);
		        }else identifiedStreams.get(streamKeyString).appendOperationToRDD(theLambda, true);
			}
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
	private String encodeResponse(List<String> lambdasToMigrate, CompilationUnit cu) {
		JSONObject obj = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		
		for (String lambda: lambdasToMigrate){
			System.out.println(lambda);
			jsonArray.add(lambda); 
		}
		//Separator between lambdas and the job source code
		obj.put("job-code", cu.toString());		
		obj.put("lambdas", jsonArray);
		return obj.toString();
	}
}