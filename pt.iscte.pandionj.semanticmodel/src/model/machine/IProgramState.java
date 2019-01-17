package model.machine;

import java.util.List;

import model.program.IDataType;
import model.program.IProcedure;
import model.program.ISourceElement;
import model.program.IStructType;

public interface IProgramState {
	ICallStack getCallStack();
	IHeapMemory getHeapMemory();
	int getCallStackMaximum();
	int getLoopIterationMaximum();
	int getAvailableMemory();
	int getUsedMemory();
	
	ISourceElement getInstructionPointer();
	IValue getValue(String literal);
	IValue getValue(Object object);
	
	IArray allocateArray(IDataType baseType, int ... dimensions);
	IStructObject allocateObject(IStructType type);
	
	IExecutionData execute(IProcedure procedureName, Object ... args);

	default int getMemory() {
		return getCallStack().getMemory() + getHeapMemory().getMemory();
	}
	
	interface IListener {
		default void programEnded() { }
		default void instructionPointerMoved(ISourceElement currentInstruction) { }
		
	}
}
