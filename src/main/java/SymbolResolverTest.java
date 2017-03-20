package main.java;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.github.javaparser.ParseException;
import com.github.javaparser.symbolsolver.SourceFileInfoExtractor;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;

public class SymbolResolverTest {	
	
	private static final File src = new File("src/test/resources/java8streams_jobs/");

    private static SourceFileInfoExtractor getSourceFileInfoExtractor() {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        //combinedTypeSolver.add(new ReflectionTypeSolver());
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
    	String fileName = "SimpleLogAnalyzer";
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
        /*File dstFile = adaptPath(new File(path));

        if (DEBUG && (sourceFileInfoExtractor.getKo() != 0 || sourceFileInfoExtractor.getUnsupported() != 0)) {
            System.err.println(output);
        }

        assertTrue("No failures expected when analyzing " + path, 0 == sourceFileInfoExtractor.getKo());
        assertTrue("No UnsupportedOperationException expected when analyzing " + path, 0 == sourceFileInfoExtractor.getUnsupported());

        String expected = readFile(dstFile);

        String[] outputLines = output.split("\n");
        String[] expectedLines = expected.split("\n");

        for (int i = 0; i < Math.min(outputLines.length, expectedLines.length); i++) {
            assertEquals("Line " + (i + 1) + " of " + path + " is different from what is expected", expectedLines[i].trim(), outputLines[i].trim());
        }

        assertEquals(expectedLines.length, outputLines.length);*/
	}

}
