package impl.program;

import model.program.IArrayType;
import model.program.IDataType;

class ValueTypeArray implements IArrayType {
	private final IDataType type;
	private final int dimensions;
	private final String id;

	public ValueTypeArray(IDataType type, int dimensions) {
		this.type = type;
		this.dimensions = dimensions;
		String id = type.getId();
		for(int i = 0; i < dimensions; i++)
			id += "[]";
		this.id = id;
	}

	@Override
	public int getDimensions() {
		return dimensions;
	}

	@Override
	public boolean matches(Object object) {
		return type.matches(object);
	}

	@Override
	public boolean matchesLiteral(String literal) {
		return type.matchesLiteral(literal);
	}

	@Override
	public String getId() {
		return id;
	}

	public boolean sameAs(IArrayType type) {
		return id.equals(type.getId());
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public Object create(String literal) {
		return type.create(literal);
	}

	@Override
	public Object getDefaultValue() {
		return null;
	}

	@Override
	public IDataType getComponentType() {
		return type;
	}

	@Override
	public int getMemoryBytes() {
		return 4;
	}
}