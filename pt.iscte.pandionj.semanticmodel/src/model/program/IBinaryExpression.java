package model.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

import model.program.IOperator.OperationType;

public interface IBinaryExpression extends IExpression {
	IBinaryOperator getOperator();
	IExpression getLeftExpression();
	IExpression getRightExpression();
	
	@Override
	default OperationType getOperationType() {
		return getOperator().getOperationType();
	}
	
	@Override
	default List<IExpression> decompose() {
		return ImmutableList.of(getLeftExpression(), getRightExpression());
	}
}

