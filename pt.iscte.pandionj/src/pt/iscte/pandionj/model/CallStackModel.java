package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.eclipse.debug.core.model.IStackFrame;



public class CallStackModel extends Observable {

	private List<StackFrameModel> frames;

	public CallStackModel() {
		frames = new ArrayList<>();
	}


	public void update() {
		for(StackFrameModel f : frames)
			f.update();
	}

//	public void handle(IStackFrame[] stackFrames) {
//		boolean clean = false;
//		for(int i = stackFrames.length-1; i >= 0; i--) {
//			if(i < frames.size() && frames.get(i).getStackFrame() == stackFrames[i])
//				continue;
//			else {
//				if(!clean && i < frames.size()) {
//					StackFrameModel removed = null;
//					while(removed != frames.get(i))
//						removed = frames.remove(0);
//					clean = true;
//				}
//				frames.add(0, new StackFrameModel(stackFrames[i]));
//			}
//		}
//	}
	
	public void handle(IStackFrame[] stackFrames) {
		boolean clean = false;
		for(int i = stackFrames.length-1; i >= 0; i--) {
			if(i < frames.size() && frames.get(i).getStackFrame() == stackFrames[i])
				continue;
			else {
				if(!clean && i < frames.size()) {
					StackFrameModel removed = null;
					StackFrameModel tmp = frames.get(i);
					while(removed != tmp)
						removed = frames.remove(0);
					clean = true;
				}
				frames.add(0, new StackFrameModel(stackFrames[i]));
			}
		}
	}

	public StackFrameModel getTopFrame() {
		return frames.get(0);
	}

	public int getSize() {
		return frames.size();
	}


	//	public StackFrameModel getFrame(int index) {
	//		// validate
	//		return frames.get(index);
	//	}

}
