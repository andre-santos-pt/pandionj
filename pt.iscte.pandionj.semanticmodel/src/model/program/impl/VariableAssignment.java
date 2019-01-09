package model.program.impl;

import model.program.IExpression;
import model.program.IVariableAssignment;
import model.program.IVariableDeclaration;

class VariableAssignment extends Statement implements IVariableAssignment {

	private final IVariableDeclaration variable;
	private final IExpression expression;
	
	public VariableAssignment(Block parent, IVariableDeclaration variable, IExpression expression) {
		super(parent);
		this.variable = variable;
		this.expression = expression;
	}
	@Override
	public IVariableDeclaration getVariable() {
		return variable;
	}

	@Override
	public IExpression getExpression() {
		return expression;
	}
	
	@Override
	public String toString() {
		return variable.getIdentifier() + " = " + expression;
	}

}
