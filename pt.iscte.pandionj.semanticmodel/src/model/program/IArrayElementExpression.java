package model.program;

import java.util.List;

import model.machine.IArray;
import model.machine.IStackFrame;
import model.machine.IValue;

public interface IArrayElementExpression extends IVariableExpression {

	IArrayVariableDeclaration getVariable();
	
	List<IExpression> getIndexes(); // size() >= 1
	
	@Override
	default IValue evaluate(IStackFrame frame) {
		IValue variable = frame.getVariable(getVariable().getIdentifier());
		IValue element = variable;
		for(IExpression e : getIndexes()) {
			IValue i = e.evaluate(frame);
			if((int) i.getValue() < 0)
				throw new ExecutionError() {
					
					@Override
					public ISourceElement getSourceElement() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public String getMessage() {
						return "negative";
					}
				};
			element = ((IArray) element).getElement((int) i.getValue());
		}
		return element;
	}
}
