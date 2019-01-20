package model.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

public interface IVariableExpression extends IExpression {
	IVariableDeclaration getVariable();
	
	@Override
	default IDataType getType() {
		return getVariable().getType();
	}

	@Override
	default List<IExpression> decompose() {
		return ImmutableList.of();
	}
	
	@Override
	default boolean isDecomposable() {
		return false;
	}
}
