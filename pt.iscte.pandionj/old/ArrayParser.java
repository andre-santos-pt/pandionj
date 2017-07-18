package pt.iscte.pandionj.parser2;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;


public class ArrayParser {

	private JavaSourceParser parser;
	private CompilationUnit cunit;

	public ArrayParser(String path) {
		parser = JavaSourceParser.createFromFile(path);
		cunit = parser.getCompilationUnit();
	}

	public ArrayParser(IFile file) {
		this(file.getLocation().toOSString());
	}



	public void run() {
		parser.parse(new MethodVisitor());
	}

	
	
	class MethodVisitor extends ASTVisitor {
		
//		@Override
//		public boolean visit(FieldDeclaration node) {
//			System.out.println(node.getType());
//			return true;
//		}
		
//		@Override
//		public boolean visit(Block node) {
//			ArrayIndexVisitor v = new ArrayIndexVisitor(node);
//			node.accept(v);
//			System.out.println(node + " :: ");
//			return false;
//		}
		
		@Override
		public boolean visit(ForStatement node) {
			Statement body = node.getBody();
			Expression condExp = node.getExpression();
			InfixExpression exp = (InfixExpression) condExp;
			System.out.println(node.initializers() + " :: " + condExp + " :: " + body);
			return super.visit(node);
		}
	}
	
	
	
	
	
	
	
	
	
	class ArrayIndexVisitor extends ASTVisitor {
		Block block;
		
		public ArrayIndexVisitor(Block block) {
			this.block = block;
		}
		
		@Override
		public boolean visit(VariableDeclarationStatement node) {
			List fragments = node.fragments();
			VariableDeclarationFragment varDec = (VariableDeclarationFragment) fragments.get(0);
			System.out.println(varDec.getName().getIdentifier() + "  " + node.getParent());
			return super.visit(node);
		}

		@Override
		public boolean visit(ArrayAccess node) {
			Expression indexExp = node.getIndex();
			if(indexExp instanceof SimpleName) {
				SimpleName varName = (SimpleName) indexExp;
				System.out.println(node + " : " + varName.getIdentifier());
			}
			return super.visit(node);
		}

		

	}

	void test1() {
		int[] v = new int[3];
		int i = 0;
		v[i] = 8;
		v[2] = 9;
		
		for(int j = 0; j < v.length; j++)
			v[j] = 10;
		
		{
			int x = 0;
		}
		
		{
			int x = 0;
		}
	}

	static void test2() { 

	}



	public static void main(String[] args) {

		ArrayParser parser = new ArrayParser("/Users/andresantos/git/pandionj2/pt.iscte.pandionj/src/pt/iscte/pandionj/parser2/ArrayParser.java");
		parser.run();

	}

}
