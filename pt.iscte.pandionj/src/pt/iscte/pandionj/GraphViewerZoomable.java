package pt.iscte.pandionj;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.internal.ZoomManager;

class GraphViewerZoomable extends GraphViewer {

		GraphViewerZoomable(Composite composite, int style) {
			super(composite, style);
			getZoomManager().setZoom(1);
		}

		public void decreaseZoom() {
			ZoomManager mng = getZoomManager();
			mng.setZoom(mng.getZoom()*.95);
		}

		void increaseZoom() {
			ZoomManager mng = getZoomManager();
			mng.setZoom(mng.getZoom()*1.05);
		}
	}