package model.machine.impl;

import model.machine.IReference;
import model.machine.IValue;
import model.program.IDataType;

public class Reference implements IReference {
	private IValue target;
	
	public Reference(IValue target) {
		this.target = target;
	}
	
	@Override
	public IDataType getType() {
		return target == IValue.NULL ? null : target.getType();
	}

	@Override
	public Object getValue() {
		return target == IValue.NULL ? null : target.getValue();
	}

	@Override
	public IValue getTarget() {
		return target;
	}

}
