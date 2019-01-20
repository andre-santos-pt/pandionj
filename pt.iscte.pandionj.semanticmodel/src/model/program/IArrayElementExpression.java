package model.program;

import java.util.List;

public interface IArrayElementExpression extends IVariableExpression {

	IArrayVariableDeclaration getVariable();
	
	List<IExpression> getIndexes(); // size() >= 1
	
	default IDataType getType() {
		return getVariable().getType();
	}
	
	@Override
	default boolean isDecomposable() {
		return true;
	}
	
	
}
