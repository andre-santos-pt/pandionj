package pt.iscte.pandionj;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.GestureEvent;
import org.eclipse.swt.events.GestureListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.internal.ZoomManager;
import org.eclipse.zest.core.widgets.Graph;

class GraphViewerZoomable extends GraphViewer {

	GraphViewerZoomable(Composite composite, int style) {
		super(composite, style);
		getZoomManager().setZoom(1.0);
		Graph graph = getGraphControl();
		graph.setScrollBarVisibility(FigureCanvas.ALWAYS);
		graph.setTouchEnabled(false);
		graph.addGestureListener(new GestureListener() {
			public void gesture(GestureEvent e) {
				if(e.detail == SWT.GESTURE_MAGNIFY)
					zoom(e.magnification);
			}
		});
	}

	private void zoom(double ratio) {
		ZoomManager mng = getZoomManager();
		mng.setZoom(ratio);
	}
	
	
}
