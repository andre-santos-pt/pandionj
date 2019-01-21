package impl.machine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import impl.machine.ExpressionEvaluator.Step;
import model.machine.IArray;
import model.machine.ICallStack;
import model.machine.IEvaluable;
import model.machine.IExecutable;
import model.machine.IStackFrame;
import model.machine.IStructObject;
import model.machine.IValue;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IProcedure;
import model.program.IProgramElement;
import model.program.IStatement;
import model.program.IStructType;
import model.program.IVariableDeclaration;

class StackFrame implements IStackFrame {

	private final CallStack callStack;
//	private final StackFrame parent;
	private final IProcedure procedure;
	private final Map<String, VariableHistory> variables;
	private IValue returnValue;
	private ProcedureExecutor executor;

	
	private int time;
	private int timeBackwards;
	
	private List<IListener> listeners = new ArrayList<>(5);
	public void addListener(IListener listener) {
		listeners.add(listener);
	}

	public StackFrame(CallStack callStack, StackFrame parent, IProcedure procedure, List<IValue> arguments) {
		assert procedure.getNumberOfParameters() == arguments.size();

		this.callStack = callStack;
//		this.parent = parent;
		this.procedure = procedure;
		this.variables = new LinkedHashMap<>();
		this.returnValue = IValue.NULL;

		this.time = 0;
		this.timeBackwards = 0;
		
		int i = 0;
		for(IVariableDeclaration param : procedure.getParameters()) {
			variables.put(param.getId(), new VariableHistory(arguments.get(i)));
			i++;
		}

		for (IVariableDeclaration var : procedure.getVariables(false)) {
			IValue defValue = Value.create(var.getType(), var.getType().getDefaultValue());
			variables.put(var.getId(), new VariableHistory(defValue));
		}

		executor = new ProcedureExecutor(procedure);
	}

//	@Override
//	public IStackFrame getParent() {
//		return parent;
//	}

	@Override
	public IProcedure getProcedure() {
		return procedure;
	}

	@Override
	public Map<String, IValue> getVariables() {
		return variables.entrySet().stream().collect(Collectors.toMap(
		                e -> e.getKey(),
		                e -> e.getValue().getCurrent()));
	}

	@Override // TODO copy when not reference
	public IValue getVariableValue(String id) {
		assert variables.containsKey(id);
		return variables.get(id).getCurrent();
	}

	public List<IValue> getVariableValueHistory(String id) {
		assert variables.containsKey(id);
		return variables.get(id).getHistory();
	}
	
	public void setVariable(String id, IValue value) {
		assert variables.containsKey(id) : id;
		variables.get(id).newValue(value);
	}

	@Override
	public IValue getReturn() {
		return returnValue;
	}

	public void setReturn(IValue value) {
		this.returnValue = value;
	}

	@Override
	public int getMemory() {
		int bytes = 0;
		for (IVariableDeclaration var : procedure.getVariables(true))
			bytes += var.getType().getMemoryBytes();
		return bytes;
	}

	@Override
	public ICallStack getCallStack() {
		return callStack;
	}

	@Override
	public IStackFrame newChildFrame(IProcedure procedure, List<IValue> args) {
		return callStack.newFrame(procedure, args);
	}

	@Override
	public void terminateFrame() {
		callStack.terminateTopFrame(returnValue);
		for(IListener l : listeners)
			l.terminated(returnValue);
	}

	@Override
	public IValue getValue(String literal) {
		return callStack.getProgramState().getValue(literal);
	}

	@Override
	public IValue getValue(Object object) {
		return callStack.getProgramState().getValue(object);
	}

	@Override
	public IArray allocateArray(IDataType baseType, int[] dimensions) {
		return callStack.getProgramState().allocateArray(baseType, dimensions);
	}

	@Override
	public IStructObject allocateObject(IStructType type) {
		return callStack.getProgramState().allocateObject(type);
	}

	public boolean isOver() {
		return executor.isOver();
	}


