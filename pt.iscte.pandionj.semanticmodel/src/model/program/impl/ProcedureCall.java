package model.program.impl;

import java.util.List;

import model.program.IExpression;
import model.program.IProcedure;
import model.program.IProcedureCall;

public class ProcedureCall extends SourceElement implements IProcedureCall {
	private final IProcedure procedure;
	private final List<IExpression> arguments;
	
	public ProcedureCall(IProcedure procedure, List<IExpression> arguments) {
		this.procedure = procedure;
		this.arguments = arguments;
	}

	@Override
	public IProcedure getProcedure() {
		return procedure;
	}

	@Override
	public List<IExpression> getArguments() {
		return arguments;
	}

	@Override
	public String toString() {
		return procedure.getIdentifier() + "(...)";
	}
}
