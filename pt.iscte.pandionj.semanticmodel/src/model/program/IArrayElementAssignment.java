package model.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

import model.machine.IArray;
import model.machine.ICallStack;
import model.machine.IStackFrame;
import model.machine.IValue;

public interface IArrayElementAssignment extends IVariableAssignment {
	List<IExpression> getIndexes(); // not null, length == getDimensions
	default int getDimensions() {
		return getIndexes().size();
	}
	
	IArrayVariableDeclaration getVariable();
	
	@Override
	default List<ISemanticProblem> validateSematics() {
		if(!getVariable().getComponentType().equals(getExpression().getType()))
			return ImmutableList.of(ISemanticProblem.create("incompatible types", this, getExpression()));
		return ImmutableList.of();
	}
	
	@Override
	default boolean execute(ICallStack callStack) throws ExecutionError {
		IStackFrame frame = callStack.getTopFrame();
		IValue valueArray = frame.getVariable(getVariable().getId());
		if(valueArray.isNull())
			throw new ExecutionError(ExecutionError.Type.NULL_POINTER, this, "null pointer", getVariable());
		
		IArray array = (IArray) valueArray;
		
		List<IExpression> indexes = getIndexes();
		IValue v = array;
		for(int i = 0; i < indexes.size()-1; i++) {
			int index = ((Number) frame.evaluate(indexes.get(i)).getValue()).intValue();
			v = array.getElement(index);
		}
		int index = ((Number) frame.evaluate(indexes.get(indexes.size()-1)).getValue()).intValue();
		IValue value = frame.evaluate(getExpression());
		((IArray) v).setElement(index, value);
		return true;
	}
}
