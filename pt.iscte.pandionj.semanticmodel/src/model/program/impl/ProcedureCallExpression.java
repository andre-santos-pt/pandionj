package model.program.impl;

import java.util.List;

import com.google.common.collect.ImmutableList;

import model.program.IDataType;
import model.program.IExpression;
import model.program.IProcedure;
import model.program.IProcedureCallExpression;

public class ProcedureCallExpression extends SourceElement implements IProcedureCallExpression {

	private final IProcedure procedure;
	private final ImmutableList<IExpression> args;
	
	public ProcedureCallExpression(IProcedure procedure, List<IExpression> args) {
		this.procedure = procedure;
		this.args = ImmutableList.copyOf(args);
	}
	
	@Override
	public IDataType getType() {
		return procedure.getReturnType();
	}

	@Override
	public IProcedure getProcedure() {
		return procedure;
	}

	@Override
	public List<IExpression> getArgs() {
		return args;
	}

	@Override
	public String toString() {
		String text = procedure.getId() + "(";
		for(IExpression a : args) {
			if(!text.endsWith("("))
				text += ", ";
			text += a;
		}
		return text + ")";
	}
}
