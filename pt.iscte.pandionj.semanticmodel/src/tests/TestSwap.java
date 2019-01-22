package tests;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.EnumSet;

import org.junit.BeforeClass;
import org.junit.Test;

import impl.machine.ProgramState;
import impl.program.Factory;
import model.machine.IArray;
import model.machine.IExecutionData;
import model.program.IArrayVariableDeclaration;
import model.program.IDataType;
import model.program.IFactory;
import model.program.IModule;
import model.program.IProcedure;
import model.program.IVariableDeclaration;

public class TestSwap {

	private static IModule program;
	private static IProcedure swapProc;
	private static IProcedure main;

	@BeforeClass
	public static void setup() {
		IFactory factory = new Factory();
		program = factory.createModule("Swap");
		
		swapProc = program.addProcedure("swap", IDataType.VOID);
		IArrayVariableDeclaration vParam = (IArrayVariableDeclaration) swapProc.addParameter("v", factory.arrayType(IDataType.INT, 1), EnumSet.of(IVariableDeclaration.Flag.REFERENCE));
		IVariableDeclaration iParam = swapProc.addParameter("i", IDataType.INT);
		IVariableDeclaration jParam = swapProc.addParameter("j", IDataType.INT);
		
		IVariableDeclaration tVar = swapProc.getBody().addVariableDeclaration("t", IDataType.INT);
		tVar.addAssignment(vParam.elementExpression(iParam.expression()));
		vParam.elementAssignment(vParam.elementExpression(jParam.expression()), iParam.expression());
		vParam.elementAssignment(tVar.expression(), jParam.expression());
		
		swapProc.getBody().addProcedureCall(program.getProcedure("print", IDataType.INT), jParam.expression());

		iParam.addAssignment(factory.literal(4));
		
		main = program.addProcedure("main", IDataType.VOID);
		IArrayVariableDeclaration array = main.getBody().addArrayDeclaration("test", factory.arrayType(IDataType.INT, 1));
		array.addAssignment(factory.arrayAllocation(IDataType.INT, factory.literal(3)));
		array.elementAssignment(factory.literal(5), factory.literal(0));
		array.elementAssignment(factory.literal(7), factory.literal(1));
		array.elementAssignment(factory.literal(9), factory.literal(2));
		
		IVariableDeclaration iVar = main.getBody().addVariableDeclaration("i", IDataType.INT);
		iVar.addAssignment(factory.literal(0));
		
		main.getBody().addProcedureCall(swapProc, array.expression(), iVar.expression(), factory.literal(2));
	}

	@Test
	public void testSwap() {
		System.out.println(program);
		ProgramState state = new ProgramState(program);
		IExecutionData data = state.execute(main);
		IArray array = (IArray) data.getVariableValue("test");
		assertEquals(new BigDecimal(9), array.getElement(0).getValue());
		assertEquals(new BigDecimal(5), array.getElement(2).getValue());
		assertEquals(new BigDecimal(0), data.getVariableValue("i").getValue());
	}
}
