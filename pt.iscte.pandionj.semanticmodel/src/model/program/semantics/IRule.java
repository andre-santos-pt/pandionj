package model.program.semantics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.program.IModule;
import model.program.IProgramElement;

public abstract class IRule implements IModule.IVisitor {

//	boolean applicableTo(IProgramElement element);
	private final List<ISemanticProblem> problems = new ArrayList<>();
	
	protected void addProblem(ISemanticProblem p) {
		problems.add(p);
	}
	
	public List<ISemanticProblem> getProblems() {
		return Collections.unmodifiableList(problems);
	}
}
