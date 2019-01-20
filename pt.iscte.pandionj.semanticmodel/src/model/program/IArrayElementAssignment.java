package model.program;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

public interface IArrayElementAssignment extends IVariableAssignment {
	List<IExpression> getIndexes(); // not null, length == getDimensions
	default int getDimensions() {
		return getIndexes().size();
	}
	
	IArrayVariableDeclaration getVariable();
	
	@Override
	default List<ISemanticProblem> validateSematics() {
		if(!getVariable().getComponentType().equals(getExpression().getType()))
			return ImmutableList.of(ISemanticProblem.create("incompatible types", this, getExpression()));
		return ImmutableList.of();
	}
	
	@Override
	default List<IExpression> getExpressionParts() {
		List<IExpression> list = new ArrayList<IExpression>(getIndexes());
		list.add(getExpression());
		return list;
	}
}
