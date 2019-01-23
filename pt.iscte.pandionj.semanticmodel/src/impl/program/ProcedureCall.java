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
import model.program.IProcedureDeclaration;

class ProcedureCall extends Statement implements IProcedureCall {
	private final IProcedureDeclaration procedure;
	private final ImmutableList<IExpression> arguments;
	
	public ProcedureCall(Block parent, IProcedure procedure, List<IExpression> arguments) {
		super(parent, true);
		assert procedure != null;
		this.procedure = procedure;
		this.arguments = ImmutableList.copyOf(arguments);
	}

	@Override
	public IProcedureDeclaration getProcedure() {
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
		executeInternal(stack, getProcedure(), expressions);
		return true;
	}
	
	static IValue executeInternal(ICallStack stack, IProcedureDeclaration procedure, List<IValue> expressions) throws ExecutionError {
		IProcedure p = stack.getProgramState().getProgram().resolveProcedure(procedure);
		if(p instanceof BuiltinProcedure)
			return ((BuiltinProcedure) p).hookAction(expressions);
		else {
			stack.newFrame(p, expressions);
			return null;
		}
	}
}
