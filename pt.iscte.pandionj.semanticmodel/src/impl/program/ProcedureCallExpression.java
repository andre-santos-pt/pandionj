package impl.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

import model.machine.ICallStack;
import model.machine.IValue;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IProcedure;
import model.program.IProcedureCallExpression;

public class ProcedureCallExpression extends Expression implements IProcedureCallExpression {

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
	public List<IExpression> getArguments() {
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
	
	@Override
	public List<IExpression> decompose() {
		return args;
	}
	
	@Override
	public boolean isDecomposable() {
		return true;
	}	
	
	@Override
	public IValue evalutate(List<IValue> values, ICallStack stack) {
		stack.newFrame(getProcedure(), values);
		return null; // excepcional case, for pending value from created stack
	}
}
