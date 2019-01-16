package model.program.impl;

import model.machine.ICallStack;
import model.machine.IValue;
import model.program.ExecutionError;
import model.program.IDataType;

public class PrintProcedure extends Procedure {

	public PrintProcedure() {
		super("print", IDataType.VOID);
		addParameter("value", IDataType.INT);
	}
	
	@Override
	public boolean execute(ICallStack stack) throws ExecutionError {
		IValue value = stack.getTopFrame().getVariable("value");
		System.out.println(value.getValue());
		return true;
	}

	@Override
	public boolean isBuiltIn() {
		return true;
	}
}
