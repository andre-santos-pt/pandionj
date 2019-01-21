package model.program.semantics.java;

import model.program.IIdentifiableElement;
import model.program.IProgramElement;
import model.program.semantics.ISemanticProblem;
import model.program.semantics.ISemanticRule;

public class JavaIdentifiers implements ISemanticRule {

	@Override
	public boolean applicableTo(IProgramElement element) {
		return element instanceof IIdentifiableElement;
	}

	@Override
	public ISemanticProblem check(IProgramElement element) {
		// TODO Auto-generated method stub
		return null;
	}

}
