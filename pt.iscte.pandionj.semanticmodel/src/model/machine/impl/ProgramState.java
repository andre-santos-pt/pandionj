package model.machine.impl;

import java.util.List;

import com.google.common.collect.ImmutableList;

import model.machine.ICallStack;
import model.machine.IMemorySegment;
import model.machine.IProgramState;
import model.machine.IValue;
import model.program.IDataType;
import model.program.IProcedure;
import model.program.IProcedureCall;
import model.program.IProgram;
import model.program.ISourceElement;
import model.program.impl.ProcedureCall;

public class ProgramState implements IProgramState {

	private IProgram program;
	private ICallStack stack;	
	private ISourceElement instructionPointer;
	
	public ProgramState(IProgram program) {
		this.program = program;
		stack = new CallStack(this);
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
				return new Value(type, type.match(object.toString()));
		}
		return null;
	}
	
	public void execute() {
		IProcedure main = program.getMainProcedure();
		instructionPointer = program.getMainProcedure().getBody().getStatements().get(0);
		IProcedureCall call = new ProcedureCall(main, ImmutableList.of());
//		IStackFrame rootFrame = stack.newFrame(main, ImmutableList.of()); // root frame (empty)
		call.execute(stack);
//		stack.terminateTopFrame(Value.NULL_VALUE);
//		program.getMainProcedure().execute(stack.getTopFrame());
	}

}
