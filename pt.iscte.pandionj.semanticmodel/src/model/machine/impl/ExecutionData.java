package model.machine.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import model.machine.ICallStack;
import model.machine.IExecutionData;
import model.machine.IValue;
import model.program.IExpression;
import model.program.IExpression.OperationType;
import model.program.IProcedure;

public class ExecutionData implements IExecutionData  {
	private Map<IProcedure, Integer> assignmentCount = new HashMap<IProcedure, Integer>();
	
	private int callStackMax = 0;
	
	
	private int arithmeticCount = 0;
	private int relationalCount = 0;
	private int logicalCount = 0;
	private int callCount = 0;
	private int otherOpCount = 0;
	
	
	private IValue returnValue = IValue.NULL;
	
	@Override
	public Map<IProcedure, Integer> getAssignmentData() {
		return Collections.unmodifiableMap(assignmentCount);
	}

	@Override
	public int getTotalAssignments() {
		int s = 0;
		for(Integer i : assignmentCount.values())
			s += i;
		return s;
	}
	
	public int getTotalOperations() {
		return arithmeticCount + relationalCount + logicalCount + callCount + otherOpCount;
	}
	
	@Override
	public int getOperationCount(OperationType operation) {
		switch(operation) {
		case ARITHMETIC:	return arithmeticCount;
		case RELATIONAL:	return relationalCount;
		case LOGICAL:		return logicalCount;
		case CALL:			return callCount;
		case NONE: 			return otherOpCount;
		default:			return 0;
		}
	}
	
	@Override
	public int getCallStackDepth() {
		return callStackMax;
	}
	
	@Override
	public int getTotalProcedureCalls() {
		return callCount;
	}
	
	public void countAssignment(IProcedure p) {
		Integer c = assignmentCount.get(p);
		if(c == null)
			c = 1;
		else
			c++;
		assignmentCount.put(p, c);
	}

	public void updateCallStackSize(ICallStack stack) {
		callStackMax = Math.max(callStackMax, stack.getSize());
	}

	@Override
	public String toString() {
		String text = 
				"call stack depth: " + getCallStackDepth() + "\n" +
				"procedure calls: " + callCount + "\n" +
				"assignments: " + getTotalAssignments() + "\n" +
				"operations: " + getTotalOperations();
		return text;
	}

	public void countCall() {
		callCount++;
	}
	
	public void countOperation(IExpression.OperationType operation) {
		switch(operation) {
		case ARITHMETIC:	arithmeticCount++;	break;
		case RELATIONAL:	relationalCount++;	break;
		case LOGICAL:		logicalCount++;		break;
		case CALL:			callCount++;		break;
		case NONE: 			otherOpCount++; 	break;
		}
	}
	
	@Override
	public IValue getReturnValue() {
		return returnValue;
	}
	
	public void setReturnValue(IValue value) {
		returnValue = value;
	}

	@Override
	public IValue getVariableValue(String id) {
		return state.get(id);
	}
	
	private Map<String, IValue> state;
	
	public void setVariableState(Map<String, IValue> variables) {
		state = variables;
	}
}
