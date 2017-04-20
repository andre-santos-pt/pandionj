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
	
	public static PrimitiveType match(IJavaType type) {
		try {
			return match(type.getName());
		} catch (DebugException e) {
			e.printStackTrace();
			return null;
		}
	}
			
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
	
}
