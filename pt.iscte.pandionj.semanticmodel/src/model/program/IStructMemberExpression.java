package model.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

public interface IStructMemberExpression extends IExpression {

	IVariableDeclaration getVariable();
	String getMemberId();

	@Override
	default List<IExpression> decompose() {
		return ImmutableList.of();
	}

	@Override
	default boolean isDecomposable() {
		return false;
	}
}
