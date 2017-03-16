package pt.iscte.pandionj.model;

import java.util.Observer;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.zest.core.widgets.Graph;

public interface ModelElement {
	void update();
	IJavaValue getContent();
	IFigure createFigure(Graph graph);
	void registerObserver(Observer o);
	
	

	
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
