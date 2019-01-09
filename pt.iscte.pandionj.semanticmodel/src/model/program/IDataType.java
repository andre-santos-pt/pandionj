package model.program;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

public interface IDataType extends IIdentifiableElement {
	boolean matches(Object object);
	Object match(String literal);
	default boolean isNumeric() {
		return this.equals(INT) || this.equals(DOUBLE);
	}
	
	default boolean isBoolean() {
		return this.equals(BOOLEAN);
	}
	
	IDataType VOID = new IDataType() {
		
		@Override
		public String getIdentifier() {
			return "void";
		}
		
		@Override
		public boolean matches(Object object) {
			return false;
		}
		
		@Override
		public Object match(String literal) {
			return null;
		}
	};
	
	IDataType INT = new ValueType("int", Integer.class);
	IDataType DOUBLE = new ValueType("double", Double.class);
	IDataType BOOLEAN = new ValueType("boolean", Boolean.class);
//	IDataType CHAR = new ValueType("char", Character.class);
			
	
	class ValueType implements IDataType {
		final String id;
		final Class<?> valueType;
		
		public ValueType(String id, Class<?> valueType) {
			this.id = id;
			this.valueType = valueType;
		}

		@Override
		public String getIdentifier() {
			return id;
		}

		@Override
		public boolean matches(Object object) {
			return valueType.isInstance(object);
		}
		
		@Override
		public Object match(String literal) {
			try {
				Object obj = valueType.getConstructor(String.class).newInstance(literal);
				return obj;
			}
			catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		public String toString() {
			return id;
		}
	}

	ImmutableCollection<IDataType> DEFAULTS = ImmutableList.of(VOID, INT, DOUBLE, BOOLEAN);
}