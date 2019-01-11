package model.machine;

import java.util.List;

import model.program.IDataType;
import model.program.ISourceElement;

public interface IProgramState {
	ICallStack getCallStack();
	List<IMemorySegment> getHeapMemory();
	ISourceElement getInstructionPointer();
	int getCallStackMaximum();
	// getHeapMemory()?
	
	IValue getValue(String value);
	IValue getValue(Object object);
	
	IArray getArray(IDataType baseType, int length);
	
	void execute();
	
	// IExecutionData getExecutionData()
}
