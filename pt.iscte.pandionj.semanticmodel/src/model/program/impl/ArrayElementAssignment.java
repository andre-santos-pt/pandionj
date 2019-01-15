package model.program.impl;

import java.util.List;

import com.google.common.collect.ImmutableList;

import model.program.IArrayElementAssignment;
import model.program.IArrayVariableDeclaration;
import model.program.IBlock;
import model.program.IExpression;

public class ArrayElementAssignment extends VariableAssignment implements IArrayElementAssignment {

	private ImmutableList<IExpression> indexes;

	public ArrayElementAssignment(IBlock parent, IArrayVariableDeclaration variable, List<IExpression> indexes, IExpression expression) {
		super(parent, variable, expression);
		this.indexes = ImmutableList.copyOf(indexes);
	}

	@Override
	public List<IExpression> getIndexes() {
		return indexes;
	}
	
	@Override
	public IArrayVariableDeclaration getVariable() {
		return (IArrayVariableDeclaration) super.getVariable();
	}

	@Override
	public String toString() {
		String text = getVariable().getId();
		for(IExpression e : indexes)
			text += "[" + e + "]";
		
		text += " = " + getExpression();
		return text;
	}
}
