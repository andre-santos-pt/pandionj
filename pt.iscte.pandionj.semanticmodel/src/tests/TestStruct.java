package tests;


import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import impl.machine.ProgramState;
import impl.program.Factory;
import model.machine.IExecutionData;
import model.program.IBlock;
import model.program.IDataType;
import model.program.IFactory;
import model.program.IModule;
import model.program.IProcedure;
import model.program.IStructType;
import model.program.IVariableDeclaration;

public class TestStruct {

	@Test
	public void test() {
		IFactory factory = new Factory();
		IModule program = factory.createModule("Struct");
		
		IStructType pointType = program.addStruct("Point");
		pointType.addMemberVariable("x", IDataType.INT);
		pointType.addMemberVariable("y", IDataType.INT);
		
		IProcedure moveProc = program.addProcedure("move", IDataType.VOID);
		IVariableDeclaration pParam = moveProc.addParameter("p", pointType);
		
		pParam.addMemberAssignment("x", factory.literal(7));
		
		IProcedure main = program.addProcedure("main", IDataType.INT);
		IBlock body = main.getBody();
		IVariableDeclaration pVar = body.addVariableDeclaration("pp", pointType);
		pVar.addAssignment(pointType.allocationExpression());
		
		body.addProcedureCall(moveProc, pVar.expression());
		
		body.addReturnStatement(pVar.memberExpression("x"));
		
		
		System.out.println(program);
		ProgramState state = new ProgramState(program);
		IExecutionData data = state.execute(main);
		
		assertEquals(new BigDecimal(7), data.getReturnValue().getValue());
	}
}
