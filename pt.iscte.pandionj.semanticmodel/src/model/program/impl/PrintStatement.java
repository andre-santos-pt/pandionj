package model.program.impl;

import model.machine.ICallStack;
import model.machine.IValue;
import model.program.ExecutionError;
import model.program.IBlock;
import model.program.IExpression;
import model.program.IStatement;

public class PrintStatement extends Statement implements IStatement {

	private final IExpression expression;
	
	public PrintStatement(IBlock parent, IExpression expression) {
		super(parent);
		this.expression = expression;
	}

	@Override
	public void execute(ICallStack callStack) throws ExecutionError {
		IValue value = callStack.evaluate(expression);
		callStack.getTopFrame().setReturn(value);
	}

	@Override
	public String toString() {
		return "print " + expression;
	}
}
