package impl.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

import impl.machine.ExecutionError;
import model.machine.IArray;
import model.machine.ICallStack;
import model.machine.IStackFrame;
import model.machine.IValue;
import model.program.IArrayElementAssignment;
import model.program.IArrayVariableDeclaration;
import model.program.IBlock;
import model.program.IExpression;

public class ArrayElementAssignment extends VariableAssignment implements IArrayElementAssignment {

	private ImmutableList<IExpression> indexes;

	public ArrayElementAssignment(IBlock parent, IArrayVariableDeclaration variable, List<IExpression> indexes, IExpression expression) {
		super(parent, variable, expression);
		this.indexes = ImmutableList.copyOf(indexes);
	}

	@Override
	public List<IExpression> getIndexes() {
		return indexes;
	}
	
	@Override
	public IArrayVariableDeclaration getVariable() {
		return (IArrayVariableDeclaration) super.getVariable();
	}

	@Override
	public String toString() {
		String text = getVariable().getId();
		for(IExpression e : indexes)
			text += "[" + e + "]";
		
		text += " = " + getExpression();
		return text;
	}
	
	@Override
	public boolean execute(ICallStack callStack, List<IValue> values) throws ExecutionError {
		assert values.size() == getIndexes().size() + 1;
		
		IStackFrame frame = callStack.getTopFrame();
		IValue valueArray = frame.getVariableValue(getVariable().getId());
		if(valueArray.isNull())
			throw new ExecutionError(ExecutionError.Type.NULL_POINTER, this, "null pointer", getVariable());
		
		IArray array = (IArray) valueArray;
		
		List<IExpression> indexes = getIndexes();
		IValue v = array;
		for(int i = 0; i < indexes.size()-1; i++) {
			int index = ((Number) values.get(i).getValue()).intValue();
			v = array.getElement(index);
		}
		int index = ((Number) values.get(indexes.size()-1).getValue()).intValue();
//		IValue value = frame.evaluate(getExpression());
		((IArray) v).setElement(index, values.get(values.size()-1));
		return true;
	}
}
