package pt.iscte.pandionj.parser.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VariableInfo {
	enum Role {
		ARRAY_INDEX,
		GATHERER,
		MOST_WANTED_HOLDER;
	}
	
	private final String name;
	private List<VariableOperation> operations;
	
	public VariableInfo(String name) {
		this.name = name;
		operations = new ArrayList<>(5);
	}

	@Override
	public String toString() {
		List<String> accessedArrays = getAccessedArrays();
		return name + " " + 
				(isFixedValue() ? "CONSTANT " : "") +
				(isGatherer() ? "GATHERER " : "") + 
				(!accessedArrays.isEmpty() ? "ARRAY_INDEX " : "") + 
				operations;
	}

	public String getName() {
		return name;
	}
	
	public void addOperation(VariableOperation op) {
		operations.add(op);
	}
	
	public List<VariableOperation> getOperations() {
		return Collections.unmodifiableList(operations);
	}
	
	public boolean isFixedValue() {
		for(VariableOperation op : operations)
			if(op.isModifier())
				return false;
		return true;
	}
	
	public boolean isGatherer() {
		boolean hasAcc = false;
		boolean hasMod = false;
		for(VariableOperation op : operations)
			if(op.isModifier())
				if(op.getType() != VariableOperation.Type.ACC)
					hasMod = true;
				else
					hasAcc = true;
		
		return hasAcc && !hasMod;
	}
	
	public List<String> getAccessedArrays() {
		List<String> list = new ArrayList<>();
		for(VariableOperation op : operations)
			if(op.getType() == VariableOperation.Type.INDEX)
				list.add(op.getParam(0).toString());
		
		return list;
	}
	
	public String getBound() {
		for(VariableOperation op : operations)
			if(op.getType() == VariableOperation.Type.BOUNDED)
				return op.getParam(0).toString();
		
		return null;
	}
	
}