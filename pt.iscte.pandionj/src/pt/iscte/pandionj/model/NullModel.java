package pt.iscte.pandionj.model;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;

import pt.iscte.pandionj.figures.NullFigure;

public class NullModel extends EntityModel<IJavaObject> {

	public NullModel(StackFrameModel model) {
		super((IJavaObject) ((IJavaDebugTarget) model.getStackFrame().getDebugTarget()).nullValue(), model);
	}
	
	@Override
	protected void init(IJavaObject entity) {
		
	}
	
	@Override
	public boolean update(int step) {
		return false;
	}

	@Override
	public IFigure createInnerFigure() {
		return new NullFigure();
	}

	@Override
	public String toString() {
		return "NULL";
	}

	@Override
	public boolean hasWidgetExtension() {
		return false;
	}

	@Override
	public void setStep(int stepPointer) {
		
	}

	
}
