package pt.iscte.pandionj;


import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.PandionJUI;

class StaticArea extends Composite {
	private FrameViewer viewer;
	private IStackFrameModel model;

	StaticArea(Composite parent) {
		super(parent, SWT.NONE);
		setBackground(ColorConstants.white);
		setLayout(new GridLayout());
		viewer = new FrameViewer(this);
	}

	//	@Override
	//	public Point computeSize(int wHint, int hHint) {
	//		return new Point(SWT.DEFAULT, Constants.STATIC_AREA_HEIGHT);
	//	}

	void setInput(IStackFrameModel model) {
		if(this.model != model) {
			this.model = model;

			PandionJUI.executeUpdate(() -> {
				if(!model.getRuntime().isTerminated()) {
					viewer.setModel(model, (v) -> v.isStatic());
				}
				requestLayout();
			});
		}
	}
}
