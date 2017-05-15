package main.java.analyzer.visitor;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.ModifierVisitor;

import main.java.graph.FlowControlGraph;

/**
 * This class is intended to identify the variables (e.g., RDDs, Streams)
 * that will be object of optimization by sending some of the operations
 * executed on them to the storage.
 *
 */
public class StreamIdentifierVisitor extends ModifierVisitor<Void> {
	
	public Pattern datasetsPattern;
	public Map<String, FlowControlGraph> identifiedStreams;
	
	public StreamIdentifierVisitor(String targetedDatasets, Map<String, FlowControlGraph> identifiedStreams) {
		this.datasetsPattern = Pattern.compile(targetedDatasets);
		this.identifiedStreams = identifiedStreams;
	}

	@Override
    public Node visit(VariableDeclarator declarator, Void args) {	
		//FIXME: Limitation here, we need a variable declared to find it, so this
		//does not work with an anonymous declaration like createStream().stream().lambdas...
		Matcher matcher = datasetsPattern.matcher(declarator.getType().toString());
		//Check if we found and in memory data structure like an RDD
     	if (matcher.matches()){
     		String streamVariable = declarator.getChildNodes().get(0).toString();
     		FlowControlGraph graph = new FlowControlGraph(streamVariable);
     		graph.setType(declarator.getType().toString());
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