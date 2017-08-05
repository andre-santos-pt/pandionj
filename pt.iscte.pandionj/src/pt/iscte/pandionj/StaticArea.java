package pt.iscte.pandionj;


import java.util.Observable;
import java.util.Observer;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.zest.core.viewers.IGraphEntityRelationshipContentProvider;

import pt.iscte.pandionj.extensibility.PandionJUI;
import pt.iscte.pandionj.model.StackFrameModel;

class StaticArea extends Composite implements Observer {

	private GraphViewerZoomable viewer;
	private StackFrameModel model;

	StaticArea(Composite parent) {
		super(parent, SWT.NONE);
		setLayout(new FillLayout());
		viewer = new GraphViewerZoomable(this, SWT.NONE);
		viewer.setContentProvider(new StaticNodeProvider());
		PandionJLayoutAlgorithm pandionJLayoutAlgorithm = new PandionJLayoutAlgorithm();
		pandionJLayoutAlgorithm.addObserver(viewSizeObserver);
		viewer.setLayoutAlgorithm(pandionJLayoutAlgorithm);
		viewer.getGraphControl().setBackground(ColorConstants.white);
	}

	private Observer viewSizeObserver = new Observer() {
		public void update(Observable o, Object arg) {
			PandionJUI.executeUpdate(() -> {
//				if(viewer.getNodeElements().length == 0)
//					setLayoutData(new GridData(Constants.MARGIN, Constants.MARGIN));
//				else {
					Point size = viewer.getGraphControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
					setLayoutData(new GridData(size.x, size.y));
//				}
				requestLayout();
			});
		}
	};

	//	@Override
	//	public Point computeSize(int wHint, int hHint) {
	//		return new Point(SWT.DEFAULT, Constants.STATIC_AREA_HEIGHT);
	//	}

	void setInput(StackFrameModel model) {
		if(this.model != model) {
			this.model = model;

			PandionJUI.executeUpdate(() -> {
				//				setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
				//				setLayoutData(new GridData(SWT.DEFAULT, 100));
				//				getParent().layout();
				if(!model.getRuntime().isTerminated()) {
					viewer.setInput(model);
					viewer.setLabelProvider(new FigureProvider(model));
					viewer.applyLayout();
//					if(viewer.getNodeElements().length == 0)
//						setLayoutData(new GridData(Constants.MARGIN, Constants.MARGIN));
//					else {
						Point size = viewer.getGraphControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
						setLayoutData(new GridData(size.x, size.y));
//					}
				}

				requestLayout();
			});
			model.registerDisplayObserver(this);
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if(model.isObsolete()) {
			viewer.refresh();
			viewer.applyLayout();
		}
		else {
			model.unregisterObserver(this);
		}
	}

	class StaticNodeProvider implements IGraphEntityRelationshipContentProvider {
		@Override
		public Object[] getElements(Object input) {
			StackFrameModel model = (StackFrameModel) input;
			if(model.getRuntime().isTerminated())
				return NodeProvider.EMPTY;
			else
				return NodeProvider.getElementsInternal(model.getStaticVariables()).toArray();
		}

		@Override
		public Object[] getRelationships(Object source, Object dest) {
			return NodeProvider.getRelationshipsInternal(source, dest);
		}

	}
}
