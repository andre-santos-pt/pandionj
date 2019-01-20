package model.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

public interface ILiteral extends IExpression {
	String getStringValue();
	
	@Override
	default List<IExpression> decompose() {
		return ImmutableList.of();
	}
	
	@Override
	default boolean isDecomposable() {
		return false;
	}
}
