package model.program;

public interface IArrayType extends IDataType {

	int getDimensions();
	
	class ValueTypeArray implements IArrayType {
		private IDataType.ValueType type;
		private int dimensions;
		private String id;
		
		public ValueTypeArray(IDataType type, int dimensions) {
			assert type instanceof IDataType.ValueType;
			this.dimensions = dimensions;
			id = type.getIdentifier();
			for(int i = 0; i < dimensions; i++)
				id += "[]";
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
		public Object match(String literal) {
			return type.match(literal);
		}

		@Override
		public String getIdentifier() {
			return id;
		}
		
		public boolean sameAs(IArrayType type) {
			return id.equals(type.getIdentifier());
		}
	}
}
