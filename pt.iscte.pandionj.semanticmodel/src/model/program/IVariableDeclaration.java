package model.program;

import model.machine.ICallStack;
import model.program.roles.IGatherer;
import model.program.roles.IVariableRole;

public interface IVariableDeclaration extends ISourceElement, IIdentifiableElement {
	ISourceElement getParent();
	
	IDataType getType();

	boolean isReference();
	boolean isConstant();
	boolean isParameter();
	
//	@Override
//	default boolean isControl() {
//		return false;
//	}
	
	default IVariableRole getRole() {
		return IVariableRole.NONE;
	}
	
	default boolean isGatherer() {
		return getRole() instanceof IGatherer;
	}
	
	IVariableAssignment addAssignment(IExpression exp);
	
	IVariableExpression expression();
	
	IStructMemberAssignment addMemberAssignment(String memberId, IExpression expression);
	
	IStructMemberExpression memberExpression(String memberId);

//	@Override
//	default boolean execute(ICallStack callStack) {
//		return true;
//	}
	
	enum Flag {
		REFERENCE, CONSTANT, PARAM, FIELD
	}
}
