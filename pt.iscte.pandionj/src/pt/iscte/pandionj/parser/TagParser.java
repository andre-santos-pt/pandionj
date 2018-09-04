package pt.iscte.pandionj.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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

import pt.iscte.pandionj.extensibility.PandionJConstants;



public class TagParser {

	private IFile file;
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
		this.file = file;

		try {
			file.deleteMarkers(PandionJConstants.MARKER_ID, true, IResource.DEPTH_ONE);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public Tag getTag(String varName, int line, boolean isField) {
		for (VariableTags t : variables)
			if(t.name.equals(varName) && (isField && t.isField || t.withinLine(line)) )
				return t.getTag();

		return null;
	}

	public Tag getAttributeTag(String typeName, String attName) {
		for (VariableTags t : variables)
			if(typeName.equals(t.type) && attName.equals(t.name))
				return t.getTag();

		return null;
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
		//		return variables.stream().filter((v) -> !v.tags.isEmpty()).collect(Collectors.toList()).toString();
		return variables.stream().filter((v) -> v.tag != null).toString();
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
			List fragments = node.fragments();
			for(Object o : fragments) {
				VariableDeclarationFragment frag = (VariableDeclarationFragment) o;
				String varName = frag.getName().getIdentifier();
				int line = cunit.getLineNumber(frag.getStartPosition());
				Scope scope = new Scope(line, getEndLine(node.getParent(), cunit));
				VariableTags tags = new VariableTags(varName, null, line, scope, false);
				variables.add(tags);
			}
			return super.visit(node);
		}

		@Override
		public boolean visit(FieldDeclaration node) {
			List fragments = node.fragments();
			for(Object o : fragments) {
				VariableDeclarationFragment frag = (VariableDeclarationFragment) o;
				String varName = frag.getName().getIdentifier();
				int line = cunit.getLineNumber(frag.getStartPosition());
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
			}
			return false;
		}
	}

	private static int getEndLine(ASTNode node, CompilationUnit cunit) {
		return cunit.getLineNumber(node.getStartPosition() + node.getLength());
	}

	private static final String ARG = "[a-z]+=[a-zA-Z0-9\\.]+";
	class CommentVisitor extends ASTVisitor {
		public boolean visit(LineComment node) {
			int start = node.getStartPosition();
			int line = cunit.getLineNumber(start);
			String comment = parser.getSourceFragment(start, node.getLength());
			comment = comment.substring(comment.indexOf('/')+2).trim();

			if(!comment.startsWith("@"))
				return true;
			
			String tag = null;
			Map<String,String> args = new HashMap<>();
			if(comment.matches("@[a-z]+")) {
				tag = comment.substring(1);
			}
			else if(comment.matches("@[a-z]+\\s*\\(" + ARG + "\\s*(,\\s*" + ARG +"\\s*)*\\)")) {
				int i = comment.indexOf('(');
				tag = comment.substring(1, i).trim();
				String[] split = comment.substring(i+1, comment.length()-1).split("\\s*,\\s*");
				for(String a : split) {
					int j = a.indexOf('=');
					args.put(a.substring(0, j), a.substring(j+1));
				}
			}
			else {
				addMarker(node, start, line, comment, "Invalid tag syntax. Example: @tag(param1=a, param2=b)");
				return true;
			}

			if(!validTags.contains(tag)) {
				addMarker(node, start, line, comment, "Unknown tag. Installed tags: @" + String.join(", @", validTags));
			}
			else {
				for (VariableTags t : variables) {
					if(t.declarationLine == line)
						t.addTag(new Tag(tag, args));

				}	
			}
			return true;
		}

		private void addMarker(LineComment node, int start, int line, String comment, String message) {
			try {
				IMarker m = file.createMarker(PandionJConstants.MARKER_ID);
				m.setAttribute(IMarker.LINE_NUMBER, line);
				m.setAttribute(IMarker.MESSAGE, message);
				m.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
				m.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
				int dif = node.getLength() - comment.length();
				m.setAttribute(IMarker.CHAR_START, start + dif);
				m.setAttribute(IMarker.CHAR_END, start + dif + comment.length());
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	private static class VariableTags implements Comparable<VariableTags>{
		private final String name;
		private final String type;
		private final int declarationLine;
		private final Scope scope;
		private final boolean isField;
		private Tag tag;

		public VariableTags(String name, String type, int declarationLine, Scope scope, boolean isField) {
			this.name = name;
			this.type = type;
			this.declarationLine = declarationLine;
			this.scope = scope;
			this.isField = isField;
		}

		public boolean withinLine(int line) {
			return scope.contains(line);
		}

		public void addTag(Tag tag) {
			//			tags.add(tag);
			this.tag = tag;
		}			

		@Override
		public String toString() {
			return name + " on scope " + scope + " (" + type + ") : " + tag;
		}

		//		public Set<String> getTags() {
		//			return Collections.unmodifiableSet(tags);
		//		}

		public Tag getTag() {
			return tag;
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

	void print() {
		for(VariableTags v : variables) {
			if(v.tag != null)
				System.out.println(v.tag + " -> " + v.name);
		}
	}
}
