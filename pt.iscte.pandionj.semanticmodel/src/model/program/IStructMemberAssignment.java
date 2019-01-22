package model.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

public interface IStructMemberAssignment extends IVariableAssignment {

	String getMemberId();

	@Override
	default List<IExpression> getExpressionParts() {
		return ImmutableList.of(getExpression());
	}
}
