package impl.program;

import java.util.List;

import model.machine.ICallStack;
import model.machine.IStructObject;
import model.machine.IValue;
import model.program.IDataType;
import model.program.IStructMemberExpression;
import model.program.IVariableDeclaration;

class StructMemberExpression extends Expression implements IStructMemberExpression {

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
	
	@Override
	public IValue evalutate(List<IValue> values, ICallStack stack) {
		// TODO validate
		IStructObject object = (IStructObject) stack.getTopFrame().getVariable(getVariable().getId());
		IValue field = object.getField(getMemberId());
		return field;
	}
}
