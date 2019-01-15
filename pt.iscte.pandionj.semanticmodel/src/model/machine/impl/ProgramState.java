package model.machine.impl;

import java.util.ArrayList;
import java.util.List;

import model.machine.IArray;
import model.machine.ICallStack;
import model.machine.IExecutionData;
import model.machine.IProgramState;
import model.machine.IStackFrame;
import model.machine.IValue;
import model.program.ExecutionError;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IProcedure;
import model.program.IProcedureCallExpression;
import model.program.IProgram;
import model.program.ISourceElement;
import model.program.IStatement;
import model.program.IVariableAssignment;
import model.program.impl.Factory;

public class ProgramState implements IProgramState {

	private IProgram program;
	private ICallStack stack;
	private final int callStackMax;
	private ISourceElement instructionPointer;
	
	private ExecutionData data;
	
	public ProgramState(IProgram program) {
		this(program, 1024); // TODO Constants?
	}
	
	public ProgramState(IProgram program, int callStackMax) {
		assert program != null;
		assert callStackMax >= 1;
		this.program = program;
		this.callStackMax = callStackMax;
		stack = new CallStack(this);
		addStateListener();
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
	public ISourceElement getInstructionPointer() {
		return instructionPointer;
	}

	@Override
	public IValue getValue(String value) {
		for(IDataType type : program.getDataTypes()) {
			if(type.matchesLiteral(value))
				return Value.create(type, type.create(value));
		}
		return null;
	}
	
	@Override
	public IValue getValue(Object object) {
		for(IDataType type : program.getDataTypes()) {
			if(type.matches(object))
				return Value.create(type, type.create(object.toString()));
		}
		assert false;
		return null;
	}
	
	
	@Override
	public IArray allocateArray(IDataType type, int length) {
		return new Array(this, type, length);
	}
	
	public IExecutionData execute(IProcedure procedure, String ... args) {
		
		if(args.length != procedure.getNumberOfParameters())
			throw new RuntimeException("incorrect number of arguments for " + procedure);
		
		instructionPointer = procedure.getBody().getStatements().get(0); // TODO no statements
		data = new ExecutionData();
		
		Factory factory = new Factory();
		List<IExpression> procArgs = new ArrayList<>(args.length);
		for(String a : args)
			procArgs.add(factory.literal(Integer.parseInt(a))); // FIXME other types of program args
		
		IProcedureCallExpression call = procedure.callExpression(procArgs);
		try {
//			stack.evaluate(call);
			call.evaluate(stack);
//			call.execute(stack);
		} catch (ExecutionError e) {
			System.err.println("Execution error: " + e);
		}
		return data;
	}

	private void addStateListener() {
		stack.addListener(new ICallStack.IListener() {
			@Override
			public void stackFrameCreated(IStackFrame stackFrame) {
				data.updateCallStackSize(stack);
				stackFrame.addListener(new IStackFrame.IListener() {
					public void statementExecutionEnd(IStatement statement) {
						if(statement instanceof IVariableAssignment)
							data.countAssignment(stackFrame.getProcedure());
					}
					
					@Override
					public void expressionEvaluationEnd(IExpression expression, IValue result) {
						if(expression.isOperation())
							data.countOperation();
//						else if(expression instanceof IProcedureCall) {
//							data.countCall();
//							System.out.println(((IProcedureCall) expression).getProcedure().getIdentifier() + "(...) -> " + result);
//						}
					}
					
					
//					public void terminated(IValue result) {
//						System.out.println(stackFrame.getProcedure() + " returns " + result);
//					}
				});
			}
			
			@Override
			public void stackFrameTerminated(IStackFrame stackFrame, IValue returnValue) {
				System.out.println(stackFrame.getProcedure() + " returns " + returnValue);
				data.setVariableState(stackFrame.getVariables());
				data.setReturnValue(returnValue);
				data.countCall();
			}
		});
	}

	
	void countAssignment(IProcedure p) {
		data.countAssignment(p);
	}
	
	@Override
	public List<IValue> getHeapMemory() {
		// TODO Auto-generated method stub
		return null;
	}
}
