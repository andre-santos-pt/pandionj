package model.machine.impl;

import com.google.common.collect.ImmutableMap;

import model.machine.IProgramState;
import model.machine.IStackFrame;
import model.program.IProgram;

public class Run {
	public static void execute(IProgram program) {
		IProgramState state = new ProgramState(program); // empty instance
		IStackFrame rootFrame = state.getCallStack().newFrame(ImmutableMap.of()); // root frame (empty)
		program.getMainProcedure().execute(state.getCallStack().getTopFrame());
	}
}
