package model.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

public interface IContinue extends IStatement {

	@Override
	default List<IExpression> getExpressionParts() {
		return ImmutableList.of();
	}
}