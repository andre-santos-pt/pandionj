package model.machine;

import java.util.Map;

import model.program.IOperator.OperationType;
import model.program.IProcedure;

public interface IExecutionData {
//	Map<IProcedure, Integer> getNumberOfProcedureCalls();
	Map<IProcedure, Integer> getAssignmentData();
//	Map<IProcedure, Integer> getNumberOfComparisons();
	
	int getTotalAssignments();
	int getOperationCount(OperationType operation);
	
	int getTotalProcedureCalls();
	
	int getCallStackDepth();
	IValue getReturnValue();
	IValue getVariableValue(String id);
//	int getTotalMemory();
	
	default void printResult() {
		System.out.println(getReturnValue());
	}
}
