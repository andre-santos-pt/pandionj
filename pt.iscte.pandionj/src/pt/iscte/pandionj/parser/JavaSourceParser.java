package pt.iscte.pandionj.parser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;


public class JavaSourceParser {
	private final ASTParser parser;
	private char[] source;
	
	private JavaSourceParser(String source, String className) {
		this.source = source.toCharArray();
		parser = ASTParser.newParser(AST.JLS8);
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);		
		parser.setEnvironment(null, new String[] {}, new String[] {}, true);
		parser.setUnitName(className);
		parser.setSource(this.source);
		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		parser.setCompilerOptions(options);
	}
	
	public static JavaSourceParser createFromFile(String javaFilePath) {
		validateFilePath(javaFilePath);
		return new JavaSourceParser(readFileToString(javaFilePath), getClassName(javaFilePath));
	}
	
	public static JavaSourceParser createFromSource(String source, String className) {
		return new JavaSourceParser(source, className);
	}

	private static void validateFilePath(String filePath) {
		File f = new File(filePath);
		if(!f.exists()) throw new IllegalArgumentException(filePath + " does not exist");
		if(!f.isFile())	throw new IllegalArgumentException(filePath + " is not a file");
		if(!f.getAbsolutePath().endsWith(".java")) throw new IllegalArgumentException(filePath + " is not a java file (.java)");
	}

	public void parse(ASTVisitor visitor) {
		parser.setSource(this.source);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		
//		if(unit.getProblems().length > 0)
//			throw new RuntimeException("code has compilation errors");
		
		unit.accept(visitor);
	}

	private static String readFileToString(String filePath) {
		StringBuilder fileData = new StringBuilder(1000);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));

			char[] buf = new char[10];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
				buf = new char[1024];
			}
			reader.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return fileData.toString();	
	}
	
	private static String getClassName(String javaFilePath) {
		String trim = javaFilePath.substring(0, javaFilePath.lastIndexOf('.'));
		trim = trim.substring(trim.lastIndexOf(File.separatorChar)+1);
		return trim;
	}

	public String getSourceFragment(int start, int length) {
		return new String(source, start, length);
//		return source.substring(start, end);
	}
	
	public CompilationUnit getCompilationUnit() {
		return (CompilationUnit) parser.createAST(null);
	}
}