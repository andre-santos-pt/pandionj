package model.machine.impl;

import model.machine.IValue;
import model.program.IDataType;

public class Value implements IValue {
	private final IDataType type;
	private final Object value;
	
	public Value(IDataType type, Object value) {
		this.type = type;
		this.value = value;
	}

	@Override
	public IDataType getDataType() {
		return type;
	}

	@Override
	public Object getValue() {
		return value;
	}

}
