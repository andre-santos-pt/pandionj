package model.machine;

import java.util.Collection;
import java.util.List;

import model.program.IDataType;
import model.program.ISourceElement;

public interface IProgramState {
	ICallStack getCallStack();
	List<IMemorySegment> getHeapMemory();
	ISourceElement getInstructionPointer();
	Collection<IDataType> getDataTypes();

	// getStackCapacity()?
	// getHeapMemory()?
	
	IValue getValue(String value);
	
}
