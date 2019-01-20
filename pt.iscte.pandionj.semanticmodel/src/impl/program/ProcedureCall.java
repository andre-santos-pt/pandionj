package impl.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

import impl.machine.ExecutionError;
import model.machine.ICallStack;
import model.machine.IStackFrame;
import model.machine.IValue;
import model.program.IExpression;
import model.program.IProcedure;
import model.program.IProcedureCall;

class ProcedureCall extends Statement implements IProcedureCall {
	private final IProcedure procedure;
	private final ImmutableList<IExpression> arguments;
	
	public ProcedureCall(Block parent, IProcedure procedure, List<IExpression> arguments) {
		super(parent, true);
		this.procedure = procedure;
		this.arguments = ImmutableList.copyOf(arguments);
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
		return procedure.getId() + "(" + argsToString() + ")";
	}
	
	private String argsToString() {
		String args = "";
		for(IExpression e : arguments) {
			if(!args.isEmpty())
				args += ", ";
			args += e.toString();
		}
		return args;
	}
	
	@Override
	public List<IExpression> getExpressionParts() {
		return arguments;
	}
	
	@Override
	public boolean execute(ICallStack stack, List<IValue> expressions) throws ExecutionError {
		IStackFrame newFrame = stack.newFrame(getProcedure(), expressions);
		return true;
	}
	
//	@Override
//	public IDataType getType() {
//		return procedure.getReturnType();
//	}
}
