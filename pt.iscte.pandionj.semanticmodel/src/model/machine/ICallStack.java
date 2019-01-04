package model.machine;

import java.util.List;
import java.util.Map;

import model.program.IVariableDeclaration;

public interface ICallStack {

	interface Listener {
		void newStackFrame();
	}
	
	IProgramState getProgramState();
	
	List<IStackFrame> getFrames();
	IStackFrame getTopFrame();
	
	IStackFrame newFrame(Map<IVariableDeclaration, IValue> variables);
	void terminateTopFrame(IValue returnValue);
}
