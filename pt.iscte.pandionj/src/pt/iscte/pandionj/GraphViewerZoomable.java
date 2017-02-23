package pt.iscte.pandionj;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.internal.ZoomManager;

class GraphViewerZoomable extends GraphViewer {

	GraphViewerZoomable(Composite composite, int style) {
		super(composite, style);
		getZoomManager().setZoom(1.0);
	}

	public void setZoom(double v) {
		ZoomManager mng = getZoomManager();
		mng.setZoom(v);
	}
}
