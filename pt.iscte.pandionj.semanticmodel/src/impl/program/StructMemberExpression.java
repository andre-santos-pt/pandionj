package impl.program;

import model.program.IDataType;
import model.program.IStructMemberExpression;
import model.program.IStructType;
import model.program.IVariableDeclaration;

class StructMemberExpression extends SourceElement implements IStructMemberExpression {

	private final IVariableDeclaration variable;
	private final String memberId;
	
	public StructMemberExpression(IVariableDeclaration variable, String memberId) {
		assert variable != null;
		assert memberId != null;
		
		// TODO validation variable
		this.variable = variable;
		this.memberId = memberId;
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
	public IDataType getType() {
		return variable.getType();
	}
	
	@Override
	public String toString() {
		return variable.getId() + "." + memberId;
	}
}
