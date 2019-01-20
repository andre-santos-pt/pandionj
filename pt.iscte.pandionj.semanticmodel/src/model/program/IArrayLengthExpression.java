package model.program;

import java.util.List;

public interface IArrayLengthExpression extends IExpression {
	IArrayVariableDeclaration getVariable();
	List<IExpression> getIndexes(); // size() >= 1
}
