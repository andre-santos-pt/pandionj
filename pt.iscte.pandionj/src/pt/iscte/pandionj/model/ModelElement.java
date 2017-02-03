package pt.iscte.pandionj.model;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaValue;

public interface ModelElement {
	void update();
	IJavaValue getContent();
	IFigure createFigure();
	
	default boolean isReference() {
		return this instanceof ReferenceModel;
	}
	
	default ReferenceModel asReference() {
		assert isReference();
		return (ReferenceModel) this;
	}
	
	default boolean isValue() {
		return this instanceof ValueModel;
	}
	
	default ValueModel asValue() {
		assert isValue();
		return (ValueModel) this;
	}
	
//	default boolean isArray() {
//		return this instanceof ArrayModel; 
//	}
//	
//	default ArrayModel asArray() {
//		assert isArray();
//		return (ArrayModel) this;
//	}
//	
//	

//	
//	default boolean isNull() {
//		return this instanceof NullModel;
//	}
	
	
}
