package model.program.impl;

import java.util.List;

import com.google.common.collect.ImmutableList;

import model.program.IProblem;
import model.program.ISourceElement;

class Problem implements IProblem {
	private final String message;
	private final ImmutableList<ISourceElement> elements;
	
	public Problem(String message, ISourceElement ... elements) {
		this.message = message;
		this.elements = ImmutableList.copyOf(elements);
	}
	
	@Override
	public List<ISourceElement> getSourceElements() {
		return elements;
	}
	
	@Override
	public String getMessage() {
		return message;
	}
}
