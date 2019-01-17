package impl.program;

import model.program.IBlock;
import model.program.IExpression;
import model.program.IVariableAssignment;
import model.program.IVariableDeclaration;

class VariableAssignment extends Statement implements IVariableAssignment {

	private final IVariableDeclaration variable;
	private final IExpression expression;
	
	public VariableAssignment(IBlock parent, IVariableDeclaration variable, IExpression expression) {
		super(parent, true);
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
		return variable.getId() + " = " + expression;
	}

}
