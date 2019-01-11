package model.machine.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.machine.IArray;
import model.machine.ICallStack;
import model.machine.IStackFrame;
import model.machine.IValue;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IProcedure;
import model.program.IStatement;
import model.program.IVariableDeclaration;

class StackFrame implements IStackFrame {

	private final ICallStack callStack;
	private final IStackFrame parent;
	private final IProcedure procedure;
	private final Map<String, IValue> variables;
	private IValue returnValue;
	
	public StackFrame(ICallStack callStack, IStackFrame parent, IProcedure procedure, List<IValue> arguments) {
		assert procedure.getNumberOfParameters() == arguments.size();
		
		this.callStack = callStack;
		this.parent = parent;
		this.procedure = procedure;
		this.variables = new HashMap<>();
		this.returnValue = IValue.NULL;
		
		int i = 0;
		for(IVariableDeclaration param : procedure.getParameters()) {
			variables.put(param.getIdentifier(), arguments.get(i));
			i++;
		}
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

	@Override
	public IValue getVariable(String name) {
		return variables.get(name);
	}

	@Override
	public void addVariable(String identifier, IDataType type) {
		assert identifier != null && !identifier.isEmpty() && !variables.containsKey(identifier);
		variables.put(identifier, IValue.NULL);
	}
	
	@Override
	public void setVariable(String identifier, IValue value) {
		assert variables.containsKey(identifier);
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
		return 0; // TODO
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
	public void terminateFrame(IValue returnValue) {
		callStack.terminateTopFrame(returnValue);
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
	public IArray getArray(IDataType baseType, int length) {
		return callStack.getProgramState().getArray(baseType, length);
	}
	
	@Override
	public void execute(IStatement statement) {
		System.out.println(statement);
		statement.execute(this.getCallStack());
		
	}

	@Override
	public void evaluate(IExpression expression) {
		// TODO Auto-generated method stub
		
	}
}
