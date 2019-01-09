package model.program.impl;

import model.program.IProblem;
import model.program.ISourceElement;

class Problem implements IProblem {
	private final ISourceElement element;
	private final String message;
	
	public Problem(ISourceElement element, String message) {
		this.element = element;
		this.message = message;
	}
	
	@Override
	public ISourceElement getSourceElement() {
		return element;
	}
	@Override
	public String getMessage() {
		return message;
	}
}
