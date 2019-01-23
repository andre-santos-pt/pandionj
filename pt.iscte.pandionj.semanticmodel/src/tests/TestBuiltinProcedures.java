package tests;

import org.junit.Test;

import impl.machine.ProgramState;
import impl.program.Factory;
import model.program.IDataType;
import model.program.IFactory;
import model.program.IModule;
import model.program.IProcedure;

public class TestBuiltinProcedures {

	@Test
	public void test() {
		IFactory factory = new Factory();
		IModule program = factory.createModule("Test");
		IProcedure proc = program.addProcedure("test", IDataType.VOID);
		IProcedure print = program.resolveProcedure("print", IDataType.INT);
		proc.getBody().addProcedureCall(print, factory.literal(4));
		System.out.println(program);
		ProgramState state = new ProgramState(program);
		state.execute(proc);
	}
}
