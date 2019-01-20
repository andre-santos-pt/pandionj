package impl.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

import impl.machine.ExecutionError;
import impl.machine.ExecutionError.Type;
import model.machine.IArray;
import model.machine.ICallStack;
import model.machine.IStackFrame;
import model.machine.IValue;
import model.program.IArrayAllocation;
import model.program.IDataType;
import model.program.IExpression;

public class ArrayAllocation extends Expression implements IArrayAllocation {
	private IDataType type;
	private ImmutableList<IExpression> dimensions;
	
	public ArrayAllocation(IDataType type, List<IExpression> dimensions) {
		this.type = type;
		this.dimensions = ImmutableList.copyOf(dimensions);
	}

	@Override
	public IDataType getType() {
		return type;
	}

	@Override
	public List<IExpression> getDimensions() {
		return dimensions;
	}

	@Override
	public String toString() {
		String text = "new " + type;
		for(IExpression e : dimensions)
			text += "[" + e + "]";
		return text;
	}
	
	@Override
	public List<IExpression> decompose() {
		return dimensions;
	}
	
	@Override
	public boolean isDecomposable() {
		return true;
	}	
	

	@Override
	public IValue evalutate(List<IValue> values, ICallStack stack) throws ExecutionError {
		assert values.size() == getDimensions().size();
		IStackFrame frame = stack.getTopFrame();

		int[] dims = new int[values.size()];
		for(int i = 0; i < dims.length; i++) {
			dims[i] = ((Number) values.get(i).getValue()).intValue();
			if(dims[i] < 0)
				throw new ExecutionError(Type.NEGATIVE_ARRAY_SIZE, this, Integer.toString(dims[i]));
		}

		IArray array = frame.allocateArray(getType(), dims); 
		return array;
	}
}
