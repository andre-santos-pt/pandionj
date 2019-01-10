package model.program.impl;

import java.util.List;

import com.google.common.collect.ImmutableList;

import model.program.IArrayElementExpression;
import model.program.IArrayVariableDeclaration;
import model.program.IExpression;

public class ArrayElementExpression extends VariableExpression implements IArrayElementExpression {
	private ImmutableList<IExpression> indexes;
	
	public ArrayElementExpression(IArrayVariableDeclaration variable, List<IExpression> indexes) {
		super(variable);
		this.indexes = ImmutableList.copyOf(indexes);
	}
	
	
	@Override
	public IArrayVariableDeclaration getVariable() {
		return (IArrayVariableDeclaration) super.getVariable();
	}

	@Override
	public List<IExpression> getIndexes() {
		return indexes;
	}
	
	@Override
	public String toString() {
		String text = getVariable().getIdentifier();
		for(IExpression e : indexes)
			text += "[" + e + "]";
		return text;
	}
}
