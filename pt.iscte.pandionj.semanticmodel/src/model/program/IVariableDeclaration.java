package model.program;

import model.machine.ICallStack;

public interface IVariableDeclaration extends IStatement, IIdentifiableElement {
	IProcedure getProcedure();
	
	IDataType getType();

	boolean isReference();
	boolean isConstant();
	boolean isParameter();
	
	default IVariableRole getRole() {
		return IVariableRole.NONE;
	}
	
	IVariableAssignment assignment(IExpression exp);
	
	IVariableExpression expression();
	
	@Override
	default void execute(ICallStack callStack) {
		callStack.getTopFrame().addVariable(getIdentifier(), getType());
	}
	
	enum Flag {
		REFERENCE, CONSTANT, PARAM 
	}
}
