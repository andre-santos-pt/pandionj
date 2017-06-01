package pt.iscte.pandionj;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.zest.core.viewers.IGraphEntityRelationshipContentProvider;

public class StaticArea extends Composite {

	public StaticArea(Composite parent) {
		super(parent, SWT.NONE);
		setBackground(ColorConstants.white);
		setLayout(new FillLayout());
		new Label(this, SWT.NONE).setText("Static area");
		GraphViewerZoomable viewer = new GraphViewerZoomable(this, SWT.NONE);
		
	}

	
	static class NodeProvider implements IGraphEntityRelationshipContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object[] getRelationships(Object source, Object dest) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
