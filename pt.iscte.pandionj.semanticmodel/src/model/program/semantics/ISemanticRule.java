package model.program.semantics;

import model.program.IProgramElement;

public interface ISemanticRule {

	boolean applicableTo(IProgramElement element);
	
	ISemanticProblem check(IProgramElement element);
}