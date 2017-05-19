package pt.iscte.pandionj.parser2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;


public class TagParser {

	private List<VariableTags> variables;
	private JavaSourceParser parser;
	private CompilationUnit cunit;

	public TagParser(String path) {
		parser = JavaSourceParser.createFromFile(path);
		cunit = parser.getCompilationUnit();
		variables = new ArrayList<>();
	}

	public TagParser(IFile file) {
		this(file.getLocation().toOSString());
	}

	public Collection<String> getTags(String varName, int line) {
		for (VariableTags t : variables)
			if(t.name.equals(varName) && t.withinLine(line))
				return t.getTags();
		
		return Collections.emptyList();
	}


	public void run() {
		parser.parse(new TagVisitor());
		CommentVisitor commentVisitor = new CommentVisitor();
		for (Comment comment : (List<Comment>) cunit.getCommentList())
			comment.accept(commentVisitor);
	}

	class TagVisitor extends ASTVisitor {

		public boolean visit(SingleVariableDeclaration node) {
			String varName = node.getName().getIdentifier();
			int line = cunit.getLineNumber(node.getStartPosition());
			Scope scope = new Scope(node.getParent(), cunit);
			VariableTags tags = new VariableTags(varName, null, line, scope);
			variables.add(tags);
			return false;
		}
		
		@Override
		public boolean visit(VariableDeclarationStatement node) {
			VariableDeclarationFragment frag = (VariableDeclarationFragment) node.fragments().get(0);
			String varName = frag.getName().getIdentifier();
			int line = cunit.getLineNumber(node.getStartPosition());
			Scope scope = new Scope(node.getParent(), cunit);
			VariableTags tags = new VariableTags(varName, null, line, scope);
			variables.add(tags);
			return super.visit(node);
		}

		@Override
		public boolean visit(FieldDeclaration node) {
			VariableDeclarationFragment frag = (VariableDeclarationFragment) node.fragments().get(0);
			String varName = frag.getName().getIdentifier();
			int line = cunit.getLineNumber(node.getStartPosition());
			Scope scope = new Scope(node.getParent(), cunit);
			VariableTags tags = new VariableTags(varName, null, line, scope);
			variables.add(tags);
			return false;
		}
	}

	class CommentVisitor extends ASTVisitor {
		public boolean visit(LineComment node) {
			int start = node.getStartPosition();
			int end = start + node.getLength();
			int line = cunit.getLineNumber(start);
			String comment = parser.getSourceFragment(start, end);
			comment = comment.substring(comment.indexOf('/')+2).trim();
			String[] tags = comment.split("\\s+");
			
			for (VariableTags t : variables)
				if(t.declarationLine == line)
					t.addTags(tags);
			
			return true;
		}
	}
	
	private static class VariableTags {
		private final String name;
		private final String type;
		private final int declarationLine;
		private final Scope scope;
		private final List<String> tagList; // unique
		
		public VariableTags(String name, String type, int declarationLine, Scope scope) {
			this.name = name;
			this.type = type;
			this.declarationLine = declarationLine;
			this.scope = scope;
			this.tagList = new ArrayList<String>();
		}
		
		public boolean withinLine(int line) {
			return scope.contains(line);
		}

		public void addTags(String[] tags) {
			for(String t : tags)
				if(!tagList.contains(t))
					tagList.add(t);
		}
		
		@Override
		public String toString() {
			return name + " on scope " + scope + " (" + type + ") : " + tagList;
		}

		public Collection<String> getTags() {
			return Collections.unmodifiableList(tagList);
		}
	}

	private static class Scope {
		final int firstLine;
		final int lastLine;
		
		Scope(ASTNode node, CompilationUnit cunit) {
			this.firstLine = cunit.getLineNumber(node.getStartPosition());
			this.lastLine = cunit.getLineNumber(node.getStartPosition() + node.getLength());
		}
		
	
		boolean contains(int line) {
			return line >= firstLine && line <= lastLine;
		}
		
		boolean isInnerScopeOf(Scope scope) {
			return firstLine >= scope.firstLine && lastLine <= scope.lastLine;
		}
		
		@Override
		public String toString() {
			return "[" + firstLine + ", " + lastLine + "]";
		}
		
	}
	public static void main(String[] args) {
		TagParser parser = new TagParser("/Users/andresantos/git/pandionj2/pt.iscte.pandionj/src/pt/iscte/pandionj/parser2/ClassInfo.java");
		parser.run();

		Collection<String> tags = parser.getTags("name", 14);
		System.out.println(parser.getTags("name", 14));
		System.out.println(parser.getTags("name", 21));
		System.out.println(parser.getTags("img", 32));
		System.out.println(parser.variables);
	}


}