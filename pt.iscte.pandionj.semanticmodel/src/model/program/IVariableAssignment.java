package model.program;

public interface IVariableAssignment extends IStatement {
	IVariableDeclaration getVariable();
	IExpression getExpression();
	
	/*
	boolean isAccumulation();
	*/
}
