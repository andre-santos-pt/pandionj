package model.program.semantics;

import model.program.IProgramElement;

public interface ISemanticRule<T> {

//	boolean applicableTo(IProgramElement element);
	
	ISemanticProblem check(T element);
}
