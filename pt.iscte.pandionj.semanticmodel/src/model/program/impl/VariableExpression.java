package model.program.impl;

import model.program.IVariableDeclaration;
import model.program.IVariableExpression;

public class VariableExpression extends SourceElement implements IVariableExpression {

	private final IVariableDeclaration variable;
	
	public VariableExpression(IVariableDeclaration variable) {
		this.variable = variable;
	}

	@Override
	public IVariableDeclaration getVariable() {
		return variable;
	}

	@Override
	public boolean isAddress() {
		// TODO Auto-generated method stub
		return false;
	}

}
