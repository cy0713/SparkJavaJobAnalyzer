package main.java;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.symbolsolver.SourceFileInfoExtractor;
import com.github.javaparser.symbolsolver.javaparser.Navigator;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.typesystem.Type;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

public class SymbolResolverTest {	
	
	private static final File src = new File(//"/media/raul/Data/Documentos/Recerca/Proyectos/IOStack/Code/SparkJavaJobAnalyzer/" +
			"src/test/resources/java8streams_jobs");

    private static SourceFileInfoExtractor getSourceFileInfoExtractor() {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(src));
        //combinedTypeSolver.add(new JavaParserTypeSolver(new File("src/test/resources/javaparser_src/generated")));
        SourceFileInfoExtractor sourceFileInfoExtractor = new SourceFileInfoExtractor();
        sourceFileInfoExtractor.setTypeSolver(combinedTypeSolver);
        sourceFileInfoExtractor.setPrintFileName(true);
        return sourceFileInfoExtractor;
    }
    
    static String readFile(File file) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        return new String(encoded, StandardCharsets.UTF_8);
    }
    
    public static void main(String[] args) {
    	
    	
    	CompilationUnit cu = null;
		try {
			cu = JavaParser.parse(new File(
					"/media/raul/Data/Documentos/Recerca/Proyectos/IOStack/Code/SparkJavaJobAnalyzer/src/test/resources/java8streams_jobs/HelloWorld.java"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
        /*com.github.javaparser.ast.body.ClassOrInterfaceDeclaration clazz = Navigator.demandClass(cu, "Agenda");
        MethodDeclaration method = Navigator.demandMethod(clazz, "lambdaMap");
        ReturnStmt returnStmt = Navigator.findReturnStmt(method);
        MethodCallExpr methodCallExpr = (MethodCallExpr) returnStmt.getExpression().get();
        Expression expression = methodCallExpr.getArguments().get(0);*/

		CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
	    combinedTypeSolver.add(new ReflectionTypeSolver());
	    combinedTypeSolver.add(new JavaParserTypeSolver(src));
	        
        JavaParserFacade javaParserFacade = JavaParserFacade.get(combinedTypeSolver);
        System.out.println(javaParserFacade.toString());
        //Type type = javaParserFacade.getType(expression);
        //System.out.println("java.util.function.Function<? super java.lang.String, ? extends java.lang.String>" +  type.describe());
    	
        for (Node node: cu.getChildNodes()){
			System.out.println(node.toString());
			System.out.println(javaParserFacade.getType(node));;
		}

    	/*String fileName = "HelloWorld";
    	File sourceFile = new File(src.getAbsolutePath() + "/" + fileName + ".java");
        SourceFileInfoExtractor sourceFileInfoExtractor = getSourceFileInfoExtractor();
        OutputStream outErrStream = new ByteArrayOutputStream();
        PrintStream outErr = new PrintStream(outErrStream);

        sourceFileInfoExtractor.setOut(outErr);
        sourceFileInfoExtractor.setErr(outErr);
        try {
			sourceFileInfoExtractor.solve(sourceFile);
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String output = outErrStream.toString();

        String path = "src/test/resources/javaparser_expected_output/" + fileName.replaceAll("/", "_") + ".txt";
        //File dstFile = adaptPath(new File(path));

        //if (DEBUG && (sourceFileInfoExtractor.getKo() != 0 || sourceFileInfoExtractor.getUnsupported() != 0)) {
            System.err.println(output);
        //}

        System.out.println("No failures expected when analyzing " + path + ": " + 
        		(0 == sourceFileInfoExtractor.getKo()));
        System.out.println("No UnsupportedOperationException expected when analyzing " + path + ": "+
        		(0 == sourceFileInfoExtractor.getUnsupported()));

        //String expected = readFile(dstFile);

        //String[] outputLines = output.split("\n");
        //String[] expectedLines = expected.split("\n");

        //for (int i = 0; i < Math.min(outputLines.length, expectedLines.length); i++) {
        //    assertEquals("Line " + (i + 1) + " of " + path + 
        //    		" is different from what is expected", expectedLines[i].trim(), outputLines[i].trim());
        //}

        //assertEquals(expectedLines.length, outputLines.length);*/
	}

}
