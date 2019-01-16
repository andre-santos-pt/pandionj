package impl.program;

import model.program.IConstantDeclaration;
import model.program.IConstantExpression;
import model.program.IDataType;

class ConstantExpression extends SourceElement implements IConstantExpression {

	private final IConstantDeclaration constant;

	public ConstantExpression(IConstantDeclaration constant) {
		this.constant = constant;
	}
	
	@Override
	public IDataType getType() {
		return constant.getType();
	}

	@Override
	public IConstantDeclaration getConstant() {
		return constant;
	}

	@Override
	public String toString() {
		return constant.getId();
	}
}
