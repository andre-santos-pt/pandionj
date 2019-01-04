package model.machine.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableMap;

import model.machine.ICallStack;
import model.machine.IMemorySegment;
import model.machine.IProgramState;
import model.machine.IStackFrame;
import model.machine.IValue;
import model.program.IDataType;
import model.program.IProgram;
import model.program.ISourceElement;

public class ProgramState implements IProgramState {

	private IProgram program;
	private List<IDataType> types;
	private ICallStack stack;	
	private ISourceElement instructionPointer;
	
	public ProgramState(IProgram program) {
		this.program = program;
		types = new ArrayList<>();
		types.add(new DataType("int", Integer.class));
		types.add(new DataType("double", Double.class));
		stack = new CallStack(this);
		instructionPointer = program.getMainProcedure().getBody().getStatements().get(0);
	}
	
	@Override
	public ICallStack getCallStack() {
		return stack;
	}

	@Override
	public List<IMemorySegment> getHeapMemory() {
		return null;
	}

	@Override
	public ISourceElement getInstructionPointer() {
		return instructionPointer;
	}

	@Override
	public Collection<IDataType> getDataTypes() {
		return Collections.unmodifiableCollection(types);
	}

	@Override
	public IValue getValue(String value) {
		for(IDataType type : types) {
			Object val = type.match(value);
			if(val != null)
				return new Value(type, val);
		}
		return null;
	}
	
	public void execute() {
		IStackFrame rootFrame = stack.newFrame(ImmutableMap.of()); // root frame (empty)
		program.getMainProcedure().execute(stack.getTopFrame());
	}
	
	
	private static class DataType implements IDataType {
		final String id;
		final Class<?> valueType;
		
		public DataType(String id, Class<?> valueType) {
			this.id = id;
			this.valueType = valueType;
		}

		@Override
		public String getIdentifier() {
			return id;
		}

		@Override
		public boolean matches(Object object) {
			return valueType.isInstance(object);
		}
		
		@Override
		public Object match(String literal) {
			try {
				Object obj = valueType.getConstructor(String.class).newInstance(literal);
				return obj;
			}
			catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}





	
}
