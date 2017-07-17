package pt.iscte.pandionj.parser.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


public class BlockInfo {
	public enum Type {
		TYPE, METHOD, WHILE, FOR, DO_WHILE, FOR_EACH, IF, OTHER;
	}
	private final int lineStart;
	private final int lineEnd;
	private final Type type;
	
	private final BlockInfo parent;

	private List<BlockInfo> children = new ArrayList<>(5);
	private Map<String, VariableInfo> vars = new LinkedHashMap<>(5);
	private List<VariableOperation> operationRecord = new ArrayList<>();

	
	public BlockInfo(BlockInfo parent, int start, int end, Type type) {
		this.parent = parent;
		lineStart = start;
		lineEnd = end;
		if(parent != null)
			parent.addChild(this);
		
		this.type = type;
	}

	public BlockInfo getParent() {
		return parent;
	}

	private void addChild(BlockInfo b) {
		children.add(b);
	}

	public String toText() {
		String s = lineStart+"-"+lineEnd+ " " + type + " {\n";
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

	public void addVar(String var) {
		vars.put(var, new VariableInfo(var));
	}

	private boolean inScope(int line) {
		return line >= lineStart && line <= lineEnd;
	}
	
	
	private VariableInfo getVariable(String name) {
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

	public Set<String> getVarsModified(VariableOperation.Type t) {
		Set<String> set = new HashSet<>();
		for(VariableOperation op : operationRecord)
			if(op.getType().equals(t))
				set.add(op.getVarName());
		return set;
	}

//	public Collection<VariableInfo> getVars() {
//		return vars.values();
//	}
}