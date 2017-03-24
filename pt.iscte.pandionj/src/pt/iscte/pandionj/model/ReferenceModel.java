package pt.iscte.pandionj.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.zest.core.widgets.Graph;

import pt.iscte.pandionj.figures.ReferenceFigure;

public class ReferenceModel extends VariableModel<IJavaObject> {

	private final boolean isInstance;
	
	private NullModel nullModel;
	
	ReferenceModel(IJavaVariable var, boolean isInstance, StackFrameModel model) throws DebugException {
		super(var, model);
		assert var.getValue() instanceof IJavaObject;
		this.isInstance = isInstance;
	}

	public boolean isInstance() {
		return isInstance;
	}
	
	public EntityModel<?> getModelTarget() {
		IJavaObject target = getContent();
		return target.isNull() ? getNullInstance() : getStackFrame().getObject(target, true);
	}

	public boolean isNull() {
		return getContent().isNull();
	}
	
	public NullModel getNullInstance() {
		if(nullModel == null)
			nullModel = new NullModel(getStackFrame());
		return nullModel;
	}
	

	public boolean isArrayValue() {
		try {
			return variable.getValue() instanceof IJavaArray &&
					!(((IJavaArrayType) variable.getJavaType()).getComponentType() instanceof IJavaReferenceType); 
		} catch (DebugException e) {
			e.printStackTrace();
			return false;
		}
	}


	
	@Override
	public IFigure createInnerFigure(Graph graph) {
		return new ReferenceFigure(this);
	}
	
	@Override
	public String toString() {
		return getName() + " -> " + getModelTarget();
	}
}
