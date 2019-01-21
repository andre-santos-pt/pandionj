package model.program;

public interface IVariableDeclaration extends IIdentifiableElement {
	IProgramElement getParent();
	
	IDataType getType();

	boolean isReference();
	boolean isConstant();
	boolean isParameter();
	
	IVariableAssignment addAssignment(IExpression exp);
	
	IVariableExpression expression();
	
	IStructMemberAssignment addMemberAssignment(String memberId, IExpression expression);
	
	IStructMemberExpression memberExpression(String memberId);

	enum Flag {
		REFERENCE, CONSTANT, PARAM, FIELD
	}
}
