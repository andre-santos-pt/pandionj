package model.program;

import model.machine.ICallStack;
import model.machine.IValue;

public interface IStructMemberExpression extends IExpression {

	IStructType getStruct();
	String getMemberId();
	
	@Override
	default IValue evaluate(ICallStack frame) throws ExecutionError {
		// TODO Auto-generated method stub
		return null;
	}
	
}
