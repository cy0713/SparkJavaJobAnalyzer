package main.java.analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.util.stream.Stream;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class MethodChanger {

    public static void main(String[] args) throws Exception {
        // parse a file
        CompilationUnit cu = JavaParser.parse(
        	new FileInputStream("/media/raul/Data/Documentos/Recerca/"
        		+ "Proyectos/IOStack/Code/SparkJavaJobAnalyzer/src/test/resources/LogAnalyzer.java"));

        // visit and change the methods names and parameters
        new MyVisitor().visit(cu, null);

        // prints the changed compilation unit
        //System.out.println(cu);
    }

    /**
     * Simple visitor implementation for visiting MethodDeclaration nodes.
     */
    private static class MethodChangerVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(MethodDeclaration n, Void arg) {
            // change the name of the method to upper case
            n.setName(n.getNameAsString().toUpperCase());

            // add a new parameter to the method
            n.addParameter("int", "value");
        }
    }
    
    private static class MyVisitor extends ModifierVisitor<Void> {
        @Override
        public Node visit(VariableDeclarator declarator, Void args) {
        	if (declarator.getType().toString().equals("Stream<String>")){
        		System.out.println(declarator.toString());
        		for (Node n: declarator.getChildNodes()){
        			System.out.println(n.toString());
        		}
        	}
            /*if (declarator.getNameAsString().equals("a")
                    // the initializer is optional, first check if there is one
                    && declarator.getInitializer().isPresent()) {
                Expression expression = declarator.getInitializer().get();
                // We're looking for a literal integer.
                if (expression instanceof IntegerLiteralExpr) {
                    // We found one. Is it literal integer 20?
                    if (((IntegerLiteralExpr) expression).getValue().equals("20")) {
                        // Returning null means "remove this VariableDeclarator"
                        return null;
                    }
                }
            }*/
            return declarator;
        }
    }
}