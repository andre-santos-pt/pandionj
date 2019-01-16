package tests;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import impl.machine.ProgramState;
import impl.program.Factory;
import model.machine.IArray;
import model.machine.IExecutionData;
import model.program.IArrayType;
import model.program.IArrayVariableDeclaration;
import model.program.IBlock;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IFactory;
import model.program.ILoop;
import model.program.IOperator;
import model.program.IProcedure;
import model.program.IProgram;
import model.program.IVariableDeclaration;

public class Test2DArrays {

	@Test
	public void testIdMatrix() {
		IFactory factory = new Factory();
		IProgram program = factory.createProgram();
		
		IProcedure idFunc = program.createProcedure("idMatrix", new IArrayType.ValueTypeArray(IDataType.INT, 2));
		IVariableDeclaration nParam = idFunc.addParameter("n", IDataType.INT);
		
		IArrayVariableDeclaration idVar = idFunc.arrayDeclaration("id", factory.arrayType(IDataType.INT, 2));
		idVar.assignment(factory.arrayAllocation(IDataType.INT, nParam.expression(), nParam.expression()));
		IVariableDeclaration iVar = idFunc.variableDeclaration("i", IDataType.INT);
		IExpression e = factory.binaryExpression(IOperator.DIFFERENT, iVar.expression(), nParam.expression());
		ILoop loop = idFunc.loop(e);
		loop.arrayElementAssignment(idVar, factory.literal(1), iVar.expression(), iVar.expression());
		loop.assignment(iVar, factory.binaryExpression(IOperator.ADD, iVar.expression(), factory.literal(1)));
		
		idFunc.returnStatement(idVar.expression());

		System.out.println(program);
		final int N = 4;
		final BigDecimal ZERO = new BigDecimal(0);
		final BigDecimal ONE = new BigDecimal(1);
		
		ProgramState state = new ProgramState(program);
		IExecutionData data = state.execute(idFunc, N);
		IArray returnValue = (IArray) data.getReturnValue();
		assertEquals(N, returnValue.getLength());
		for(int i = 0; i < N; i++) {
			IArray line = (IArray) returnValue.getElement(i);
			assertEquals(N, line.getLength());
			for(int j = 0; j < N; j++)
				assertEquals(i == j ? ONE : ZERO, line.getElement(j).getValue());
		}
	}


	@Test
	public void testNatMatrix() {
		IFactory factory = new Factory();
		IProgram program = factory.createProgram();
		
		IProcedure natFunc = program.createProcedure("natMatrix", new IArrayType.ValueTypeArray(IDataType.INT, 2));
		IVariableDeclaration linesParam = natFunc.addParameter("lines", IDataType.INT);
		IVariableDeclaration colsParam = natFunc.addParameter("cols", IDataType.INT);
		
		IArrayVariableDeclaration mVar = natFunc.arrayDeclaration("m", factory.arrayType(IDataType.INT, 2));
		mVar.assignment(factory.arrayAllocation(IDataType.INT, linesParam.expression(), colsParam.expression()));
		
		IVariableDeclaration iVar = natFunc.variableDeclaration("i", IDataType.INT);
		iVar.assignment(factory.literal(0));
		
		IVariableDeclaration jVar = natFunc.variableDeclaration("j", IDataType.INT);
		IVariableDeclaration nVar = natFunc.variableDeclaration("n", IDataType.INT);
		nVar.assignment(factory.literal(1));

		IExpression outerGuard = factory.binaryExpression(IOperator.DIFFERENT, iVar.expression(), linesParam.expression());
		ILoop outerLoop = natFunc.loop(outerGuard);
		outerLoop.assignment(jVar, factory.literal(0));
		IExpression innerGuard = factory.binaryExpression(IOperator.DIFFERENT, jVar.expression(), colsParam.expression());
		ILoop innerLoop = outerLoop.loop(innerGuard);
		innerLoop.arrayElementAssignment(mVar, nVar.expression(), iVar.expression(), jVar.expression());
		innerLoop.assignment(jVar, factory.binaryExpression(IOperator.ADD, jVar.expression(), factory.literal(1)));
		innerLoop.assignment(nVar, factory.binaryExpression(IOperator.ADD, nVar.expression(), factory.literal(1)));
		
		outerLoop.assignment(iVar, factory.binaryExpression(IOperator.ADD, iVar.expression(), factory.literal(1)));
		
		natFunc.returnStatement(mVar.expression());
		
		final int L = 2;
		final int C = 4;
		ProgramState state = new ProgramState(program);
		System.out.println(program);
		IExecutionData data = state.execute(natFunc, L, C);
		IArray returnValue = (IArray) data.getReturnValue();
		assertEquals(L, returnValue.getLength());
		int n = 1;
		for(int i = 0; i < L; i++) {
			IArray line = (IArray) returnValue.getElement(i);
			assertEquals(C, line.getLength());
			for(int j = 0; j < C; j++)
				assertEquals(new BigDecimal(n++), line.getElement(j).getValue());
		}
		
		data.printResult();
	}
	
