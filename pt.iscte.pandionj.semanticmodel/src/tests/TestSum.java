package tests;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.BeforeClass;
import org.junit.Test;

import impl.machine.ProgramState;
import impl.program.Factory;
import model.machine.IExecutionData;
import model.program.IArrayVariableDeclaration;
import model.program.IDataType;
import model.program.IFactory;
import model.program.ILoop;
import model.program.IOperator;
import model.program.IProcedure;
import model.program.IProgram;
import model.program.IVariableDeclaration;

public class TestSum {

	private static IProgram program;
	private static IProcedure sumArrayProc;
	private static IProcedure main;

	@BeforeClass
	public static void setup() {
		IFactory factory = new Factory();
		program = factory.createProgram();
		
		sumArrayProc = program.addProcedure("sum", IDataType.INT);
		IArrayVariableDeclaration vParam = (IArrayVariableDeclaration) sumArrayProc.addParameter("v", factory.arrayType(IDataType.INT, 1));
		
		IVariableDeclaration sVar = sumArrayProc.getBody().addVariableDeclaration("s", IDataType.INT);
		sVar.addAssignment(factory.literal(0));
		IVariableDeclaration iVar = sumArrayProc.getBody().addVariableDeclaration("i", IDataType.INT);
		iVar.addAssignment(factory.literal(0));
		
		ILoop loop = sumArrayProc.getBody().addLoop(factory.binaryExpression(IOperator.DIFFERENT, iVar.expression(), vParam.lengthExpression()));
		loop.addAssignment(sVar, factory.binaryExpression(IOperator.ADD, sVar.expression(), vParam.elementExpression(iVar.expression())));
		loop.addAssignment(iVar, factory.binaryExpression(IOperator.ADD, iVar.expression(), factory.literal(1)));
		
		sumArrayProc.getBody().addReturnStatement(sVar.expression());
		
		main = program.addProcedure("main", IDataType.INT);
		
		IArrayVariableDeclaration array = main.getBody().addArrayDeclaration("test", factory.arrayType(IDataType.INT, 1));
		array.addAssignment(factory.arrayAllocation(IDataType.INT, factory.literal(3)));
		array.elementAssignment(factory.literal(5), factory.literal(0));
		array.elementAssignment(factory.literal(7), factory.literal(1));
		array.elementAssignment(factory.literal(9), factory.literal(2));
		
		IVariableDeclaration mVar = main.getBody().addVariableDeclaration("s", IDataType.VOID);
		mVar.addAssignment(sumArrayProc.callExpression(array.expression()));
	}

	@Test
	public void testSum() {
		System.out.println(program);
		ProgramState state = new ProgramState(program);
		IExecutionData data = state.execute(main);
		assertEquals(2, data.getCallStackDepth());
		assertEquals(new BigDecimal(21), data.getVariableValue("s").getValue());
		
		
//		assertTrue(sumArrayProc.getVariable("s").getRole() instanceof IGatherer); 
	}
	
	// TODO @Test average
}
