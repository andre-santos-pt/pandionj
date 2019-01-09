package model.program.impl;

import model.program.IIdentifiableElement;

abstract class IdentifiableElement implements IIdentifiableElement {
	private final String identifier;
	
	public IdentifiableElement(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

}
