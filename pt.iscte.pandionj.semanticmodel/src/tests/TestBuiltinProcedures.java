package tests;

import org.junit.Test;

import impl.machine.ProgramState;
import impl.program.Factory;
import model.program.IDataType;
import model.program.IFactory;
import model.program.IModule;
import model.program.IProcedure;

public class TestBuiltinProcedures {

	public static class TestProcedures {
		public static int max(int a, int b) {
			return a > b ? a : b;
		}
	}
	
	@Test
	public void test() {
		IFactory factory = new Factory();
		IModule program = factory.createModule("Test");
		program.loadBuildInProcedures(TestBuiltinProcedures.TestProcedures.class);
		IProcedure proc = program.addProcedure("test", IDataType.VOID);
		IProcedure max = program.resolveProcedure("max", IDataType.INT, IDataType.INT);
		proc.getBody().addProcedureCall(max, factory.literal(4), factory.literal(6));
		System.out.println(program);
		ProgramState state = new ProgramState(program);
		state.execute(proc);
	}
}