	@Test
	public void testContainsNinMatrix() {
		IFactory factory = new Factory();
		IProgram program = factory.createProgram();
		
		IProcedure findFunc = program.createProcedure("contains", IDataType.BOOLEAN);
		IArrayVariableDeclaration mParam = (IArrayVariableDeclaration) findFunc.addParameter("m", factory.arrayType(IDataType.INT, 2));
		IVariableDeclaration nParam = findFunc.addParameter("n", IDataType.INT);
		
		IVariableDeclaration iVar = findFunc.variableDeclaration("i", IDataType.INT);
		iVar.assignment(factory.literal(0));
		
		IVariableDeclaration jVar = findFunc.variableDeclaration("j", IDataType.INT);
		IExpression outerGuard = factory.binaryExpression(IOperator.DIFFERENT, iVar.expression(), mParam.lengthExpression());
		ILoop outerLoop = findFunc.loop(outerGuard);
		outerLoop.assignment(jVar, factory.literal(0));
		IExpression innerGuard = factory.binaryExpression(IOperator.DIFFERENT, jVar.expression(), mParam.lengthExpression(iVar.expression()) );
		ILoop innerLoop = outerLoop.loop(innerGuard);
		IBlock ifBlock = innerLoop.block();
		ifBlock.returnStatement(factory.literal(true));
		innerLoop.selection(factory.binaryExpression(IOperator.EQUAL, mParam.elementExpression(iVar.expression(), jVar.expression()), nParam.expression()), ifBlock);
		innerLoop.increment(jVar);
		outerLoop.increment(iVar);
		
		findFunc.returnStatement(factory.literal(false));
		
		
		
		
		IProcedure main = program.createProcedure("main", IDataType.BOOLEAN);
		IArrayVariableDeclaration array = main.arrayDeclaration("test", factory.arrayType(IDataType.INT, 2));
		array.assignment(factory.arrayAllocation(IDataType.INT, factory.literal(3)));
		array.elementAssignment(factory.arrayAllocation(IDataType.INT, factory.literal(0)), factory.literal(0));
		array.elementAssignment(factory.arrayAllocation(IDataType.INT, factory.literal(2)), factory.literal(1));
		array.elementAssignment(factory.arrayAllocation(IDataType.INT, factory.literal(4)), factory.literal(2));
		
		array.elementAssignment(factory.literal(5), factory.literal(2), factory.literal(2));
		
		IVariableDeclaration var = main.variableDeclaration("contains", IDataType.BOOLEAN);
		var.assignment(findFunc.callExpression(array.expression(), factory.literal(5)));
		main.returnStatement(var.expression());
		
		
		System.out.println(program);
		ProgramState state = new ProgramState(program);
		IExecutionData data = state.execute(main);
		System.out.println(data.getReturnValue());
	}
}
