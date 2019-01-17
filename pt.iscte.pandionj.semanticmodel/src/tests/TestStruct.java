package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import impl.machine.ProgramState;
import impl.program.Factory;
import model.machine.IExecutionData;
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
		
		pParam.addMemberAssignment("x", factory.literal(7));
		
		
		IProcedure main = program.createProcedure("main", IDataType.INT);
		IVariableDeclaration pVar = main.addVariableDeclaration("pp", pointType);
		pVar.addAssignment(pointType.allocationExpression());
		
		main.addProcedureCall(moveProc, pVar.expression());
		
		main.addReturnStatement(pVar.memberExpression("x"));
		
		
		System.out.println(program);
		ProgramState state = new ProgramState(program);
		IExecutionData data = state.execute(main);
		
		assertEquals(new BigDecimal(7), data.getReturnValue().getValue());
	}
}
