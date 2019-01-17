package impl.program;

import model.program.IBlock;
import model.program.IExpression;
import model.program.IStructMemberAssignment;
import model.program.IStructType;
import model.program.IVariableDeclaration;

public class StructMemberAssignment extends Statement implements IStructMemberAssignment {
	
	private final IVariableDeclaration variable;
	private final String memberId;
	private final IExpression expression;
	
	public StructMemberAssignment(IBlock parent, IVariableDeclaration variable, String memberId, IExpression expression) {
		super(parent, true);
		// TODO assert type
		this.variable = variable;
		this.memberId = memberId;
		this.expression = expression;
	}

	@Override
	public IVariableDeclaration getVariable() {
		return variable;
	}
	
	@Override
	public String getMemberId() {
		return memberId;
	}
	
	@Override
	public IExpression getExpression() {
		return expression;
	}

	@Override
	public String toString() {
		return variable.getId() + "." + memberId + " = " + expression;
	}
}
