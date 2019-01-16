package impl.program;

import model.program.IDataType;
import model.program.ILiteral;

class Literal extends SourceElement implements ILiteral {

	private final IDataType type;
	private final String value;
	
	public Literal(IDataType type, String value) {
		assert type != null;
		assert value != null && !value.isEmpty();
		this.type = type;
		this.value = value;
	}
	
	@Override
	public IDataType getType() {
		return type;
	}
	
	@Override
	public String getStringValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return value;
	}
}
