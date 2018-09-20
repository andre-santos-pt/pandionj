package pt.iscte.pandionj.figures;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.swt.SWT;

import pt.iscte.pandionj.RuntimeViewer;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.PandionJConstants;

public class StackContainer extends Figure {
	
	public StackContainer() {
		GridLayout gridLayout = new GridLayout(1, true);
		gridLayout.verticalSpacing = PandionJConstants.OBJECT_PADDING*2;
		setLayoutManager(gridLayout);
		setOpaque(true);
	}

	public void addFrame(IStackFrameModel frame, RuntimeViewer runtimeViewer, ObjectContainer objectContainer, boolean invisible) {
		if(frame.getLineNumber() != -1) {
			StackFrameFigure sv = new StackFrameFigure(runtimeViewer, frame, objectContainer, invisible);
			add(sv);
			getLayoutManager().setConstraint(sv, new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		}
	}
	
	public void removeFrame(IStackFrameModel frame) {
		Figure f = null;
		for(Object o : getChildren())
			if(((StackFrameFigure)o).frame == frame)
				f = (Figure) o;
		
		if(f != null) {
			remove(f);
			RuntimeViewer.getInstance().removePointers(f);
		}
	}
	
}