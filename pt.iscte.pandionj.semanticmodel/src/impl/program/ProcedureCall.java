package impl.program;

import java.util.List;

import model.program.IExpression;
import model.program.IProcedure;
import model.program.IProcedureCall;

class ProcedureCall extends Statement implements IProcedureCall {
	private final IProcedure procedure;
	private final List<IExpression> arguments;
	
	public ProcedureCall(Block parent, IProcedure procedure, List<IExpression> arguments) {
		super(parent, true);
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
	
//	@Override
//	public IDataType getType() {
//		return procedure.getReturnType();
//	}
}
