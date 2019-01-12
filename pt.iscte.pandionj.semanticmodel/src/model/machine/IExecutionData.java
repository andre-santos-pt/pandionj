package model.machine;

import java.util.Map;

import model.program.IProcedure;

public interface IExecutionData {
//	Map<IProcedure, Integer> getNumberOfProcedureCalls();
	Map<IProcedure, Integer> getAssignmentData();
//	Map<IProcedure, Integer> getNumberOfComparisons();
	
	int getTotalAssignments();
	
	int getCallStackDepth();
//	int getTotalMemory();
}
