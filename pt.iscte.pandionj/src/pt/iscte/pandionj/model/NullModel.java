package pt.iscte.pandionj.model;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.zest.core.widgets.Graph;

import pt.iscte.pandionj.figures.NullFigure;

public class NullModel extends EntityModel<IJavaObject> {

	public NullModel(StackFrameModel model) {
		super((IJavaObject) ((IJavaDebugTarget) model.getStackFrame().getDebugTarget()).nullValue(), model);
	}
	
	@Override
	protected void init(IJavaObject entity) {
		
	}
	
	@Override
	public void update(int step) {

	}

	@Override
	public IFigure createInnerFigure(Graph graph) {
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

	
}
