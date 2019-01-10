package model.machine.impl;

import model.machine.IArray;
import model.machine.IValue;
import model.program.IDataType;

class Array implements IArray {
	private ProgramState programState;
	private IDataType type;
	private IValue[] array;
	
	public Array(ProgramState programState, IDataType type, int length) {
		this.programState = programState;
		this.type = type;
		this.array = new IValue[length];
	}
	
	public Array(ProgramState programState, IDataType type, IValue[] elements) {
		this(programState, type, elements.length);
		for(int i = 0; i < elements.length; i++)
			this.array[i] = elements[i];
	}
	@Override
	public IDataType getType() {
		return type;
	}

	@Override
	public int getLength() {
		return array.length;
	}

	@Override
	public IValue getElement(int i) {
		return programState.getValue(array[i].getValue());
	}
	
	@Override
	public void setElement(int i, IValue value) {
		array[i] = value;
	}
	
	@Override
	public Object getValue() {
		return array;
	}
	
	@Override
	public String toString() {
		String text = "[";
		for(int i = 0; i < array.length; i++) {
			if(i != 0)
				text += ", ";
			text += array[i];
		}
		return text + "]";
	}
}
