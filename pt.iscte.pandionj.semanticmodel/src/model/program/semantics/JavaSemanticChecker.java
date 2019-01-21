package model.program.semantics;

import java.util.List;

import model.program.IProgram;

public class JavaSemanticChecker implements ISemanticChecker {

	public JavaSemanticChecker(IProgram program) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getName() {
		return "Java semantic checker";
	}

	@Override
	public List<ISemanticProblem> getProblems() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
