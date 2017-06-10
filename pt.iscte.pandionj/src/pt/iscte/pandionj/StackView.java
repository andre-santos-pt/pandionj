package pt.iscte.pandionj;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import pt.iscte.pandionj.model.CallStackModel;
import pt.iscte.pandionj.model.StackFrameModel;

class StackView extends Composite {
		double zoom;
		List<FrameView> frameViews;
		CallStackModel model;
		
		StackView(Composite parent) {
			super(parent, SWT.NONE);
			setBackground(ColorConstants.white);
			setLayout(new GridLayout(1, true));
			frameViews = new ArrayList<>();
			zoom = 1.0;
		}

		void setInput(CallStackModel model) {
			assert model != null;
			this.model = model;
			model.addObserver((o,a) -> 
				Display.getDefault().asyncExec(() -> updateFrames(model.getFilteredStackPath()))
			);
		}
		
		private void updateFrames(List<StackFrameModel> stackPath) {
			int diff = stackPath.size() - frameViews.size();

			while(diff > 0) {
				FrameView view = new FrameView(this);
				view.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				frameViews.add(view);
				diff--;
			}
			while(diff < 0) {
				frameViews.remove(frameViews.size()-1).dispose();
				diff++;
			}

			assert stackPath.size() == frameViews.size();

			for(int i = 0; i < stackPath.size(); i++)
				frameViews.get(i).setInput(stackPath.get(i));
			
			layout();
		}

		public void zoomIn() {
			zoom *= 1.05;
			for(FrameView frame : frameViews)
				frame.setZoom(zoom);
		}

		public void zoomOut() {
			zoom *= .95;
			for(FrameView frame : frameViews)
				frame.setZoom(zoom);
		}

		public boolean isEmpty() {
			return frameViews.isEmpty();
		}

		public void copyToClipBoard() {
			if(!frameViews.isEmpty())
				frameViews.get(frameViews.size()-1).copyToClipBoard();
		}
	}
