package impl.machine;

import java.util.List;

import com.google.common.collect.ImmutableList;

import model.machine.ICallStack;
import model.machine.IEvaluable;
import model.machine.IValue;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IProcedure;

class ProcedureReturnExpression implements IExpression, IEvaluable {
	final IProcedure procedure;
	ProcedureReturnExpression(IProcedure procedure) {
		this.procedure = procedure;
	}

	@Override
	public List<IExpression> decompose() {
		return ImmutableList.of();
	}

	@Override
	public boolean isDecomposable() {
		return false;
	}

	@Override
	public IDataType getType() {
		return procedure.getReturnType();
	}

	@Override
	public IValue evalutate(List<IValue> values, ICallStack stack) throws ExecutionError {
		return stack.getLastTerminatedFrame().getReturn();
	}
	@Override
	public Object getProperty(String key) {
		return null;
	}
	@Override
	public void setProperty(String key, Object value) {
		
	}
}