package model.program;

public interface IArrayType extends IDataType {

	int getDimensions();
	
	class ValueTypeArray implements IArrayType {
		private IDataType type;
		private int dimensions;
		private String id;
		
		public ValueTypeArray(IDataType type, int dimensions) {
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
		public boolean matchesLiteral(String literal) {
			return type.matchesLiteral(literal);
		}

		@Override
		public String getIdentifier() {
			return id;
		}
		
		public boolean sameAs(IArrayType type) {
			return id.equals(type.getIdentifier());
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
	}
}
