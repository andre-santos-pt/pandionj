package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import model.machine.IExecutionData;
import model.machine.impl.ProgramState;
import model.program.IBinaryExpression;
import model.program.IDataType;
import model.program.IFactory;
import model.program.IOperator;
import model.program.IProcedure;
import model.program.IProgram;
import model.program.IUnaryExpression;
import model.program.IVariableDeclaration;
import model.program.impl.Factory;

public class TestRandom {

	@Test
	public void testRandom() {
		IFactory factory = new Factory();
		IProgram program = factory.createProgram();
		IProcedure proc = program.createProcedure("randomDouble", IDataType.DOUBLE);
		proc.returnStatement(program.getProcedure("random").callExpression());
		ProgramState state = new ProgramState(program);
		IExecutionData data = state.execute(proc);
		assertEquals(2, data.getTotalProcedureCalls());
	}
	
	@Test 
	public void testRandomInt() {
		IFactory factory = new Factory();
		IProgram program = factory.createProgram();
		IProcedure proc = program.createProcedure("randomInt", IDataType.DOUBLE);
		IVariableDeclaration minParam = proc.addParameter("min", IDataType.INT);
		IVariableDeclaration maxParam = proc.addParameter("max", IDataType.INT);
		IVariableDeclaration rVar = proc.variableDeclaration("r", IDataType.DOUBLE);
		rVar.assignment(program.getProcedure("random").callExpression());
		IBinaryExpression d = factory.binaryExpression(IOperator.SUB, maxParam.expression(), minParam.expression());
		IBinaryExpression d1 = factory.binaryExpression(IOperator.ADD, d, factory.literal(1));
		IBinaryExpression m = factory.binaryExpression(IOperator.MUL, rVar.expression(), d1);
		IUnaryExpression t = factory.unaryExpression(IOperator.TRUNCATE, m);
		IBinaryExpression e = factory.binaryExpression(IOperator.ADD, minParam.expression(), t);
		proc.returnStatement(e);
		
		System.out.println(program);
		ProgramState state = new ProgramState(program);
		for(int i = 0; i < 10; i++) {
			IExecutionData data = state.execute(proc, "1", "10");
			Number output = (Number) data.getReturnValue().getValue(); 
			assertTrue(output.intValue() >= 1); 
			assertTrue(output.intValue() <= 10); 
		}
	}
	
}
