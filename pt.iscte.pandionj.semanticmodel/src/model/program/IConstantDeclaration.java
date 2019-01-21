package model.program;

public interface IConstantDeclaration extends IIdentifiableElement {

	IModule getProgram();
	
	IDataType getType();
	
	ILiteral getValue();
	
	IConstantExpression expression();
}
