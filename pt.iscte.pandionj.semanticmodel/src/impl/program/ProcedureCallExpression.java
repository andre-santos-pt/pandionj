package impl.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

import impl.machine.ExecutionError;
import impl.machine.ExecutionError.Type;
import model.machine.ICallStack;
import model.machine.IValue;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IProcedure;
import model.program.IProcedureCallExpression;
import model.program.IProcedureDeclaration;

public class ProcedureCallExpression extends Expression implements IProcedureCallExpression {

	private final IProcedureDeclaration procedure;
	private final ImmutableList<IExpression> args;
	
	public ProcedureCallExpression(IProcedureDeclaration procedure, List<IExpression> args) {
		this.procedure = procedure;
		this.args = ImmutableList.copyOf(args);
	}
	
	@Override
	public IDataType getType() {
		return procedure.getReturnType();
	}

	@Override
	public IProcedureDeclaration getProcedure() {
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
	public IValue evalutate(List<IValue> values, ICallStack stack) throws ExecutionError {
		// excepcional case when null: for pending value from created stack
		// if builtin procedure returns value
		return ProcedureCall.executeInternal(stack, getProcedure(), values); 
	}
}
