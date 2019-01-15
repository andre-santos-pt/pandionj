package tests;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.BeforeClass;
import org.junit.Test;

import model.machine.IExecutionData;
import model.machine.impl.ProgramState;
import model.program.IArrayType;
import model.program.IArrayVariableDeclaration;
import model.program.IDataType;
import model.program.IFactory;
import model.program.IGatherer;
import model.program.ILoop;
import model.program.IOperator;
import model.program.IProcedure;
import model.program.IProgram;
import model.program.IVariableDeclaration;
import model.program.impl.Factory;

public class TestSum {

	private static IProgram program;
	private static IProcedure sumArrayProc;
	private static IProcedure main;

	@BeforeClass
	public static void setup() {
		IFactory factory = new Factory();
		program = factory.createProgram();
		
		sumArrayProc = program.createProcedure("sum", IDataType.INT);
		IArrayVariableDeclaration vParam = (IArrayVariableDeclaration) sumArrayProc.addParameter("v", new IArrayType.ValueTypeArray(IDataType.INT, 1));
		
		IVariableDeclaration sVar = sumArrayProc.variableDeclaration("s", IDataType.INT);
		sVar.assignment(factory.literal(0));
		IVariableDeclaration iVar = sumArrayProc.variableDeclaration("i", IDataType.INT);
		iVar.assignment(factory.literal(0));
		
		ILoop loop = sumArrayProc.loop(factory.binaryExpression(IOperator.DIFFERENT, iVar.expression(), vParam.lengthExpression()));
		loop.assignment(sVar, factory.binaryExpression(IOperator.ADD, sVar.expression(), vParam.elementExpression(iVar.expression())));
		loop.assignment(iVar, factory.binaryExpression(IOperator.ADD, iVar.expression(), factory.literal(1)));
		
		sumArrayProc.returnStatement(sVar.expression());
		
		main = program.createProcedure("main", IDataType.INT);
		
		IArrayVariableDeclaration array = main.arrayDeclaration("test", IDataType.INT, 1);
		array.assignment(factory.arrayAllocation(IDataType.INT, factory.literal(3)));
		array.elementAssignment(factory.literal(5), factory.literal(0));
		array.elementAssignment(factory.literal(7), factory.literal(1));
		array.elementAssignment(factory.literal(9), factory.literal(2));
		
		IVariableDeclaration mVar = main.variableDeclaration("s", IDataType.INT);
		mVar.assignment(sumArrayProc.callExpression(array.expression()));
	}

	@Test
	public void testSum() {
		System.out.println(program);
		ProgramState state = new ProgramState(program);
		IExecutionData data = state.execute(main);
		assertEquals(2, data.getCallStackDepth());
		assertEquals(new BigDecimal(21), data.getVariableValue("s").getValue());
		
		assertTrue(sumArrayProc.getVariable("s").getRole() instanceof IGatherer); 
	}
}
