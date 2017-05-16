package pt.iscte.pandionj;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import pt.iscte.pandionj.model.StackFrameModel;

class StackView extends Composite {
		double zoom;
		List<FrameView> frames;

		public StackView(Composite parent) {
			super(parent, SWT.NONE);
			setBackground(Constants.WHITE_COLOR);
			setLayout(new GridLayout(1, true));
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			frames = new ArrayList<>();
			zoom = 1.0;
		}

		public void setError(String msg) {
			frames.get(frames.size()-1).setError(msg);
		}

		public void updateFrames(List<StackFrameModel> stackPath) {
			int diff = stackPath.size() - frames.size();

			while(diff > 0) {
				frames.add(new FrameView(this));
				diff--;
			}
			while(diff < 0) {
				frames.remove(frames.size()-1).dispose();
				diff++;
			}

			assert stackPath.size() == frames.size();

			for(int i = 0; i < stackPath.size(); i++) {
				StackFrameModel model = stackPath.get(i);
				frames.get(i).setInput(model);
				frames.get(i).setExpanded(i == stackPath.size()-1);
			}
		}

		public void zoomIn() {
			zoom *= 1.05;
			for(FrameView frame : frames)
				frame.setZoom(zoom);
		}

		public void zoomOut() {
			zoom *= .95;
			for(FrameView frame : frames)
				frame.setZoom(zoom);
		}

		public boolean isEmpty() {
			return frames.isEmpty();
		}

		public void copyToClipBoard() {
			if(!frames.isEmpty())
				frames.get(frames.size()-1).copyToClipBoard();
		}
	}
