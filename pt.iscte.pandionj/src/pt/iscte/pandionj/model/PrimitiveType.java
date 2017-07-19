package pt.iscte.pandionj.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
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

		@Override
		public Object getValue(String val) {
			return Byte.valueOf(val);
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
		
		@Override
		public Object getValue(String val) {
			return Short.valueOf(val);
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
		
		@Override
		public Object getValue(String val) {
			return Integer.valueOf(val);
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
		
		@Override
		public Object getValue(String val) {
			return Long.valueOf(val);
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
		@Override
		public Object getValue(String val) {
			return Float.valueOf(val);
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
		
		@Override
		public Object getValue(String val) {
			return Double.valueOf(val);
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
		
		@Override
		public Object getValue(String val) {
			return new Character(val.charAt(0));
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
		
		@Override
		public Object getValue(String val) {
			return Boolean.valueOf(val);
		}
	};
 	
	private final String type;
	
	private PrimitiveType() {
		this.type = name().toLowerCase();
	}
	
	public abstract void fillPrimitiveWrapperValues(IJavaValue[] elements, Object[] array);
	
	public abstract Object getValue(IJavaValue val);
	
	public abstract Object getValue(String val);
	
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

	
	public static IJavaValue[] createValues(IMethod m, String[] values, IJavaDebugTarget debugger) throws DebugException {
		assert values.length == m.getNumberOfParameters();
		IJavaValue[] v = new IJavaValue[values.length];
		for(int i = 0; i < v.length; i++) {
			String pType = Signature.toString(m.getParameterTypes()[i]);
			if(pType.equals(char.class.getName()))			v[i] = debugger.newValue(values[i].charAt(0));
			else if(pType.equals(boolean.class.getName())) 	v[i] = debugger.newValue(Boolean.parseBoolean(values[i]));
			else if(pType.equals(byte.class.getName())) 		v[i] = debugger.newValue(Byte.parseByte(values[i]));
			else if(pType.equals(short.class.getName()))		v[i] = debugger.newValue(Short.parseShort(values[i]));
			else if(pType.equals(int.class.getName())) 		v[i] = debugger.newValue(Integer.parseInt(values[i]));
			else if(pType.equals(long.class.getName())) 		v[i] = debugger.newValue(Long.parseLong(values[i]));
			else if(pType.equals(float.class.getName())) 	v[i] = debugger.newValue(Float.parseFloat(values[i]));
			else if(pType.equals(double.class.getName())) 	v[i] = debugger.newValue(Double.parseDouble(values[i]));

			else if(pType.equals(String.class.getName()) && values[i].matches("\"(.)*\"")) 	
				v[i] = debugger.newValue(values[i].substring(1, values[i].length()-1));

			else if(values[i].equals("null"))
				v[i] = debugger.nullValue();
			else {
				IJavaValue val = (IJavaValue) debugger.findVariable(values[i]);
				v[i] = val == null ? debugger.nullValue() : val;
			}
		}

		return v;

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
