package pt.iscte.pandionj.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pt.iscte.pandionj.PandionJView;
import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.extensibility.IArrayIndexModel.BoundType;
import pt.iscte.pandionj.parser.BlockInfo.BlockInfoVisitor;

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
				(isMostWantedHolder() ? "HOLDER " : "") +
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
	
	public boolean isArrayIndex() {
		boolean arrayAccess = false;
		for(VariableOperation op : operations)
			if(op.getType() == VariableOperation.Type.INDEX)
				arrayAccess = true;
			else if(op.isModifier() && !op.isStepper())
				return false;
		
		return arrayAccess;
	}
	
	public List<String> getAccessedArrays() {
		List<String> list = new ArrayList<>();
		for(VariableOperation op : operations)
			if(op.getType() == VariableOperation.Type.INDEX)
				list.add(op.getParam(0).toString());
		
		return list;
	}
	
	private boolean isStepper(VariableOperation.Type t) {
		boolean found = false;
		for(VariableOperation op : operations)
			if(op.getType() == t)
				found = true;
			else if(op.isModifier())
				return false;
		
		return found;
	}
	
	public boolean isStepperForward() {
		return isStepper(VariableOperation.Type.INC);
	}
	
	public boolean isStepperBackward() {
		return isStepper(VariableOperation.Type.DEC);
	}
	
	public boolean isBounded() {
		boolean hasBound = false;
		for(VariableOperation op : operations)
			if(op.getType() == VariableOperation.Type.BOUNDED) {
				if(hasBound)
					return false;
				else
					hasBound = true;
			}
		
		return hasBound;
						
	}
	public IArrayIndexModel.IBound getBound() {
		for(VariableOperation op : operations)
			if(op.getType() == VariableOperation.Type.BOUNDED) {
				return new Bound(op.getParam(0).toString(), IArrayIndexModel.BoundType.valueOf(op.getParam(1).toString()));
			}
		
		return null;
	}

	private class Bound implements IArrayIndexModel.IBound {
		String expression;
		IArrayIndexModel.BoundType type;
		
		Bound(String expression, IArrayIndexModel.BoundType type) {
			assert expression != null;
			assert type != null;
			this.expression = expression;
			this.type = type;
		}
		
		@Override
		public String getExpression() {
			return expression;
		}

		@Override
		public int getValue() {
			// TODO eval
			PandionJView.getInstance().evaluate(expression, null);
			return -1;
		}

		@Override
		public BoundType getType() {
			return type;
		}
	}
	

	public boolean isMostWantedHolder() {
		boolean hasSubs = false;
		for(VariableOperation op : operations) {
			if(op.getType() == VariableOperation.Type.SUBS) {
				hasSubs = true;
				IfVisitor v = new IfVisitor(op);
				declarationBlock.accept(v);
				if(!v.insideIf)
					return false;
			}
			else if(op.isModifier())
				return false;
		}
		
		return hasSubs;
	}
	
	private class IfVisitor implements BlockInfoVisitor {
		boolean insideIf = false;
		VariableOperation op;
		
		public IfVisitor(VariableOperation op) {
			this.op = op;
		}

		public boolean visit(BlockInfo b) {
			if(b != declarationBlock && b.getType() == BlockInfo.Type.IF && b.contains(op)) {
				BlockInfo n = b;
				while(n != declarationBlock && !insideIf) {
					n = n.getParent();
					if(n.isLoop())
						insideIf = true;
				}
			}
			return true;
		}
	}
	
}