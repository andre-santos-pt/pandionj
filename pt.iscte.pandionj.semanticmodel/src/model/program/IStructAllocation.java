package model.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

public interface IStructAllocation extends IExpression {
	
	@Override
	IStructType getType();
	
	@Override
	default List<IExpression> decompose() {
		return ImmutableList.of();
	}
	
	@Override
	default boolean isDecomposable() {
		return false;
	}
}
