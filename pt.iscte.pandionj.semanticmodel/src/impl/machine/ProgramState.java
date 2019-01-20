package impl.machine;

import java.util.ArrayList;
import java.util.List;

import impl.program.Factory;
import model.machine.IArray;
import model.machine.ICallStack;
import model.machine.IExecutionData;
import model.machine.IHeapMemory;
import model.machine.IProgramState;
import model.machine.IStackFrame;
import model.machine.IStructObject;
import model.machine.IValue;
import model.program.IBinaryExpression;
import model.program.IDataType;
import model.program.IExpression;
import model.program.ILiteral;
import model.program.IProcedure;
import model.program.IProcedureCallExpression;
import model.program.IProgram;
import model.program.IProgramElement;
import model.program.IStatement;
import model.program.IStructType;
import model.program.IVariableAssignment;

public class ProgramState implements IProgramState {

	private final IProgram program;
	private final CallStack stack;
	private final IHeapMemory heap;
	private final int callStackMax;
	private final int loopIterationMax;
	private final int availableMemory;

	private IProgramElement instructionPointer;

	private ExecutionData data;

	private boolean stepBystep = false;

	public ProgramState(IProgram program) {
		this(program, 1024, 100, 1024); // TODO Constants?
	}

	public ProgramState(IProgram program, int callStackMax, int loopIterationMax, int availableMemory) {
		assert program != null;
		assert callStackMax >= 1;
		this.program = program;
		this.callStackMax = callStackMax;
		this.loopIterationMax = loopIterationMax;
		this.availableMemory = availableMemory;
		stack = new CallStack(this);
		heap = new HeapMemory(this); 
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
	public IProgramElement getInstructionPointer() {
		return instructionPointer;
	}

	@Override
	public IHeapMemory getHeapMemory() {
		return heap;
	}

	@Override
	public int getAvailableMemory() {
		return availableMemory;
	}

	@Override
	public int getUsedMemory() {
		return stack.getMemory() + heap.getMemory();
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
	public IArray allocateArray(IDataType baseType, int ... dimensions) {
		return heap.allocateArray(baseType, dimensions);
	}

	@Override
	public IStructObject allocateObject(IStructType type) {
		return heap.allocateObject(type);
	}

	public void launchExecution(IProcedure procedure, Object... args) throws ExecutionError {
		if(args.length != procedure.getNumberOfParameters())
			throw new RuntimeException("incorrect number of arguments for " + procedure);

		//		instructionPointer = procedure.getBody().getStatements().get(0); // TODO instruction pointer
		data = new ExecutionData();

		Factory factory = new Factory();
		List<IExpression> procArgs = new ArrayList<>(args.length);
		for(Object a : args)
			procArgs.add(factory.literalMatch(a.toString()));

		IProcedureCallExpression call = procedure.callExpression(procArgs);
		instructionPointer = call;

		List<IValue> argsValues = new ArrayList<>();
		for(int i = 0; i < procedure.getNumberOfParameters(); i++) {
			IValue arg = stack.evaluate((ILiteral) procArgs.get(i));
			argsValues.add(arg);
		}
		IStackFrame newFrame = stack.newFrame(procedure, argsValues);
	}

	public void stepIn() throws ExecutionError {
		assert !isOver();
		stack.stepIn();
	}

	public boolean isOver() {
		return stack.isEmpty();
	}

	public IExecutionData execute(IProcedure procedure, Object ... args) {
		try {
			launchExecution(procedure, args);
			while(!isOver())
				stepIn();
		}
		catch (ExecutionError e) {
			System.err.println("Execution error: " + e);
		}
		return data;
	}

	//	public IExecutionData execute(IProcedure procedure, Object ... args) {
	//
	//		if(args.length != procedure.getNumberOfParameters())
	//			throw new RuntimeException("incorrect number of arguments for " + procedure);
	//
	//		//		instructionPointer = procedure.getBody().getStatements().get(0); // TODO instruction pointer
	//		data = new ExecutionData();
	//
	//		Factory factory = new Factory();
	//		List<IExpression> procArgs = new ArrayList<>(args.length);
	//		for(Object a : args)
	//			procArgs.add(factory.literalMatch(a.toString()));
	//
	//		IProcedureCallExpression call = procedure.callExpression(procArgs);
	//		instructionPointer = call;
	//		try {
	//			call.evaluate(stack);
	//		} catch (ExecutionError e) {
	//			System.err.println("Execution error: " + e);
	//		}
	//		return data;
	//	}



	private void addStateListener() {
		stack.addListener(new ICallStack.IListener() {
			@Override
			public void stackFrameCreated(IStackFrame stackFrame) {
				data.updateCallStackSize(stack);

				stackFrame.addListener(new IStackFrame.IListener() {
					@Override
					public void statementExecutionStart(IStatement statement) {
							instructionPointer = statement;
//							System.out.println("+ " + instructionPointer);
					}

					public void statementExecutionEnd(IStatement statement) {
						if(statement instanceof IVariableAssignment)
							data.countAssignment(stackFrame.getProcedure());
					}

					@Override
					public void expressionEvaluationStart(IExpression expression) {
						if(expression instanceof IBinaryExpression) {
							instructionPointer = expression;
//							System.out.println("+ " + instructionPointer);
						}
					}

					@Override
					public void expressionEvaluationEnd(IExpression expression, IValue result) {
						data.countOperation(expression.getOperationType());
					}
				});
			}

			@Override
			public void stackFrameTerminated(IStackFrame stackFrame, IValue returnValue) {
				System.out.println(stackFrame.getProcedure().getId() + " returns " + returnValue);
				data.setVariableState(stackFrame.getVariables());
				data.setReturnValue(returnValue);
				data.countCall();
			}
		});
	}

	void countAssignment(IProcedure p) {
		data.countAssignment(p);
	}



}
