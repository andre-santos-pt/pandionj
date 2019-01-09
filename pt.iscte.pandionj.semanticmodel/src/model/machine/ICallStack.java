package model.machine;

import java.util.List;

import model.program.IProcedure;

public interface ICallStack {

	interface Listener {
		void newStackFrame();
	}
	
	int getSize();
	
	IProgramState getProgramState();
	
	List<IStackFrame> getFrames();
	IStackFrame getTopFrame();
	
	IStackFrame newFrame(IProcedure procedure, List<IValue> args);
	
	void terminateTopFrame(IValue returnValue);
}
