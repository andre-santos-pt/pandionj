package model.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

public interface IFactory {

	IProgram createProgram(ImmutableList<IProcedure> procedures, IProcedure main, ImmutableList<IConstantDeclaration> constants);
	
	IProcedure createProcedure(String name, ImmutableList<IVariableDeclaration> parameters);
	IProcedure createProcedure(String name, ImmutableList<IVariableDeclaration> parameters, IStatement ... statements);
	
	IVariableDeclaration createVariableDeclaration(IProcedure parent, String name, IDataType type);

	IVariableExpression createVariableExpression(IVariableDeclaration var);
	
	ILiteral createLiteral(String string);

	IBlock createBlock(IStatement ... statement);
	IBlock createBlock(List<IStatement> statements);

	ISelection createSelection(IExpression expression, IBlock block);
	ISelection createSelection(IExpression expression, IStatement statement);

	IVariableAssignment createAssignment(IVariableDeclaration var, IExpression exp);

	IBinaryExpression createBinaryExpression(Operator operator, IExpression left, IExpression right);

	
	
}
