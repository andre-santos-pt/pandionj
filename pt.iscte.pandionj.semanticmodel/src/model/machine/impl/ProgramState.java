package model.machine.impl;

import java.util.List;

import com.google.common.collect.ImmutableList;

import model.machine.IArray;
import model.machine.ICallStack;
import model.machine.IMemorySegment;
import model.machine.IProgramState;
import model.machine.IValue;
import model.program.IDataType;
import model.program.IProcedure;
import model.program.IProcedureCall;
import model.program.IProgram;
import model.program.ISourceElement;
import model.program.impl.Factory;

public class ProgramState implements IProgramState {

	private IProgram program;
	private ICallStack stack;
	private final int callStackMax;
	private ISourceElement instructionPointer;
	
	public ProgramState(IProgram program) {
		this(program, 1024); // TODO Constants?
	}
	
	public ProgramState(IProgram program, int callStackMax) {
		assert program != null;
		assert callStackMax >= 1;
		this.program = program;
		this.callStackMax = callStackMax;
		stack = new CallStack(this);
	}
	
	@Override
	public ICallStack getCallStack() {
		return stack;
	}

	@Override
	public int getCallStackMaximum() {
		return callStackMax;
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
	public IValue getValue(String value) {
		for(IDataType type : program.getDataTypes()) {
			Object val = type.match(value);
			if(val != null)
				return new Value(type, val);
		}
		return null;
	}
	
	@Override
	public IValue getValue(Object object) {
		for(IDataType type : program.getDataTypes()) {
			if(type.matches(object))
				if(type.equals(IDataType.BOOLEAN))
					return object == Boolean.TRUE ? IValue.TRUE : IValue.FALSE;
				else
					return new Value(type, type.match(object.toString()));
		}
		return null;
	}
	
	
	@Override
	public IArray getArray(IDataType type, int length) {
		return new Array(this, type, length);
	}
	
	public void execute() {
		Factory factory = new Factory();
		
		IProcedure main = program.getMainProcedure();
		if(main == null)
			throw new RuntimeException("no main procedure defined");
		
		instructionPointer = program.getMainProcedure().getBody().getStatements().get(0); // TODO no statements
		IProcedureCall call = factory.procedureCall(main, ImmutableList.of());
		call.execute(stack);
	}

}
