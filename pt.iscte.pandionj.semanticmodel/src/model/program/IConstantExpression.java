package model.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

public interface IConstantExpression extends IExpression {

	IConstantDeclaration getConstant();
	
	@Override
	default List<IExpression> decompose() {
		return ImmutableList.of();
	}
	
	@Override
	default boolean isDecomposable() {
		return false;
	}
}
