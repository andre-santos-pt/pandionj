package pt.iscte.pandionj.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

	private List<BlockInfo> children = new ArrayList<>(5);
	private Map<String, VariableInfo> vars = new LinkedHashMap<>(5);
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
		for(VariableInfo v : vars.values())
			if(v.isParam())
				c++;
		return c;
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
	
	public BlockInfo getMethod(String id, int nParams) {
		class MethodFinder implements BlockInfoVisitor {
			BlockInfo method;
			public boolean visit(BlockInfo b) {
				if(id.equals(b.id) && b.getNumberOfParams() == nParams) {
					if(method != null) // TODO not unique name/nparams
						;
					else
						method = b;
				}
				return false;
			}
		}
		MethodFinder methodFinder = new MethodFinder();
		accept(methodFinder);
		return methodFinder.method;
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
			for(VariableInfo var : vars.values())
				v.visit(var);
			for(BlockInfo c : children) {
				c.accept(v);
				
			}
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
				System.out.println(lineStart+"-"+lineEnd+ " " + b.getId() + " {");
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
		for(VariableInfo v : vars.values())
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

	public VariableInfo addVar(String var, boolean param) {
		VariableInfo varInfo = new VariableInfo(var, param, this);
		vars.put(var, varInfo);
		return varInfo;
	}

	private boolean inScope(int line) {
		return line >= lineStart && line <= lineEnd;
	}


	public VariableInfo getVariable(String name) {
		BlockInfo b = this;
		while(b != null) {
			VariableInfo v = b.vars.get(name);
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

	public VariableInfo locateVariable(String name, int line) {
		if(line == -1)
			return getVariable(name);
		else
			return locateVariable(this, name, line);
	}


	private static VariableInfo locateVariable(BlockInfo block, String name, int line) {
		if(block.inScope(line)) {
			BlockInfo childInScope = null;
			for(BlockInfo child : block.children) {
				if(child.inScope(line)) {
					childInScope = child;
					break;
				}
			}
			if(childInScope != null)
				return locateVariable(childInScope, name, line);
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



	//	public Collection<VariableInfo> getVars() {
	//		return vars.values();
	//	}
}