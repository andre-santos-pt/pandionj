package pt.iscte.pandionj;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.zest.core.viewers.IGraphEntityRelationshipContentProvider;
import org.eclipse.zest.layouts.algorithms.HorizontalShift;

import pt.iscte.pandionj.model.StackFrameModel;

class StaticArea extends Composite {

//	CallStackModel model;
	private GraphViewerZoomable viewer;
	
	
	StaticArea(Composite parent) {
		super(parent, SWT.NONE);
		setBackground(ColorConstants.white);
		setLayout(new FillLayout());
		viewer = new GraphViewerZoomable(this, SWT.NONE);
		viewer.setContentProvider(new NodeProvider());
		viewer.setLayoutAlgorithm(new HorizontalShift(0));
		new Label(parent, SWT.NONE).setText("static");
	}
	
	
	void setInput(StackFrameModel model) {
		assert model != null;
//		this.model = frames;
		model.addObserver((o,a) -> {
			Display.getDefault().asyncExec(() -> {
				viewer.setInput(model);
				layout();
			});
		});
		//TODO: observer
		
	}
	
	
	static class NodeProvider implements IGraphEntityRelationshipContentProvider {
		@Override
		public Object[] getElements(Object input) {
			StackFrameModel model = (StackFrameModel) input;
			return model.getStaticVariables().toArray();
		}

		@Override
		public Object[] getRelationships(Object source, Object dest) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
