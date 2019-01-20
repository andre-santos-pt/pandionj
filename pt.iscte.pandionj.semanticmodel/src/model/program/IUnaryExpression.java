package model.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

public interface IUnaryExpression extends IExpression {
	IUnaryOperator getOperator();
	IExpression getExpression();
	
	@Override
	default List<IExpression> decompose() {
		return ImmutableList.of(getExpression());
	}
}
