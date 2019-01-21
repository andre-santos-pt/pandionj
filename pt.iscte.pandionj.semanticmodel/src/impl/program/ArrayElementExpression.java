package impl.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

import impl.machine.ExecutionError;
import model.machine.IArray;
import model.machine.ICallStack;
import model.machine.IStackFrame;
import model.machine.IValue;
import model.program.IArrayElementExpression;
import model.program.IArrayVariableDeclaration;
import model.program.IDataType;
import model.program.IExpression;

public class ArrayElementExpression extends VariableExpression implements IArrayElementExpression {
	private ImmutableList<IExpression> indexes;
	
	public ArrayElementExpression(IArrayVariableDeclaration variable, List<IExpression> indexes) {
		super(variable);
		this.indexes = ImmutableList.copyOf(indexes);
	}
	
	@Override
	public IArrayVariableDeclaration getVariable() {
		return (IArrayVariableDeclaration) super.getVariable();
	}

	@Override
	public List<IExpression> getIndexes() {
		return indexes;
	}
	
	@Override
	public IDataType getType() {
		return getVariable().getComponentType();
	}
	
	@Override
	public String toString() {
		String text = getVariable().getId();
		for(IExpression e : indexes)
			text += "[" + e + "]";
		return text;
	}
	
	@Override
	public List<IExpression> decompose() {
		return indexes;
	}
	
	@Override
	public IValue evalutate(List<IValue> values, ICallStack stack) throws ExecutionError {
		assert values.size() == getIndexes().size();
		IStackFrame frame = stack.getTopFrame();
		IValue variable = frame.getVariableValue(getVariable().getId());
		IValue element = variable;
		for(IValue v : values) {
			int index = ((Number) v.getValue()).intValue();
			if(index < 0)
				throw new ExecutionError(ExecutionError.Type.NEGATIVE_ARRAY_SIZE, this, "negative array index", index);
			element = ((IArray) element).getElement(index);
		}
		return element;
	}
	
}
