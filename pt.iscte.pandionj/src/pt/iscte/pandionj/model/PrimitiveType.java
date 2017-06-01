package pt.iscte.pandionj.model;

import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;

public enum PrimitiveType {
	BYTE {
		@Override
		public void fillPrimitiveWrapperValues(IJavaValue[] elements, Object[] array) {
			for(int i = 0; i < elements.length; i++)
				array[i] = ((IJavaPrimitiveValue) elements[i]).getByteValue();
		}
		
		@Override
		public Object getValue(IJavaValue val) {
			return new Byte(((IJavaPrimitiveValue) val).getByteValue());
		}
	},
	SHORT {
		@Override
		public void fillPrimitiveWrapperValues(IJavaValue[] elements, Object[] array) {
			for(int i = 0; i < elements.length; i++)
				array[i] = ((IJavaPrimitiveValue) elements[i]).getShortValue();
		}
		
		@Override
		public Object getValue(IJavaValue val) {
			return new Short(((IJavaPrimitiveValue) val).getShortValue());
		}
	},
	INT {
		@Override
		public void fillPrimitiveWrapperValues(IJavaValue[] elements, Object[] array) {
			for(int i = 0; i < elements.length; i++)
				array[i] = ((IJavaPrimitiveValue) elements[i]).getIntValue();
		}

		@Override
		public Object getValue(IJavaValue val) {
			return new Integer(((IJavaPrimitiveValue) val).getIntValue());
		}
	},
	LONG {
		@Override
		public void fillPrimitiveWrapperValues(IJavaValue[] elements, Object[] array) {
			for(int i = 0; i < elements.length; i++)
				array[i] = ((IJavaPrimitiveValue) elements[i]).getLongValue();
		}
		
		@Override
		public Object getValue(IJavaValue val) {
			return new Long(((IJavaPrimitiveValue) val).getLongValue());
		}
	},
	FLOAT {
		@Override
		public void fillPrimitiveWrapperValues(IJavaValue[] elements, Object[] array) {
			for(int i = 0; i < elements.length; i++)
				array[i] = ((IJavaPrimitiveValue) elements[i]).getFloatValue();
		}
		@Override
		public Object getValue(IJavaValue val) {
			return new Float(((IJavaPrimitiveValue) val).getFloatValue());
		}
	},
	DOUBLE {
		@Override
		public void fillPrimitiveWrapperValues(IJavaValue[] elements, Object[] array) {
			for(int i = 0; i < elements.length; i++)
				array[i] = ((IJavaPrimitiveValue) elements[i]).getDoubleValue();
		}
		
		@Override
		public Object getValue(IJavaValue val) {
			return new Double(((IJavaPrimitiveValue) val).getDoubleValue());
		}
	},
	CHAR {
		@Override
		public void fillPrimitiveWrapperValues(IJavaValue[] elements, Object[] array) {
			for(int i = 0; i < elements.length; i++)
				array[i] = ((IJavaPrimitiveValue) elements[i]).getCharValue();
		}
		
		@Override
		public Object getValue(IJavaValue val) {
			return new Character(((IJavaPrimitiveValue) val).getCharValue());
		}
	},
	BOOLEAN {
		@Override
		public void fillPrimitiveWrapperValues(IJavaValue[] elements, Object[] array) {
			for(int i = 0; i < elements.length; i++)
				array[i] = ((IJavaPrimitiveValue) elements[i]).getBooleanValue();
		}
		@Override
		public Object getValue(IJavaValue val) {
			return new Boolean(((IJavaPrimitiveValue) val).getBooleanValue());
		}
	};
 	
	private final String type;
	
	private PrimitiveType() {
		this.type = name().toLowerCase();
	}
	
	public abstract void fillPrimitiveWrapperValues(IJavaValue[] elements, Object[] array);
	
	public abstract Object getValue(IJavaValue val);
	
//	public abstract String getStringValue(IJavaValue val);
	
//	public static PrimitiveType match(IJavaType type) {
//		try {
//			return match(type);
//		} catch (DebugException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
//	public static PrimitiveType match(String type) {
//		return match(type);
//	}
			
	public static PrimitiveType match(String type) {
		for(PrimitiveType t : values())
			if(t.type.equals(type))
				return t;
		
		return null;
	}
	
	public static boolean isPrimitive(String type) {
		for(PrimitiveType t : values())
			if(t.type.equals(type))
				return true;
		
		return false;
	}

//	static Object[] getPrimitiveWrapperValues(IJavaValue[] elements, String type) {
//	Object[] array = new Object[elements.length];
//	switch(type) {
//	case "byte":
//		for(int i = 0; i < elements.length; i++)
//			array[i] = ((IJavaPrimitiveValue) elements[i]).getByteValue();
//		break;
//	
//	case "short":
//		for(int i = 0; i < elements.length; i++)
//			array[i] = ((IJavaPrimitiveValue) elements[i]).getShortValue();
//		break;
//	
//	case "int":
//		for(int i = 0; i < elements.length; i++)
//			array[i] = ((IJavaPrimitiveValue) elements[i]).getIntValue();
//		break;
//	
//	case "long":
//		for(int i = 0; i < elements.length; i++)
//			array[i] = ((IJavaPrimitiveValue) elements[i]).getLongValue();
//		break;
//		
//	case "double":
//		for(int i = 0; i < elements.length; i++)
//			array[i] = ((IJavaPrimitiveValue) elements[i]).getDoubleValue();
//		break;
//	
//	case "float":
//		for(int i = 0; i < elements.length; i++)
//			array[i] = ((IJavaPrimitiveValue) elements[i]).getFloatValue();
//		break;
//	
//	case "char":
//		for(int i = 0; i < elements.length; i++)
//			array[i] = ((IJavaPrimitiveValue) elements[i]).getCharValue();
//		break;
//	
//	case "boolean":
//		for(int i = 0; i < elements.length; i++)
//			array[i] = ((IJavaPrimitiveValue) elements[i]).getBooleanValue();
//		break;	
//		
//	default: throw new AssertionError();
//	}
//	return array;
//}


//private Class<?> matchType(IJavaType componentType) {
//	try {
//		switch(componentType.getName())  {
//		case "byte": return Byte.class;
//		case "short": return Short.class;
//		case "int": return Integer.class;
//		case "long": return Long.class;
//		case "float": return Float.class;
//		case "double": return Double.class;
//		case "boolean": return Boolean.class;
//		case "char": return Character.class;
//		default: throw new AssertionError();
//		}
//	} catch (DebugException e) {
//		e.printStackTrace();
//	}
//
//	return null;
//}
}
