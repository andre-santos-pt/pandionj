package impl.program;

import java.util.List;

import model.machine.ICallStack;
import model.machine.IValue;
import model.program.IDataType;
import model.program.ILiteral;

class Literal extends Expression implements ILiteral {

	private final IDataType type;
	private final String value;
	
	public Literal(IDataType type, String value) {
		assert type != null;
		assert value != null && !value.isEmpty();
		this.type = type;
		this.value = value;
	}
	
	@Override
	public IDataType getType() {
		return type;
	}
	
	@Override
	public String getStringValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return value;
	}
	
	@Override
	public IValue evalutate(List<IValue> values, ICallStack stack) {
		return stack.getProgramState().getValue(getStringValue());
	}
}
