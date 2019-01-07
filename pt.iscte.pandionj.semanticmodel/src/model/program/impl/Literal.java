package model.program.impl;

import model.program.ILiteral;

public class Literal extends SourceElement implements ILiteral {

	private final String value;
	
	public Literal(String value) {
		assert value != null && !value.isEmpty();
		this.value = value;
	}
	
	@Override
	public String getStringValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return value;
	}
}