	public void stepIn() throws ExecutionError {
		assert !isOver();
		time++;
		IProgramElement current = executor.current();
		if(current instanceof IStatement) {
			IStatement s = (IStatement) current;
			if(pendingEvaluations == null) {
				pendingEvaluations = new ArrayDeque<>();
				values = new ArrayList<IValue>();
				s.getExpressionParts().forEach(e -> pendingEvaluations.offer(new ExpressionEvaluator(e, callStack)));

				if(pendingEvaluations.isEmpty()) {
					execute(s);
					values = null;
					pendingEvaluations = null;
					executor.moveNext(null);
				}
				else {
					pendingEvaluations.peek().step();
				}
			}
			else if(!pendingEvaluations.isEmpty()) {
				if(pendingEvaluations.peek().isComplete()) {
					ExpressionEvaluator e = pendingEvaluations.poll();
					values.add(e.getValue());
					if(pendingEvaluations.isEmpty()) {
						execute(s);
						values = null;
						pendingEvaluations = null;
						executor.moveNext(null);
					}
				}
				else {
					pendingEvaluations.peek().step();
				}
			}
			else {
				execute(s);
				values = null;
				pendingEvaluations = null;
				executor.moveNext(null);
			}
		}
		else if(current instanceof IExpression) {
			IExpression e = (IExpression) current;
			if(pendingExpression == null) {
				pendingExpression = new ExpressionEvaluator(e, callStack);
				Step step = pendingExpression.step();
			}
			else if(!pendingExpression.isComplete()) {
				Step step = pendingExpression.step();
				if(pendingExpression.isComplete()) {
					IValue value = pendingExpression.getValue();
					pendingExpression = null;
					executor.moveNext(value);
				}
			}
			else {
				IValue value = pendingExpression.getValue();
				pendingExpression = null;
				executor.moveNext(value);
			}
		}
		else {
			System.out.println("C " + current);
			executor.moveNext(null);
		}
		if(executor.isOver())
			terminateFrame();
	}


	public void stepOver() throws ExecutionError {
		// TODO step over
	}
	
	private ExpressionEvaluator pendingExpression;
	private Queue<ExpressionEvaluator> pendingEvaluations;
	private List<IValue> values;


	public IValue evaluate(IExpression expression, List<IValue> expressions) throws ExecutionError {
		for(IListener l : listeners)
			l.expressionEvaluationStart(expression);

		IValue value = ((IEvaluable) expression).evalutate(expressions, callStack);
		
		for(IListener l : listeners)
			l.expressionEvaluationEnd(expression, value);

		return value;
	}

	private List<StatementTimestamp> historyStatements = new ArrayList<StackFrame.StatementTimestamp>();
	
	private boolean execute(IStatement statement) throws ExecutionError {
		try {
			IExecutable executable = (IExecutable) statement;
			for(IListener l : listeners)
				l.statementExecutionStart(statement);

			boolean result = executable.execute(this.getCallStack(), values);
			System.out.println("S " + statement + " " + result);
			
			historyStatements.add(new StatementTimestamp(statement));
			
			for(IListener l : listeners)
				l.statementExecutionEnd(statement);

			return result;
		}
		catch(ExecutionError e) {
			throw e;
		}
	}




	@Override
	public String toString() {
		String text = procedure.getId() + "(...)"; // TODO pretty print
		for(Entry<String, VariableHistory> e : variables.entrySet())
			text += " " + e.getKey() + "=" + e.getValue().getCurrent();
		return text;
	}
	
	
	private class VariableHistory {
		final List<ValueTimestamp> history;
		int current;
		VariableHistory(IValue initialValue) {
			history = new ArrayList<>();
			history.add(new ValueTimestamp(initialValue));
			current = 0;
		}
		
		public void newValue(IValue value) {
			history.add(new ValueTimestamp(value));
			current++;
		}

		public List<IValue> getHistory() {
			return history.stream().map(v -> v.value).collect(Collectors.toList());
		}

		IValue getCurrent() {
			return history.get(current).value;
		}
		
		void setTime(int time) {
			for(int i = 0; i < history.size()-1; i++) {
				if(time > history.get(i+1).timestamp) {
					current = i;
					return;
				}
			}
			current = history.size()-1;
		}
	}
	
	private class ValueTimestamp {
		final IValue value;
		final int timestamp;
		
		ValueTimestamp(IValue value) {
			this.value = value;
			this.timestamp = time;
		}
	}
	
	private class StatementTimestamp {
		final IStatement statement;
		final int timestamp;
		
		StatementTimestamp(IStatement statement) {
			this.statement = statement;
			this.timestamp = time++;
		}
	}
}
