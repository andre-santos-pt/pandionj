package tests;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import impl.machine.ProgramState;
import impl.program.Factory;
import model.machine.IArray;
import model.machine.IExecutionData;
import model.program.IArrayVariableDeclaration;
import model.program.IBlock;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IFactory;
import model.program.ILoop;
import model.program.IModule;
import model.program.IOperator;
import model.program.IProcedure;
import model.program.ISelectionWithAlternative;
import model.program.IVariableDeclaration;

public class Test2DArrays {

	@Test
	public void testIdMatrix() {
		IFactory factory = new Factory();
		IModule program = factory.createModule("Arrays2D");
		
		IProcedure idFunc = program.addProcedure("idMatrix", factory.arrayType(IDataType.INT, 2));
		IVariableDeclaration nParam = idFunc.addParameter("n", IDataType.INT);
		
		IBlock body = idFunc.getBody();
		IArrayVariableDeclaration idVar = body.addArrayDeclaration("id", factory.arrayType(IDataType.INT, 2));
		idVar.addAssignment(factory.arrayAllocation(IDataType.INT, nParam.expression(), nParam.expression()));
		IVariableDeclaration iVar = body.addVariableDeclaration("i", IDataType.INT);
		IExpression e = factory.binaryExpression(IOperator.DIFFERENT, iVar.expression(), nParam.expression());
		ILoop loop = body.addLoop(e);
		loop.arrayElementAssignment(idVar, factory.literal(1), iVar.expression(), iVar.expression());
		loop.addAssignment(iVar, factory.binaryExpression(IOperator.ADD, iVar.expression(), factory.literal(1)));
		
		body.addReturnStatement(idVar.expression());

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
		IModule program = factory.createModule("NatMatrix");
		
		IProcedure natFunc = program.addProcedure("natMatrix", factory.arrayType(IDataType.INT, 2));
		IVariableDeclaration linesParam = natFunc.addParameter("lines", IDataType.INT);
		IVariableDeclaration colsParam = natFunc.addParameter("cols", IDataType.INT);
		
		IBlock body = natFunc.getBody();
		
		IArrayVariableDeclaration mVar = body.addArrayDeclaration("m", factory.arrayType(IDataType.INT, 2));
		mVar.addAssignment(factory.arrayAllocation(IDataType.INT, linesParam.expression(), colsParam.expression()));
		
		IVariableDeclaration iVar = body.addVariableDeclaration("i", IDataType.INT);
		iVar.addAssignment(factory.literal(0));
		
		IVariableDeclaration jVar = body.addVariableDeclaration("j", IDataType.INT);
		IVariableDeclaration nVar = body.addVariableDeclaration("n", IDataType.INT);
		nVar.addAssignment(factory.literal(1));

		IExpression outerGuard = factory.binaryExpression(IOperator.DIFFERENT, iVar.expression(), linesParam.expression());
		ILoop outerLoop = body.addLoop(outerGuard);
		outerLoop.addAssignment(jVar, factory.literal(0));
		IExpression innerGuard = factory.binaryExpression(IOperator.DIFFERENT, jVar.expression(), colsParam.expression());
		ILoop innerLoop = outerLoop.addLoop(innerGuard);
		innerLoop.arrayElementAssignment(mVar, nVar.expression(), iVar.expression(), jVar.expression());
		innerLoop.addAssignment(jVar, factory.binaryExpression(IOperator.ADD, jVar.expression(), factory.literal(1)));
		innerLoop.addAssignment(nVar, factory.binaryExpression(IOperator.ADD, nVar.expression(), factory.literal(1)));
		
		outerLoop.addAssignment(iVar, factory.binaryExpression(IOperator.ADD, iVar.expression(), factory.literal(1)));
		
		body.addReturnStatement(mVar.expression());
		
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
		IModule program = factory.createModule("ContainsInMatrix");
		
		IProcedure findFunc = program.addProcedure("contains", IDataType.BOOLEAN);
		IArrayVariableDeclaration mParam = (IArrayVariableDeclaration) findFunc.addParameter("m", factory.arrayType(IDataType.INT, 2));
		IVariableDeclaration nParam = findFunc.addParameter("n", IDataType.INT);
		IBlock body = findFunc.getBody();
		IVariableDeclaration iVar = body.addVariableDeclaration("i", IDataType.INT);
		iVar.addAssignment(factory.literal(0));
		
		IVariableDeclaration jVar = body.addVariableDeclaration("j", IDataType.INT);
		IExpression outerGuard = factory.binaryExpression(IOperator.DIFFERENT, iVar.expression(), mParam.lengthExpression());
		ILoop outerLoop = body.addLoop(outerGuard);
		outerLoop.addAssignment(jVar, factory.literal(0));
		IExpression innerGuard = factory.binaryExpression(IOperator.DIFFERENT, jVar.expression(), mParam.lengthExpression(iVar.expression()) );
		ILoop innerLoop = outerLoop.addLoop(innerGuard);
		ISelectionWithAlternative ifEq = innerLoop.addSelectionWithAlternative(factory.binaryExpression(IOperator.EQUAL, mParam.elementExpression(iVar.expression(), jVar.expression()), nParam.expression()));
		ifEq.addReturnStatement(factory.literal(true));
		innerLoop.addIncrement(jVar);
		outerLoop.addIncrement(iVar);
		
		body.addReturnStatement(factory.literal(false));
		
		
		
		
		IProcedure main = program.addProcedure("main", IDataType.BOOLEAN);
		IBlock mainBody = main.getBody();
		IArrayVariableDeclaration array = mainBody.addArrayDeclaration("test", factory.arrayType(IDataType.INT, 2));
		array.addAssignment(factory.arrayAllocation(IDataType.INT, factory.literal(3)));
		array.elementAssignment(factory.arrayAllocation(IDataType.INT, factory.literal(0)), factory.literal(0));
		array.elementAssignment(factory.arrayAllocation(IDataType.INT, factory.literal(2)), factory.literal(1));
		array.elementAssignment(factory.arrayAllocation(IDataType.INT, factory.literal(4)), factory.literal(2));
		
		array.elementAssignment(factory.literal(5), factory.literal(2), factory.literal(2));
		
		IVariableDeclaration var = mainBody.addVariableDeclaration("contains", IDataType.BOOLEAN);
		var.addAssignment(findFunc.callExpression(array.expression(), factory.literal(5)));
		mainBody.addReturnStatement(var.expression());
		
		
		System.out.println(program);
		ProgramState state = new ProgramState(program);
		IExecutionData data = state.execute(main);
		System.out.println(data.getReturnValue());
	}
}
