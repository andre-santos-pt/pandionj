package model.machine;

import com.google.common.util.concurrent.ExecutionError;

import model.program.IDataType;
import model.program.IStructType;

public interface IHeapMemory {

	

	IArray allocateArray(IDataType baseType, int ... dimensions) throws ExecutionError;

	IStructObject allocateObject(IStructType type) throws ExecutionError;
	
	default int getMemory() {
		return 0;
	}
	
	interface IListener {
		default void allocated(IValue value) { }
		default void deallocated(IValue value) { }
	}
}
