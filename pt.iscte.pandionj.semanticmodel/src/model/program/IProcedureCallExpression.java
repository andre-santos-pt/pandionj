package model.program;

import java.util.List;

public interface IProcedureCallExpression extends IExpression {
	IProcedureDeclaration getProcedure();
	List<IExpression> getArguments();
}
