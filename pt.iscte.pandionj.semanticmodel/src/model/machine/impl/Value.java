package model.machine.impl;

import model.machine.IValue;
import model.program.IDataType;

public class Value implements IValue {
	private final IDataType type;
	private final Object value;
	
	public Value(IDataType type, Object value) {
		assert value != null;
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
	
	@Override
	public String toString() {
		return value.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		return value.equals(obj);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}
}
