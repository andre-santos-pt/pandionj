package impl.program;

import java.util.List;

import model.machine.ICallStack;
import model.machine.IValue;
import model.program.IStructAllocation;
import model.program.IStructType;

class StructAllocation extends Expression implements IStructAllocation {

	private final IStructType type;
	
	public StructAllocation(IStructType type) {
		this.type = type;
	}


	@Override
	public IStructType getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return "new " + getType().getId(); 
	}

	@Override
	public IValue evalutate(List<IValue> values, ICallStack stack) {
		return stack.getTopFrame().allocateObject(getType());
	}
}
