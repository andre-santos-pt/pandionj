package model.program;

import java.util.List;


public interface IProcedureCall extends IStatement {
	IProcedureDeclaration getProcedure();
	List<IExpression> getArguments();

	default boolean isOperation() {
		return false;
	}
}
