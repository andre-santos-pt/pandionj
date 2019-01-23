package model.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

public interface IVariableAssignment extends IStatement {
	IVariableDeclaration getVariable();
	IExpression getExpression();
	
	@Override
	default List<IExpression> getExpressionParts() {
		return ImmutableList.of(getExpression());
	}
}
