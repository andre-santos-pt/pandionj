package model.machine;

import java.util.List;

import impl.machine.ExecutionError;

public interface IEvaluable {

	IValue evalutate(List<IValue> values, ICallStack stack) throws ExecutionError;
}
