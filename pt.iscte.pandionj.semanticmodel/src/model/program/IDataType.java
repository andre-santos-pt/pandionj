package model.program;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

public interface IDataType extends IIdentifiableElement {
	boolean matches(Object object);

	boolean matchesLiteral(String literal);
	
	// pre: matchesLiteral(literal)
	Object create(String literal);
	
	Object getDefaultValue();
	
	int getMemoryBytes();
	
	default boolean isVoid() {
		return this == VOID;
	}
	
	default boolean isNumeric() {
		return this.equals(INT) || this.equals(DOUBLE);
	}

	default boolean isBoolean() {
		return this.equals(BOOLEAN);
	}

	IDataType INT = DefaultValueType.INT;
	IDataType DOUBLE = DefaultValueType.DOUBLE;
	IDataType BOOLEAN = DefaultValueType.BOOLEAN;
	// TODO IDataType CHAR
	
	ImmutableCollection<IDataType> VALUE_TYPES = ImmutableList.of(INT, DOUBLE, BOOLEAN);

	enum DefaultValueType implements IDataType {
		INT {
			public boolean matchesLiteral(String literal) {
				try {
					Integer.parseInt(literal);
					return true;
				}
				catch(NumberFormatException e) {
					return false;
				}
			}
			
			public Object create(String literal) {
				return new BigDecimal(literal);
			}
			
			@Override
			public Object getDefaultValue() {
				return new BigDecimal(0);
			}
			
			public boolean matches(Object object) {
				return Integer.class.isInstance(object) || object instanceof BigDecimal && isWhole((BigDecimal)  object);
			}
			
			private boolean isWhole(BigDecimal bigDecimal) {
			    return bigDecimal.setScale(0, RoundingMode.HALF_UP).compareTo(bigDecimal) == 0;
			}
			
			@Override
			public int getMemoryBytes() {
				return 4;
			}
		}, 
		DOUBLE {
			public boolean matchesLiteral(String literal) {
				try {
					Double.parseDouble(literal);
					return true;
				}
				catch(NumberFormatException e) {
					return false;
				}
			}
			
			public Object create(String literal) {
				return new BigDecimal(literal);
			}
			
			@Override
			public Object getDefaultValue() {
				return new BigDecimal("0.0");
			}
			
			public boolean matches(Object object) {
				return Double.class.isInstance(object);
			}
			
			@Override
			public int getMemoryBytes() {
				return 8;
			}
		}, 
		BOOLEAN {
			public boolean matchesLiteral(String literal) {
				return literal.matches("true|false");
			}
			
			public Object create(String literal) {
				return literal.equals("true") ? Boolean.TRUE : Boolean.FALSE;
			}
			
			public boolean matches(Object object) {
				return Boolean.class.isInstance(object);
			}
			
			@Override
			public Object getDefaultValue() {
				return Boolean.FALSE;
			}
			
			@Override
			public int getMemoryBytes() {
				return 1;
			}
		};
		
		@Override
		public String toString() {
			return name().toLowerCase();
		}

		@Override
		public String getId() {
			return name().toLowerCase();
		}

		@Override
		public abstract boolean matches(Object object);

		@Override
		public abstract boolean matchesLiteral(String literal);

		@Override
		public abstract Object create(String literal);
		
		@Override
		public Object getProperty(String key) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void setProperty(String key, Object value) {
			throw new UnsupportedOperationException();
		}
	}

	IDataType VOID = new IDataType() {

		@Override
		public String getId() {
			return "void";
		}

		@Override
		public void setProperty(String key, Object value) {
			
		}

		@Override
		public Object getProperty(String key) {
			return null;
		}

		@Override
		public boolean matches(Object object) {
			return false;
		}

		@Override
		public boolean matchesLiteral(String literal) {
			return false;
		}

		@Override
		public Object create(String literal) {
			return null;
		}

		@Override
		public Object getDefaultValue() {
			return null;
		}

		@Override
		public int getMemoryBytes() {
			return 0;
		}
	};
	
	
	IDataType UNKNOWN = new IDataType() {
		@Override
		public String getId() {
			return "unknown";
		}
		@Override
		public boolean matches(Object object) {
			return false;
		}
		@Override
		public boolean matchesLiteral(String literal) {
			return false;
		}
		@Override
		public Object create(String literal) {
			return null;
		}
		
		@Override
		public Object getDefaultValue() {
			return null;
		}
		
		@Override
		public String toString() {
			return getId();
		}
		
		@Override
		public int getMemoryBytes() {
			return 0;
		}
		@Override
		public void setProperty(String key, Object value) {
			
		}
		@Override
		public Object getProperty(String key) {
			return null;
		}
	};
}