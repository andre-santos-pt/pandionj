package impl.machine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.util.concurrent.ExecutionError;

import model.machine.IArray;
import model.machine.IHeapMemory;
import model.machine.IStructObject;
import model.machine.IValue;
import model.program.IDataType;
import model.program.IStructType;

public class HeapMemory implements IHeapMemory {
	private ProgramState state;
	private List<IValue> objects;
	
	public HeapMemory(ProgramState state) {
		this.state = state;
		objects = new ArrayList<>();
	}

	@Override
	public IArray allocateArray(IDataType baseType, int... dimensions) throws ExecutionError {
		assert dimensions.length > 0;
		Array array = new Array(state, baseType, dimensions[0]);
		if(dimensions.length == 1) {
			for(int i = 0; i < dimensions[0]; i++)
			array.setElement(i, Value.create(baseType, baseType.getDefaultValue()));
		}
		for(int i = 1; i < dimensions.length; i++) {
			int[] remainingDims = Arrays.copyOfRange(dimensions, i, dimensions.length);
			for(int j = 0; j < dimensions[0]; j++)
				array.setElement(j, allocateArray(baseType, remainingDims));
		}
		objects.add(array);
		return array;
	}

	@Override
	public IStructObject allocateObject(IStructType type) throws ExecutionError {
		StructObject object = new StructObject(type);
		objects.add(object);
		return object;
	}
}
