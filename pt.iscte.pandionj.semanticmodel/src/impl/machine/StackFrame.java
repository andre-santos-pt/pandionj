package impl.machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import model.machine.IArray;
import model.machine.ICallStack;
import model.machine.IStackFrame;
import model.machine.IStructObject;
import model.machine.IValue;
import model.program.ExecutionError;
import model.program.IDataType;
import model.program.IExpression;
import model.program.ILiteral;
import model.program.IProcedure;
import model.program.IStatement;
import model.program.IStructType;
import model.program.IVariableDeclaration;

class StackFrame implements IStackFrame {

	private final ICallStack callStack;
	private final IStackFrame parent;
	private final IProcedure procedure;
	private final Map<String, IValue> variables;
	private IValue returnValue;
	
	private List<IListener> listeners = new ArrayList<>(5);
	public void addListener(IListener listener) {
		listeners.add(listener);
	}
	
	public StackFrame(ICallStack callStack, IStackFrame parent, IProcedure procedure, List<IValue> arguments) {
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

	@Override
	public void setVariable(String identifier, IValue value) {
		assert variables.containsKey(identifier) : identifier;
		variables.put(identifier, value);
	}

	@Override
	public IValue getReturn() {
		return returnValue;
	}

	@Override
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
	public IArray getArray(IDataType baseType, int[] dimensions) {
		return callStack.getProgramState().allocateArray(baseType, dimensions);
	}

	@Override
	public IStructObject getObject(IStructType type) {
		return callStack.getProgramState().allocateObject(type);
	}
	
	@Override
	public boolean execute(IStatement statement) throws ExecutionError {
		try {
			for(IListener l : listeners)
				l.statementExecutionStart(statement);
			
			boolean result = statement.execute(this.getCallStack());
			
			for(IListener l : listeners)
				l.statementExecutionEnd(statement);
			
			return result;
		}
		catch(ExecutionError e) {
			throw e;
		}
	}

	@Override
	public IValue evaluate(IExpression expression) throws ExecutionError {
		if(expression instanceof ILiteral)
			return getCallStack().getProgramState().getValue(((ILiteral) expression).getStringValue());
		
		try {
			for(IListener l : listeners)
				l.expressionEvaluationStart(expression);
			
			IValue value = expression.evaluate(callStack);
			
			for(IListener l : listeners)
				l.expressionEvaluationEnd(expression, value);
			
			return value;
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
