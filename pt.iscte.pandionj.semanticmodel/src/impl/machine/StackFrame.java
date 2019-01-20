package impl.machine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import impl.machine.ExpressionEvaluator.Step;
import model.machine.IArray;
import model.machine.ICallStack;
import model.machine.IEvaluable;
import model.machine.IExecutable;
import model.machine.IStackFrame;
import model.machine.IStructObject;
import model.machine.IValue;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IProcedure;
import model.program.IProgramElement;
import model.program.IStatement;
import model.program.IStructType;
import model.program.IVariableDeclaration;

class StackFrame implements IStackFrame {

	private final CallStack callStack;
	private final StackFrame parent;
	private final IProcedure procedure;
	private final Map<String, IValue> variables;
	private IValue returnValue;
	private ProcedureExecutor executor;

	private List<IListener> listeners = new ArrayList<>(5);
	public void addListener(IListener listener) {
		listeners.add(listener);
	}

	public StackFrame(CallStack callStack, StackFrame parent, IProcedure procedure, List<IValue> arguments) {
		assert procedure.getNumberOfParameters() == arguments.size();

		this.callStack = callStack;
		this.parent = parent;
		this.procedure = procedure;
		this.variables = new LinkedHashMap<>();
		this.returnValue = IValue.NULL;

		int i = 0;
		for(IVariableDeclaration param : procedure.getParameters()) {
			variables.put(param.getId(), arguments.get(i));
			i++;
		}

		for (IVariableDeclaration var : procedure.getVariables(false))
			variables.put(var.getId(), Value.create(var.getType(), var.getType().getDefaultValue()));

		executor = new ProcedureExecutor(procedure);
	}

	@Override
	public IStackFrame getParent() {
		return parent;
	}

	@Override
	public IProcedure getProcedure() {
		return procedure;
	}

	@Override
	public Map<String, IValue> getVariables() {
		return Collections.unmodifiableMap(variables);
	}

	@Override // TODO copy when not reference
	public IValue getVariable(String name) {
		return variables.get(name);
	}

	public void setVariable(String identifier, IValue value) {
		assert variables.containsKey(identifier) : identifier;
		variables.put(identifier, value);
	}

	@Override
	public IValue getReturn() {
		return returnValue;
	}

	public void setReturn(IValue value) {
		this.returnValue = value;
	}

	@Override
	public int getMemory() {
		return 0; // TODO stack frame memory
	}

	@Override
	public ICallStack getCallStack() {
		return callStack;
	}

	@Override
	public IStackFrame newChildFrame(IProcedure procedure, List<IValue> args) {
		return callStack.newFrame(procedure, args);
	}

	@Override
	public void terminateFrame() {
		callStack.terminateTopFrame(returnValue);
		for(IListener l : listeners)
			l.terminated(returnValue);
	}

	@Override
	public IValue getValue(String literal) {
		return callStack.getProgramState().getValue(literal);
	}

	@Override
	public IValue getValue(Object object) {
		return callStack.getProgramState().getValue(object);
	}

	@Override
	public IArray allocateArray(IDataType baseType, int[] dimensions) {
		return callStack.getProgramState().allocateArray(baseType, dimensions);
	}

	@Override
	public IStructObject allocateObject(IStructType type) {
		return callStack.getProgramState().allocateObject(type);
	}

	public boolean isOver() {
		return executor.isOver();
	}



	public void stepIn() throws ExecutionError {
		assert !isOver();
		IProgramElement current = executor.current();
		if(current instanceof IStatement) {
			IStatement s = (IStatement) current;
			if(pendingEvaluations == null) {
				pendingEvaluations = new ArrayDeque<>();
				values = new ArrayList<IValue>();
				s.getExpressionParts().forEach(e -> pendingEvaluations.offer(new ExpressionEvaluator(e, callStack)));

				if(pendingEvaluations.isEmpty()) {
					execute(s);
					values = null;
					pendingEvaluations = null;
					executor.moveNext(null);
				}
				else {
					pendingEvaluations.peek().step();
				}
			}
			else if(!pendingEvaluations.isEmpty()) {
				if(pendingEvaluations.peek().isComplete()) {
					ExpressionEvaluator e = pendingEvaluations.poll();
					values.add(e.getValue());
					if(pendingEvaluations.isEmpty()) {
						execute(s);
						values = null;
						pendingEvaluations = null;
						executor.moveNext(null);
					}
				}
				else {
					pendingEvaluations.peek().step();
				}
			}
			else {
				execute(s);
				values = null;
				pendingEvaluations = null;
				executor.moveNext(null);
			}
		}
		else if(current instanceof IExpression) {
			IExpression e = (IExpression) current;
			if(pendingExpression == null) {
				pendingExpression = new ExpressionEvaluator(e, callStack);
				Step step = pendingExpression.step();
			}
			else if(!pendingExpression.isComplete()) {
				Step step = pendingExpression.step();
				if(pendingExpression.isComplete()) {
					IValue value = pendingExpression.getValue();
					pendingExpression = null;
					executor.moveNext(value);
				}
			}
			else {
				IValue value = pendingExpression.getValue();
				pendingExpression = null;
				executor.moveNext(value);
			}
		}
		else {
			System.out.println("C " + current);
			executor.moveNext(null);
		}
		if(executor.isOver())
			terminateFrame();
	}

	private ExpressionEvaluator pendingExpression;
	private Queue<ExpressionEvaluator> pendingEvaluations;
	private List<IValue> values;


	public IValue evaluate(IExpression expression, List<IValue> expressions) throws ExecutionError {
		for(IListener l : listeners)
			l.expressionEvaluationStart(expression);

		IValue value = ((IEvaluable) expression).evalutate(expressions, callStack);
		
		for(IListener l : listeners)
			l.expressionEvaluationEnd(expression, value);

		return value;
	}

	//	private IValue evaluate(IExpression expression) throws ExecutionError {
	//	if(expression instanceof ILiteral)
	//		return getCallStack().getProgramState().getValue(((ILiteral) expression).getStringValue());
	//
	//	try {
	//		for(IListener l : listeners)
	//			l.expressionEvaluationStart(expression);
	//		
	//		IValue value = expression.evaluate(callStack);
	//		
	//		System.out.println("E " + expression + " = " + value);
	//		for(IListener l : listeners)
	//			l.expressionEvaluationEnd(expression, value);
	//
	//		return value;
	//	}
	//	catch(ExecutionError e) {
	//		throw e;
	//	}
	//}




	private boolean execute(IStatement statement) throws ExecutionError {
		try {
			IExecutable executable = (IExecutable) statement;
			for(IListener l : listeners)
				l.statementExecutionStart(statement);

			boolean result = executable.execute(this.getCallStack(), values);
			System.out.println("S " + statement + " " + result);
			for(IListener l : listeners)
				l.statementExecutionEnd(statement);

			return result;
		}
		catch(ExecutionError e) {
			throw e;
		}
	}




	@Override
	public String toString() {
		String text = procedure.getId() + "(...)";
		for(Entry<String, IValue> e : variables.entrySet())
			text += " " + e.getKey() + "=" + e.getValue();
		return text;
	}
}
