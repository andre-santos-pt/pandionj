package model.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

public interface IStructMemberAssignment extends IStatement {

	IVariableDeclaration getVariable();
	String getMemberId();
	IExpression getExpression();

	@Override
	default List<IExpression> getExpressionParts() {
		return ImmutableList.of(getExpression());
	}
}
