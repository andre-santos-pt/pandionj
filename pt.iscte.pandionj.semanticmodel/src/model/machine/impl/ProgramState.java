package model.machine.impl;

import java.util.ArrayList;
import java.util.Arrays;
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
	private final int loopIterationMax;
	
	private ISourceElement instructionPointer;
	
	private ExecutionData data;
	
	public ProgramState(IProgram program) {
		this(program, 1024, 100); // TODO Constants?
	}
	
	public ProgramState(IProgram program, int callStackMax, int loopIterationMax) {
		assert program != null;
		assert callStackMax >= 1;
		this.program = program;
		this.callStackMax = callStackMax;
		this.loopIterationMax = loopIterationMax;
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
	public int getLoopIterationMaximum() {
		return loopIterationMax;
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
	public IArray allocateArray(IDataType type, int ... dimensions) {
		assert dimensions.length > 0;
		Array array = new Array(this, type, dimensions[0]);
		if(dimensions.length == 1) {
			for(int i = 0; i < dimensions[0]; i++)
			array.setElement(i, Value.create(type, type.getDefaultValue()));
		}
		for(int i = 1; i < dimensions.length; i++) {
			int[] remainingDims = Arrays.copyOfRange(dimensions, i, dimensions.length);
			for(int j = 0; j < dimensions[0]; j++)
				array.setElement(j, allocateArray(type, remainingDims));
		}
		return array;
	}
	
	public IExecutionData execute(IProcedure procedure, Object ... args) {
		
		if(args.length != procedure.getNumberOfParameters())
			throw new RuntimeException("incorrect number of arguments for " + procedure);
		
		instructionPointer = procedure.getBody().getStatements().get(0); // TODO no statements
		data = new ExecutionData();
		
		Factory factory = new Factory();
		List<IExpression> procArgs = new ArrayList<>(args.length);
		for(Object a : args)
			procArgs.add(factory.literalMatch(a.toString()));
		
		IProcedureCallExpression call = procedure.callExpression(procArgs);
		try {
			call.evaluate(stack);
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
						data.countOperation(expression.getOperationType());
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
		// TODO heapMemory
		return null;
	}
}
