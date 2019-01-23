package model.machine;

import model.program.IDataType;
import model.program.IModule;
import model.program.IProgramElement;
import model.program.IStructType;

public interface IProgramState {
	IModule getProgram();
	ICallStack getCallStack();
	IHeapMemory getHeapMemory();
	int getCallStackMaximum();
	int getLoopIterationMaximum();
	int getAvailableMemory();
	int getUsedMemory();
	
	IProgramElement getInstructionPointer();
	IValue getValue(String literal);
	IValue getValue(Object object);
	
	IArray allocateArray(IDataType baseType, int ... dimensions);
	IStructObject allocateObject(IStructType type);
	
//	void launchExecution(IProcedure procedure, Object ... args);
//
//	void stepIn();
	
	default int getMemory() {
		return getCallStack().getMemory() + getHeapMemory().getMemory();
	}
	
	interface IListener {
		default void programEnded() { }
		default void instructionPointerMoved(IProgramElement currentInstruction) { }
		default void infiniteLoop() { }
		
	}
}
