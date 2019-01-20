package model.program;

import java.util.List;

public interface IArrayAllocation extends IExpression {
	List<IExpression> getDimensions();
}
