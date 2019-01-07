package model.program.impl;

import model.program.IExpression;
import model.program.IReturn;

public class Return extends SourceElement implements IReturn {

	private final IExpression expression;
	
	public Return(IExpression expression) {
		this.expression = expression;
	}

	@Override
	public IExpression getExpression() {
		return expression;
	}
	
	@Override
	public String toString() {
		return "return " + expression;
	}
}
