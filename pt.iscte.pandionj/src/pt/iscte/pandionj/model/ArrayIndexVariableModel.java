package pt.iscte.pandionj.model;

import pt.iscte.pandionj.extensibility.IVariableModel;

public class ArrayIndexVariableModel {
	public enum Direction {
		NONE, FORWARD, BACKWARD;
	}
	
	private final IVariableModel model;
	private final int arrayLength;
	
	private int constBound;
	private IVariableModel varBound;
	
	private boolean illegalAccess;
	
	public ArrayIndexVariableModel(IVariableModel model, int arrayLength) {
		this.model = model;
		this.arrayLength = arrayLength;
		constBound = -1;
		varBound = null;
		illegalAccess = false;
	}
			
	public ArrayIndexVariableModel(IVariableModel model, int arrayLength, int constBound) {
		this(model, arrayLength);
		this.constBound = constBound;
	}

	public ArrayIndexVariableModel(IVariableModel model, int arrayLength, IVariableModel varBound) {
		this(model, arrayLength);
		this.varBound = varBound;
	}
	
	public String getName() {
		return model.getName();
	}
	
	public int getCurrentIndex() {
		return Integer.parseInt(model.getCurrentValue());
	}
	
	public boolean isOutOfBounds() {
		int i = getCurrentIndex();
		return i < 0 || i >= arrayLength;
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
}