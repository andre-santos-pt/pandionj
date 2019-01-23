package impl.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.google.common.collect.ImmutableList;

import model.machine.ICallStack;
import model.machine.IValue;
import model.program.IExpression;
import model.program.IProcedureCallExpression;

public class ExpressionEvaluator {

	private ICallStack callStack;
	private Stack<IExpression> expStack;
	private Stack<IValue> valueStack;

	public ExpressionEvaluator(IExpression expression, ICallStack callStack)  {
		this.callStack = callStack;
		expStack = new Stack<IExpression>();
		valueStack = new Stack<IValue>();
		expStack.push(expression);
	}


	public boolean isComplete() {
		return expStack.isEmpty();
	}

	public IValue evaluate() throws ExecutionError {
		while(!expStack.isEmpty())
			step();
		
		return getValue();
	}
	
	public IValue getValue() {
		assert isComplete();
		return valueStack.peek();
	}

	public Step step() throws ExecutionError {
		assert !isComplete();
		
		while(expStack.peek().isDecomposable() && valueStack.size() < expStack.peek().getNumberOfParts()) {
			expStack.peek().decompose().forEach(e -> expStack.push(e));
			
			while(!expStack.peek().isDecomposable())
				valueStack.push(callStack.getTopFrame().evaluate(expStack.pop(), ImmutableList.of()));
		}

		int parts = expStack.peek().getNumberOfParts();
		List<IValue> values = new ArrayList<>();
		while(parts-- > 0)
			values.add(valueStack.pop());

		IValue val = callStack.getTopFrame().evaluate(expStack.peek(), values);
		if(val == null) {
			IProcedureCallExpression callExp = (IProcedureCallExpression) expStack.pop();
			expStack.push(new ProcedureReturnExpression(callExp.getProcedure()));
		}
		else
			valueStack.push(val);
			
		return new Step(val == null ? null : expStack.pop(), val);
	}
	
	static class Step {
		final IExpression expression;
		final IValue value;
		 
		public Step(IExpression expression, IValue value) {
			this.expression = expression;
			this.value = value;
		}

		@Override
		public String toString() {
			return expression + " = " + value;
		}
	}
	
}
