package tests;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.EnumSet;

import org.junit.BeforeClass;
import org.junit.Test;

import model.machine.IArray;
import model.machine.IExecutionData;
import model.machine.IValue;
import model.machine.impl.ProgramState;
import model.program.IArrayType;
import model.program.IArrayVariableDeclaration;
import model.program.IDataType;
import model.program.IFactory;
import model.program.IProcedure;
import model.program.IProgram;
import model.program.IVariableDeclaration;
import model.program.impl.Factory;

public class TestSwap {

	private static IProgram program;
	private static IProcedure swapProc;
	private static IProcedure main;

	@BeforeClass
	public static void setup() {
		IFactory factory = new Factory();
		program = factory.createProgram();
		
		swapProc = program.createProcedure("swap", IDataType.VOID);
		IArrayVariableDeclaration vParam = (IArrayVariableDeclaration) swapProc.addParameter("v", new IArrayType.ValueTypeArray(IDataType.INT, 1), EnumSet.of(IVariableDeclaration.Flag.REFERENCE));
		IVariableDeclaration iParam = swapProc.addParameter("i", IDataType.INT);
		IVariableDeclaration jParam = swapProc.addParameter("j", IDataType.INT);
		
		IVariableDeclaration tVar = swapProc.variableDeclaration("t", IDataType.INT);
		tVar.assignment(vParam.elementExpression(iParam.expression()));
		vParam.elementAssignment(vParam.elementExpression(jParam.expression()), iParam.expression());
		vParam.elementAssignment(tVar.expression(), jParam.expression());
		
		swapProc.procedureCall(program.getProcedure("print"), jParam.expression());

		iParam.assignment(factory.literal(4));
		
		main = program.createProcedure("main", IDataType.VOID);
		IArrayVariableDeclaration array = main.arrayDeclaration("test", IDataType.INT, 1);
		array.assignment(factory.arrayAllocation(IDataType.INT, factory.literal(3)));
		array.elementAssignment(factory.literal(5), factory.literal(0));
		array.elementAssignment(factory.literal(7), factory.literal(1));
		array.elementAssignment(factory.literal(9), factory.literal(2));
		
		IVariableDeclaration iVar = main.variableDeclaration("i", IDataType.INT);
		iVar.assignment(factory.literal(0));
		
		main.procedureCall(swapProc, array.expression(), iVar.expression(), factory.literal(2));
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
