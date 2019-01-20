package model.program;

import java.util.List;

public interface IProcedureCallExpression extends IExpression {
	IProcedure getProcedure();
	List<IExpression> getArguments();
}
