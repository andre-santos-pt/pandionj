package model.machine;

import java.util.Map;

import model.program.IProcedure;

public interface IExecutionData {
//	Map<IProcedure, Integer> getNumberOfProcedureCalls();
	Map<IProcedure, Integer> getAssignmentData();
//	Map<IProcedure, Integer> getNumberOfComparisons();
	
	int getTotalAssignments();
	int getTotalOperations();
	
	int getCallStackDepth();
	IValue getReturnValue();
	IValue getVariableValue(String id);
//	int getTotalMemory();
}
