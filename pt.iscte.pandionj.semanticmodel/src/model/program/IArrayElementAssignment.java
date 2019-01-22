package model.program;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import model.program.semantics.ISemanticProblem;

public interface IArrayElementAssignment extends IVariableAssignment {
	List<IExpression> getIndexes(); // not null, length == getDimensions
	default int getDimensions() {
		return getIndexes().size();
	}
	
	IArrayVariableDeclaration getVariable();
	
	@Override
	default List<IExpression> getExpressionParts() {
		List<IExpression> list = new ArrayList<IExpression>(getIndexes());
		list.add(getExpression());
		return list;
	}
}
