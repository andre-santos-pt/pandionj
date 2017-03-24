package pt.iscte.pandionj.model;

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
	},
	SHORT {
		@Override
		public void fillPrimitiveWrapperValues(IJavaValue[] elements, Object[] array) {
			for(int i = 0; i < elements.length; i++)
				array[i] = ((IJavaPrimitiveValue) elements[i]).getShortValue();
		}
	},
	INT {
		@Override
		public void fillPrimitiveWrapperValues(IJavaValue[] elements, Object[] array) {
			for(int i = 0; i < elements.length; i++)
				array[i] = ((IJavaPrimitiveValue) elements[i]).getIntValue();
		}
	},
	LONG {
		@Override
		public void fillPrimitiveWrapperValues(IJavaValue[] elements, Object[] array) {
			for(int i = 0; i < elements.length; i++)
				array[i] = ((IJavaPrimitiveValue) elements[i]).getLongValue();
		}
	},
	FLOAT {
		@Override
		public void fillPrimitiveWrapperValues(IJavaValue[] elements, Object[] array) {
			for(int i = 0; i < elements.length; i++)
				array[i] = ((IJavaPrimitiveValue) elements[i]).getFloatValue();
		}
	},
	DOUBLE {
		@Override
		public void fillPrimitiveWrapperValues(IJavaValue[] elements, Object[] array) {
			for(int i = 0; i < elements.length; i++)
				array[i] = ((IJavaPrimitiveValue) elements[i]).getDoubleValue();
		}
	},
	CHAR {
		@Override
		public void fillPrimitiveWrapperValues(IJavaValue[] elements, Object[] array) {
			for(int i = 0; i < elements.length; i++)
				array[i] = ((IJavaPrimitiveValue) elements[i]).getCharValue();
		}
	},
	BOOLEAN {
		@Override
		public void fillPrimitiveWrapperValues(IJavaValue[] elements, Object[] array) {
			for(int i = 0; i < elements.length; i++)
				array[i] = ((IJavaPrimitiveValue) elements[i]).getBooleanValue();
		}
	};
 	
	private final String type;
	
	private PrimitiveType() {
		this.type = name().toLowerCase();
	}
	
	public abstract void fillPrimitiveWrapperValues(IJavaValue[] elements, Object[] array);
	
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
