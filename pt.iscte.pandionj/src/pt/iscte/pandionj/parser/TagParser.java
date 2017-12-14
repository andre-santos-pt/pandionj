package pt.iscte.pandionj.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;



public class TagParser {

	private List<VariableTags> variables;
	private JavaSourceParser parser;
	private CompilationUnit cunit;

	private Set<String> validTags;

	public TagParser(String path, Set<String> validTags) {
		parser = JavaSourceParser.createFromFile(path);
		cunit = parser.getCompilationUnit();
		variables = new ArrayList<>();
		this.validTags = validTags;
	}

	public TagParser(IFile file, Set<String> validTags) {
		this(file.getLocation().toOSString(), validTags);
	}

	public Collection<String> getTags(String varName, int line, boolean isField) {
		if(isField) {
			for (VariableTags t : variables)
				if(t.name.equals(varName) && t.isField == true)
					return t.getTags();
		}
		else {
			for (VariableTags t : variables)
				if(t.name.equals(varName) && t.withinLine(line))
					return t.getTags();
		}

		return Collections.emptyList();
	}

	public Collection<String> getAttributeTags(String typeName, String attName) {
		for (VariableTags t : variables)
			if(typeName.equals(t.type) && attName.equals(t.name))
				return t.getTags();

		return Collections.emptyList();
	}


	public void run() {
		parser.parse(new TagVisitor());
		CommentVisitor commentVisitor = new CommentVisitor();
		for (Comment comment : (List<Comment>) cunit.getCommentList())
			comment.accept(commentVisitor);

		Collections.sort(variables);
	}

	@Override
	public String toString() {
		return variables.stream().filter((v) -> !v.tags.isEmpty()).collect(Collectors.toList()).toString();
	}

	class TagVisitor extends ASTVisitor {

		public boolean visit(SingleVariableDeclaration node) {
			String varName = node.getName().getIdentifier();
			int line = cunit.getLineNumber(node.getStartPosition());
			Scope scope = new Scope(line, getEndLine(node.getParent(), cunit));
			VariableTags tags = new VariableTags(varName, null, line, scope, false);
			variables.add(tags);
			return false;
		}

		@Override
		public boolean visit(VariableDeclarationStatement node) {
			VariableDeclarationFragment frag = (VariableDeclarationFragment) node.fragments().get(0);
			String varName = frag.getName().getIdentifier();
			int line = cunit.getLineNumber(node.getStartPosition());
			Scope scope = new Scope(line, getEndLine(node.getParent(), cunit));
			VariableTags tags = new VariableTags(varName, null, line, scope, false);
			variables.add(tags);
			return super.visit(node);
		}

		@Override
		public boolean visit(FieldDeclaration node) {
			VariableDeclarationFragment frag = (VariableDeclarationFragment) node.fragments().get(0);
			String varName = frag.getName().getIdentifier();
			int line = cunit.getLineNumber(node.getStartPosition());
			ASTNode parent = node.getParent();
			Scope scope = new Scope(cunit.getLineNumber(parent.getStartPosition()), getEndLine(parent, cunit));
			TypeDeclaration dec = (TypeDeclaration) node.getParent();
			String qName = dec.getName().getFullyQualifiedName();
			PackageDeclaration packageDec = cunit.getPackage();
			if(packageDec != null)
				qName = packageDec.getName().getFullyQualifiedName() + "." + qName;
			String type = !Modifier.isStatic(node.getModifiers()) ? qName : null; 
			VariableTags tags = new VariableTags(varName, type, line, scope, true);
			variables.add(tags);
			return false;
		}
	}

	private static int getEndLine(ASTNode node, CompilationUnit cunit) {
		return cunit.getLineNumber(node.getStartPosition() + node.getLength());
	}

	class CommentVisitor extends ASTVisitor {
		public boolean visit(LineComment node) {
			int start = node.getStartPosition();
			int line = cunit.getLineNumber(start);
			String comment = parser.getSourceFragment(start, node.getLength());
			comment = comment.substring(comment.indexOf('/')+2).trim();
			String[] tags = comment.split("\\s+");

			for (VariableTags t : variables)
				if(t.declarationLine == line)
					for(String tag : tags)
						if(validTags.contains(tag))
							t.addTag(tag);

			return true;
		}
	}

	private static class VariableTags implements Comparable<VariableTags>{
		private final String name;
		private final String type;
		private final int declarationLine;
		private final Scope scope;
		private final boolean isField;
		private final Set<String> tags;

		public VariableTags(String name, String type, int declarationLine, Scope scope, boolean isField) {
			this.name = name;
			this.type = type;
			this.declarationLine = declarationLine;
			this.scope = scope;
			this.isField = isField;
			this.tags = new HashSet<String>();
		}

		public boolean withinLine(int line) {
			return scope.contains(line);
		}

		public void addTag(String tag) {
			tags.add(tag);
		}			

		@Override
		public String toString() {
			return name + " on scope " + scope + " (" + type + ") : " + tags;
		}

		public Set<String> getTags() {
			return Collections.unmodifiableSet(tags);
		}

		public boolean isField() {
			return isField;
		}

		@Override
		public int compareTo(VariableTags o) {
			return scope.compareTo(o.scope);
		}
	}

	private static class Scope implements Comparable<Scope>{
		final int firstLine;
		final int lastLine;

		Scope(int firstLine, int lastLine) {
			this.firstLine = firstLine;
			this.lastLine = lastLine;
		}

		boolean contains(int line) {
			return line >= firstLine && line <= lastLine;
		}

		

		@Override
		public String toString() {
			return "[" + firstLine + ", " + lastLine + "]";
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + firstLine;
			result = prime * result + lastLine;
			return result;
		}


		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Scope other = (Scope) obj;
			if (firstLine != other.firstLine)
				return false;
			if (lastLine != other.lastLine)
				return false;
			return true;
		}

		boolean isInnerScopeOf(Scope scope) {
			return firstLine > scope.firstLine && (lastLine < scope.lastLine || scope.lastLine == -1);
		}

		@Override
		public int compareTo(Scope s) {
			return firstLine - s.firstLine;
			// TODO repor???
//			return this.equals(s) ? 0 : (isInnerScopeOf(s) ? -1 : 1);
		}
	}
}
