package model.program.impl;

import java.util.List;

import com.google.common.collect.ImmutableList;

import model.program.ISemanticProblem;
import model.program.ISourceElement;

// TODO remove?
class SemanticProblem implements ISemanticProblem {
	private final String message;
	private final ImmutableList<ISourceElement> elements;
	
	public SemanticProblem(String message, ISourceElement ... elements) {
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
