package main.java.analyzer.visitor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.typesystem.Type;

import main.java.graph.FlowControlGraph;

/**
 * This class performs the actual work of extracting the lambdas and operations
 * executed on the desired variables, and organize them as a control flow graph
 * to infer the operations that can be safely executed at the storage side.
 *
 */
public class StatementsExtractor extends VoidVisitorAdapter<Object> {
	
	private Map<String, FlowControlGraph> identifiedStreams;
	private String pushableTransformations;
	private String pushableActions;
	private JavaParserFacade javaParserFacade;
	
	public StatementsExtractor(Map<String, FlowControlGraph> identifiedStreams,
			String pushableIntermediateLambdas, String pushableTerminalLambdas,
				JavaParserFacade javaParserFacade) {
		this.identifiedStreams = identifiedStreams;
		this.pushableTransformations = pushableIntermediateLambdas;
		this.pushableActions = pushableTerminalLambdas;	
		this.javaParserFacade = javaParserFacade;
	}

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
			System.out.println("Processing lambda: " + n);
			//Take advantage of this pass to try to infer the types of the lambdas
			//Anyway, this will require a further process later on
			String lambdaType = getLambdaTypeFromNode(n);							
			Pattern pattern = Pattern.compile("\\." + pushableTransformations + 
								"+\\(?\\S+" + Pattern.quote(n.toString()) + "\\)");
	        Matcher matcher = pattern.matcher(expressionString);
	        //Add these lambda calls to the list of calls for the particular stream
	        try {
	        	matcher.find();
		        String matchedLambda = expressionString.substring(matcher.start()+1, matcher.end());
		        //We are treating reduce operations as final operations, but we need to know the types of inner lambdas
		        if (matchedLambda.startsWith("reduce")){
		        	identifiedStreams.get(streamKeyString).appendOperationToRDD(matchedLambda, lambdaType, true);
		        } else identifiedStreams.get(streamKeyString).appendOperationToRDD(matchedLambda, lambdaType, false);
		        lastLambdaIndex = matcher.end();
	        }catch(IllegalStateException e) {
	        	System.err.println("Error parsing the lambda. Probably you need to add how to "
	        			+ "treat the following function in this code: " + expressionString);
	        	e.printStackTrace();
	        }
		}			
		if (lastLambdaIndex==0) lastLambdaIndex=expressionString.indexOf(".");
		Pattern pattern = Pattern.compile("\\." + pushableActions);
		Matcher matcher = pattern.matcher(expressionString.substring(lastLambdaIndex));
		//We enable only a single collector in the expression, if it does exist
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

	public String getLambdaTypeFromNode(Node n) {
		if (javaParserFacade==null) return "";
		Type type = javaParserFacade.getType(n, true);
		//Clean the raw input information coming from JSS
		String typeString = type.describe().replace(" ? extends ? super ", "")	
										   .replace("? super ", "")
										   .replace("? extends ", "")
										   .replace(" int", " java.lang.Integer");
		System.out.println("Type found by JSS: " + typeString);
		return typeString;
	}     	
	
	/**
	 * This class is intended to visit all the lambda functions of a code file.
	 * With this visitor, we can extract easily the candidate functions to be 
	 * migrated to the storage.
	 *
	 */
	@SuppressWarnings("unchecked")
	protected class LambdaExtractor extends VoidVisitorAdapter<Object> {		
		@Override 
		public void visit(LambdaExpr n, Object arg){
			List<Node> lambdas = (List<Node>) arg;
			lambdas.add(n);	
		}
	}
}