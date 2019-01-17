package impl.program;

import model.program.IExpression;
import model.program.IReturn;

class Return extends Statement implements IReturn {

	private final IExpression expression;
	
	public Return(Block parent, IExpression expression) {
		super(parent, true);
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
