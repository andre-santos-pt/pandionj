package pt.iscte.pandionj.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VariableInfo {
	private final String name;
	private final boolean isParam;
	private List<VariableOperation> operations;
	private BlockInfo declarationBlock;
	
	public VariableInfo(String name, boolean isParam, BlockInfo declarationBlock) {
		this.name = name;
		this.isParam = isParam;
		operations = new ArrayList<>(5);
		this.declarationBlock = declarationBlock;
	}

	@Override
	public String toString() {
		List<String> accessedArrays = getAccessedArrays();
		return name + (isParam ? "* " : " ") + 
				(isFixedValue() ? "CONSTANT " : "") +
				(isGatherer() ? "GATHERER " : "") + 
				(!accessedArrays.isEmpty() ? "ARRAY_INDEX " : "") + 
				operations;
	}

	public String getName() {
		return name;
	}
	
	public boolean isParam() {
		return isParam;
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
	
	public enum GathererType {
		SUM, PROD, UNDEFINED;
	}
	
	public GathererType getGathererType() {
		assert isGatherer();
		int sum = 0;
		int prod = 0;
		for(VariableOperation op : operations)
			if(op.getType() == VariableOperation.Type.ACC)
				switch((String) op.getParam(0)) {
				case "sum": sum++; break;
				case "prod": prod++; break;
				}
		
		if(sum > 0 && prod == 0)
			return GathererType.SUM;
		else if(prod > 0 && sum == 0)
			return GathererType.PROD;
		else
			return GathererType.UNDEFINED;
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

	public boolean isMostWantedHolder() {
		return false;
	}
	
//	public boolean isMostWantedHolder() {
//		boolean hasSubs = false;
//		for(VariableOperation op : operations)
//			if(op.getType() != VariableOperation.Type.SUBS) {
//				hasSubs = true;
//				BlockInfoVisitor v = new BlockInfoVisitor() {
//					insideIf = false;
//					public boolean visit(BlockInfo b) {
//						if(b != declarationBlock && b.getType() == BlockInfo.Type.IF) {
//							
//						}
//						return true;
//					}
//					
//				});
//				declarationBlock.accept(
//			}
//			else if(op.isModifier())
//				return false;
//		
//		return hasSubs;
//	}
}