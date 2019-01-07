package model.machine;

import java.util.List;

import model.program.ISourceElement;

public interface IProgramState {
	ICallStack getCallStack();
	List<IMemorySegment> getHeapMemory();
	ISourceElement getInstructionPointer();
	// getStackCapacity()?
	// getHeapMemory()?
	
	IValue getValue(String value);
	IValue getValue(Object object);
	
	void execute();
	
	// IExecutionData getExecutionData()
}
