package model.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

public interface IVariableAssignment extends IStatement {
	IVariableDeclaration getVariable();
	IExpression getExpression();
	
	
	@Override
	default List<ISemanticProblem> validateSematics() {
		if(!getVariable().getType().equals(getExpression().getType())) // exact match
			return ImmutableList.of(ISemanticProblem.create("incompatible types", this, getExpression()));
		return ImmutableList.of();
	}
	
	@Override
	default List<IExpression> getExpressionParts() {
		return ImmutableList.of(getExpression());
	}
}
