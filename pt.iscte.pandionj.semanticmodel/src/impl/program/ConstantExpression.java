package impl.program;

import java.util.List;

import model.machine.ICallStack;
import model.machine.IValue;
import model.program.IConstantDeclaration;
import model.program.IConstantExpression;
import model.program.IDataType;

class ConstantExpression extends Expression implements IConstantExpression {

	private final IConstantDeclaration constant;

	public ConstantExpression(IConstantDeclaration constant) {
		this.constant = constant;
	}
	
	@Override
	public IDataType getType() {
		return constant.getType();
	}

	@Override
	public IConstantDeclaration getConstant() {
		return constant;
	}

	@Override
	public String toString() {
		return constant.getId();
	}
	
	@Override
	public IValue evalutate(List<IValue> values, ICallStack stack) {
		return stack.getProgramState().getValue(getConstant().getValue().getStringValue());
	}
}
