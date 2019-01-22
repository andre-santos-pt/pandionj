package model.program;

public interface IVariableDeclaration extends IIdentifiableElement {
	IProgramElement getParent();
	IDataType getType();

	default boolean isStructField() {
		return getParent() instanceof IStructType;
	}
	
	default boolean isLocalVariable() {
		return getParent() instanceof IBlock;
	}
	
	boolean isReference();
	boolean isConstant();
	boolean isParameter();
	
	IVariableExpression expression();
	IStructMemberExpression memberExpression(String memberId);

	IVariableAssignment addAssignment(IExpression exp);
	
	IStructMemberAssignment addMemberAssignment(String memberId, IExpression expression);
	

	enum Flag {
		REFERENCE, CONSTANT, PARAM, FIELD
	}
}
