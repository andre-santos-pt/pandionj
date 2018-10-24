package pt.iscte.pandionj.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;


public class BlockInfo {
	public enum Type {
		TYPE, METHOD, WHILE, FOR, DO_WHILE, FOR_EACH, IF, OTHER;

		public boolean isLoop() {
			return this == WHILE || this == FOR || this == DO_WHILE || this == FOR_EACH;
		}
	}
	private final int lineStart;
	private final int lineEnd;
	private final Type type;
	private final BlockInfo parent;

	private String id;

	private List<BlockInfo> children = new ArrayList<>();
	private List<VariableInfo> vars = new ArrayList<>();
	private List<VariableOperation> operationRecord = new ArrayList<>();

	public static final BlockInfo NONE = new BlockInfo(null, 0, 0, Type.OTHER); 

	public BlockInfo(BlockInfo parent, int start, int end, Type type) {
		this.parent = parent;
		lineStart = start;
		lineEnd = end;
		if(parent != null)
			parent.addChild(this);

		this.type = type;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id == null ? type.name() : id + (type == Type.METHOD ? "()" : "");
	}
	
	public int getNumberOfParams() {
		int c = 0;
		for(VariableInfo v : vars)
			if(v.isParam())
				c++;
			else
				break;
		return c;
	}
	
	public VariableInfo getParam(int index) {
		assert index >= 0 && index < getNumberOfParams();
		return vars.get(index);
	}
	
	public BlockInfo getParent() {
		return parent;
	}

	public boolean isLoop() {
		return type.isLoop();
	}


	private void addChild(BlockInfo b) {
		children.add(b);
	}
	
	public BlockInfo getUniqueMethod(String id, int nParams) {
		class MethodFinder implements BlockInfoVisitor {
			BlockInfo method = null;
			boolean unique = true;
			
			public boolean visit(BlockInfo b) {
				if(id.equals(b.id) && b.getNumberOfParams() == nParams) {
					if(method != null) 
						unique = false;
					else
						method = b;
				}
				return false;
			}
		}
		MethodFinder methodFinder = new MethodFinder();
		accept(methodFinder);
		return methodFinder.unique ? methodFinder.method : null;
	}

	public BlockInfo getRoot() {
		BlockInfo b = this;
		while(b.parent != null)
			b = b.parent;
		
		return b;
	}

	public interface BlockInfoVisitor {
		default boolean visit(BlockInfo b) { return true; }
		default void endVisit(BlockInfo b) { }
		default void visit(VariableInfo var) { }
//		default void endVisit(VariableInfo var) { };
	}

	public void accept(BlockInfoVisitor v) {
		if(v.visit(this)) {
			for(VariableInfo var : vars)
				v.visit(var);
			for(BlockInfo c : children)
				c.accept(v);
		}
		v.endVisit(this);
	}

//	public void acceptUpwards(BlockInfoVisitor v) {
//		if(v.visit(this)) {
//			for(VariableInfo var : vars.values())
//				v.visit(var);
//			if(parent != null)
//				parent.accept(v);
//		}
//	}
	
	public void print() {
		accept(new BlockInfoVisitor() {
			int depth = 0;
			@Override
			public boolean visit(BlockInfo b) {
				tabs(depth);
				System.out.println(b.lineStart+"-"+b.lineEnd+ " " + b.getId() + " {");
				depth++;
				return true;
			}

			@Override
			public void endVisit(BlockInfo b) {
				depth--;
				tabs(depth);
				System.out.println("}\n");
			}

			@Override
			public void visit(VariableInfo var) {
				tabs(depth);
				System.out.println(var.toString());
			}
			
			private void tabs(int n) {
				while(n-- > 0)
					System.out.print("\t");
			}
		});
	}
	public String toText() {
		String s = lineStart+"-"+lineEnd+ " " + getId() + " {\n";
		for(VariableInfo v : vars)
			s	 += "\t" + v.toString() + "\n";

		for(BlockInfo b : children) {
			Scanner scanner = new Scanner(b.toText());
			while(scanner.hasNextLine())
				s += "\t" + scanner.nextLine() + "\n";
			scanner.close();
		}
		s += "}\n";
		return s;
	}

	@Override
	public String toString() {
		return lineStart+"-"+lineEnd+ " " + getId();
	}

	public VariableInfo addVar(String var, boolean isField) {
		return addVar(var, -1, isField);
	}
	
	public VariableInfo addVar(String var, int paramIndex, boolean isField) {
		VariableInfo varInfo = new VariableInfo(var, this, paramIndex, isField);
		vars.add(varInfo);
		return varInfo;
	}

	private boolean inScope(int line) {
		return line >= lineStart && line <= lineEnd;
	}

	private VariableInfo findVarByName(String name) {
		for(VariableInfo v : vars)
			if(v.getName().equals(name))
				return v;
		
		return null;
	}
	
	public VariableInfo getVariable(String name) {
		BlockInfo b = this;
		while(b != null) {
			VariableInfo v = b.findVarByName(name);
			if(v != null)
				return v;
			b = b.parent;
		}
		return null;
	}


	public void addOperation(VariableOperation op) {
		VariableInfo var = getVariable(op.getVarName());
		if(var == null)
			System.err.println(op.getVarName()  + "  not found");
		else {
			var.addOperation(op);
			operationRecord.add(op);
		}
	}

	public VariableInfo locateVariable(String name, int line, boolean isField) {
		if(line == -1 && isField)
			return getVariable(name);
		else
			return locateVariable(this, name, line, isField);
	}


	private static VariableInfo locateVariable(BlockInfo block, String name, int line, boolean isField) {
		if(block.inScope(line)) {
			BlockInfo childInScope = null;
			for(BlockInfo child : block.children) {
				if(child.inScope(line)) {
					childInScope = child;
					break;
				}
			}
			if(childInScope != null)
				return locateVariable(childInScope, name, line, isField);
			else
				return block.getVariable(name);
		}
		return null;
	}

	public Set<String> getOperations(VariableOperation.Type ... types) {
		Set<String> set = new HashSet<>();
		for(VariableOperation op : operationRecord)
			for(VariableOperation.Type t : types)
				if(op.getType().equals(t))
					set.add(op.getVarName());
		return set;
	}

	public Type getType() {
		return type;
	}

	public boolean contains(VariableOperation op) {
		return operationRecord.contains(op);
	}
}