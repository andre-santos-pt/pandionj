package model.program.semantics;

import java.util.List;

public interface ISemanticChecker {

	String getName();
	List<ISemanticProblem> getProblems();
}
