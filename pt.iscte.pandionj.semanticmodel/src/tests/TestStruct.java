package tests;

import org.junit.Test;

import impl.program.Factory;
import model.program.IDataType;
import model.program.IFactory;
import model.program.IProcedure;
import model.program.IProgram;
import model.program.IStructType;
import model.program.IVariableDeclaration;

public class TestStruct {

	@Test
	public void test() {
		IFactory factory = new Factory();
		IProgram program = factory.createProgram();
		
		IStructType pointType = program.createStruct("Point");
		pointType.addMemberVariable("x", IDataType.INT);
		pointType.addMemberVariable("y", IDataType.INT);
		
		IProcedure moveProc = program.createProcedure("move", IDataType.VOID);
		IVariableDeclaration pParam = moveProc.addParameter("p", pointType);
		
		// TODO struct test
		
		
		
		System.out.println(program);
	}
}
