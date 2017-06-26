package pt.iscte.pandionj.model;

import java.util.Collection;
import java.util.List;

import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.parser.variable.Variable;

public class ArrayIndexVariableModel implements IArrayIndexModel {
	private final IVariableModel model;
	
	private int constBound;
	private IVariableModel varBound;
	
	private boolean illegalAccess;
	
	public ArrayIndexVariableModel(IVariableModel model) {
		assert model != null;
		this.model = model;
		constBound = -1;
		varBound = null;
		illegalAccess = false;
	}
			
	public ArrayIndexVariableModel(IVariableModel model, int constBound) {
		this(model);
		this.constBound = constBound;
	}

	public ArrayIndexVariableModel(IVariableModel model, IVariableModel varBound) {
		this(model);
		this.varBound = varBound;
	}
	
	public String getName() {
		return model.getName();
	}
	
	public int getCurrentIndex() {
		return Integer.parseInt(model.getCurrentValue());
	}
	
	public boolean isOutOfBounds(IArrayModel array) {
		int i = getCurrentIndex();
		return i < 0 || i >= array.getLength();
	}

	public boolean isBounded() {
		return constBound != -1 || varBound != null;
	}

	public int getBound() {
		if(!isBounded())
			return -1;
		else
			return constBound != -1 ? constBound : Integer.parseInt(varBound.getCurrentValue());
	}

	public Direction getDirection() {
		if(isBounded())
			return getCurrentIndex() < getBound() ? Direction.FORWARD : Direction.BACKWARD;
		else	
			return Direction.NONE;
	}
	
	void setIllegalAccess() {
		illegalAccess = true;
	}

	public boolean isIllegalAccess() {
		return illegalAccess;
	}

	@Override
	public String getTypeName() {
		return model.getTypeName();
	}

	@Override
	public String getCurrentValue() {
		return model.getCurrentValue();
	}

	@Override
	public List<String> getHistory() {
		return model.getHistory();
	}

	@Override
	public boolean isDecimal() {
		return model.isDecimal();
	}

	@Override
	public boolean isBoolean() {
		return model.isBoolean();
	}

	@Override
	public boolean isInstance() {
		return model.isInstance();
	}

	@Override
	public boolean isWithinScope() {
		return model.isWithinScope();
	}

	@Override
	public Variable getVariableRole() {
		return model.getVariableRole();
	}

	@Override
	public Collection<String> getTags() {
		return model.getTags();
	}
}