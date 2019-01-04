package model.program.impl;

import model.program.ILiteral;

public class Literal extends SourceElement implements ILiteral {

	private final String value;
	
	public Literal(String value) {
		this.value = value;
	}
	
	@Override
	public String getStringValue() {
		return value;
	}
}
