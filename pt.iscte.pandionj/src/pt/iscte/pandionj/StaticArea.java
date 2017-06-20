package pt.iscte.pandionj;


import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.zest.core.viewers.IGraphEntityRelationshipContentProvider;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;

import pt.iscte.pandionj.model.StackFrameModel;


// TODO adjust size
class StaticArea extends Composite {

	private GraphViewerZoomable viewer;
	private StackFrameModel model;
	
	StaticArea(Composite parent) {
		super(parent, SWT.NONE);
		setLayout(new FillLayout());
		viewer = new GraphViewerZoomable(this, SWT.NONE); // TODO zoom
		viewer.setContentProvider(new StaticNodeProvider());
		viewer.setLayoutAlgorithm(new PandionJLayoutAlgorithm());
		viewer.getGraphControl().setBackground(ColorConstants.white);
		viewer.setLabelProvider(new FigureProvider());
	}

//	@Override
//	public Point computeSize(int wHint, int hHint) {
//		return new Point(SWT.DEFAULT, Constants.STATIC_AREA_HEIGHT);
//	}

	void setInput(StackFrameModel model) {
		if(this.model != model) {
			this.model = model;
			boolean collapse = model.getStaticVariables().isEmpty();
			PandionJView.executeUpdate(() -> {
				setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, !collapse));
				getParent().layout();
				viewer.setInput(model);
			}
			);
			// TODO if there are changes
			model.addObserver((o,a) -> {
				Display.getDefault().asyncExec(() -> {
					viewer.refresh();
					layout();
				});
			});
		}
		//TODO: observer

	}


	static class StaticNodeProvider implements IGraphEntityRelationshipContentProvider {
		@Override
		public Object[] getElements(Object input) {
			StackFrameModel model = (StackFrameModel) input;
			return NodeProvider.getElementsInternal(model.getStaticVariables()).toArray();
		}

		@Override
		public Object[] getRelationships(Object source, Object dest) {
			return NodeProvider.getRelationshipsInternal(source, dest);
		}

	}
}
