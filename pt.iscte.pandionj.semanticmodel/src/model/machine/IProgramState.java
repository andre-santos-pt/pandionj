package model.machine;

import java.util.List;

import model.program.IDataType;
import model.program.IProcedure;
import model.program.ISourceElement;

public interface IProgramState {
	ICallStack getCallStack();
	List<IValue> getHeapMemory();
	int getCallStackMaximum();
	ISourceElement getInstructionPointer();
	IValue getValue(String literal);
	IValue getValue(Object object);
	
	IArray allocateArray(IDataType baseType, int length);
	
	IExecutionData execute(IProcedure procedureName, String ... args);
//	default IExecutionData execute(String procedureName, String ... args) {
//		
//	}
	
	interface IListener {
		default void programEnded() { }
		default void instructionPointerMoved(ISourceElement currentInstruction) { }
		default void addedToHeap(IValue value) { }
		default void removedFromHeap(IValue value) { }
	}
}
