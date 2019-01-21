package model.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

import model.program.semantics.ISemanticProblem;

public interface IProcedureCall extends IStatement {
	IProcedure getProcedure();
	List<IExpression> getArguments();

	default boolean isOperation() {
		return false;
	}
	
	default List<ISemanticProblem> validateSematics() {
		if(getProcedure().getNumberOfParameters() != getArguments().size())
			return ImmutableList.of(ISemanticProblem.create("wrong number of arguments", this));
		
		// TODO param match
		
		return ImmutableList.of();
	}
}
