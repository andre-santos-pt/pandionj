package pt.iscte.pandionj.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.parser.BlockInfo.BlockInfoVisitor;
import pt.iscte.pandionj.parser.VariableOperation.Type;


public class VarParser {
	private JavaSourceParser parser;
	private CompilationUnit cunit;
	private MethodVisitor visitor;

	public VarParser(String path) {
		parser = JavaSourceParser.createFromFile(path);
		cunit = parser.getCompilationUnit();
	}

	public VarParser(IFile file) {
		this(file.getLocation().toOSString());
	}

	public void run() {
		visitor = new MethodVisitor();
		parser.parse(visitor);
//		if(visitor.current != null)
//			visitor.current.accept(new PostRolesVisitor());
	}

//	static class PostRolesVisitor implements BlockInfoVisitor {
//		@Override
//		public void visit(VariableInfo var) {
//			if(var.isFixedValue()) {
//				var.getDeclarationBlock().accept(new BlockInfoVisitor() {
//					public void visit(VariableInfo v) {
//						if(v.isArrayIndex() && var.getName().equals(v.getInitVariable()))
//							var.setFixedArrayIndex(v.getAccessedArrays());
//					}
//				});
//			}
//		}
//	}

	public String toText() {
		assert visitor != null;
		String text = "";
		for(BlockInfo b : visitor.roots)
			text += b.toText() + "\n";

		return text;
	}

	public VariableInfo locateVariable(String name, int line) {
		assert visitor != null;
		for(BlockInfo b : visitor.roots) {
			VariableInfo v = b.locateVariable(name, line);
			if(v != null)
				return v;
		}
		return null;
	}


	class MethodVisitor extends ASTVisitor {
		List<BlockInfo> roots = new ArrayList<>();
		BlockInfo current = null;

		private BlockInfo createBlock(ASTNode node) {
			int start = cunit.getLineNumber(node.getStartPosition());
			int end = cunit.getLineNumber(node.getStartPosition() + node.getLength()-1);
			return new BlockInfo(current, start, end, getBlockType(node));
		}

		// TODO other blocks
		private BlockInfo.Type getBlockType(ASTNode node) {
			if(node instanceof TypeDeclaration)
				return BlockInfo.Type.TYPE;

			else if(node instanceof MethodDeclaration)
				return BlockInfo.Type.METHOD;

			else if(node instanceof WhileStatement)
				return BlockInfo.Type.WHILE;

			else if(node instanceof ForStatement)
				return BlockInfo.Type.FOR;

			else if(node instanceof IfStatement)
				return BlockInfo.Type.IF;

			else
				return BlockInfo.Type.OTHER;
		}



		@Override
		public boolean visit(TypeDeclaration node) {
			BlockInfo block = createBlock(node);
			current = block;
			current.setId(node.getName().toString());
			if(node.getParent() instanceof CompilationUnit)
				roots.add(block);
			return true;
		}

		@Override
		public void endVisit(TypeDeclaration node) {
			current = current.getParent();
		}

		@Override
		public boolean visit(FieldDeclaration node) {
			for(Object o : node.fragments()) {
				handleVarDeclaration((VariableDeclarationFragment) o);
			}
			return true;
		}

		private boolean isLoopStatement(Block block) {
			ASTNode parent = block.getParent();
			return
					parent instanceof WhileStatement ||
					parent instanceof ForStatement ||
					parent instanceof DoStatement ||
					parent instanceof EnhancedForStatement ||
					parent instanceof IfStatement;
		}

		@Override
		public boolean visit(Block node) {
			if(!isLoopStatement(node)) {
				ASTNode parent = node.getParent();
				current = createBlock(parent);

				if(parent instanceof MethodDeclaration)
					handleMethodParams((MethodDeclaration) parent);
			}
			return true;
		}

		@Override
		public void endVisit(Block node) {
			if(!isLoopStatement(node))
				current = current.getParent();
		}

		private void handleMethodParams(MethodDeclaration node) {
			current.setId(node.getName().toString());
			for(Object p : node.parameters()) {
				SingleVariableDeclaration var = (SingleVariableDeclaration) p;
				current.addVar(var.getName().getIdentifier(), true);
			}
		}



		@Override
		public boolean visit(VariableDeclarationExpression node) {
			for(Object var : node.fragments())
				handleVarDeclaration((VariableDeclarationFragment) var);
			return true;
		}


		@Override
		public boolean visit(VariableDeclarationStatement node) {
			for(Object var : node.fragments())
				handleVarDeclaration((VariableDeclarationFragment) var);
			return true;
		}

		private void handleVarDeclaration(VariableDeclarationFragment var) {
			String varName = var.getName().getIdentifier();
			VariableInfo varInfo = current.addVar(varName, false);
			Expression init = var.getInitializer();
			if(init instanceof SimpleName) {
				String initVar = ((SimpleName) init).getIdentifier();
				varInfo.addOperation(new VariableOperation(varName, VariableOperation.Type.INIT, initVar));
			}

		}


		// TODO n += LITERAL?
		@Override
		public boolean visit(Assignment node) {
			if(node.getLeftHandSide() instanceof SimpleName) {
				String varName = node.getLeftHandSide().toString(); 
				VariableOperation.Type op = null;

				if(node.getOperator() == Assignment.Operator.PLUS_ASSIGN && node.getRightHandSide() instanceof NumberLiteral || 
						isAcumulationAssign(node, InfixExpression.Operator.PLUS, (e) -> e instanceof NumberLiteral))
					op = VariableOperation.Type.INC;

				else if(node.getOperator() == Assignment.Operator.MINUS_ASSIGN && node.getRightHandSide() instanceof NumberLiteral ||
						isAcumulationAssign(node, InfixExpression.Operator.MINUS, (e) -> e instanceof NumberLiteral))
					op = VariableOperation.Type.DEC;

				if(op != null)
					current.addOperation(new VariableOperation(varName, op));
				else {
					op = Type.SUBS;
					Object[] params = new Object[0];
					if(node.getOperator() == Assignment.Operator.PLUS_ASSIGN ||
							isAcumulationAssign(node, InfixExpression.Operator.PLUS, (e) -> !(e instanceof NumberLiteral)) || 
							node.getOperator() == Assignment.Operator.MINUS_ASSIGN ||
							isAcumulationAssign(node, InfixExpression.Operator.MINUS, (e) -> !(e instanceof NumberLiteral))) {
						op = Type.ACC;
						params = new Object[] {"sum"};
					}
					else if(node.getOperator() == Assignment.Operator.TIMES_ASSIGN ||
							isAcumulationAssign(node, InfixExpression.Operator.TIMES, (e) -> !(e instanceof NumberLiteral)) || 
							node.getOperator() == Assignment.Operator.DIVIDE_ASSIGN ||
							isAcumulationAssign(node, InfixExpression.Operator.DIVIDE, (e) -> !(e instanceof NumberLiteral))) {
						op = Type.ACC;
						params = new Object[] {"prod"};
					}

					current.addOperation(new VariableOperation(varName, op, params));
				}
			}
			return true;
		}


		@Override
		public boolean visit(PostfixExpression exp) {
			if(exp.getOperand() instanceof SimpleName) {
				String varName = exp.getOperand().toString(); 
				VariableOperation op = null;
				if(exp.getOperator() == PostfixExpression.Operator.INCREMENT)
					op = new VariableOperation(varName, VariableOperation.Type.INC);

				else if(exp.getOperator() == PostfixExpression.Operator.DECREMENT)
					op = new VariableOperation(varName, VariableOperation.Type.DEC);

				if(op != null)
					current.addOperation(op);
			}
			return true;
		}



		@Override
		public boolean visit(PrefixExpression exp) {
			if(exp.getOperand() instanceof SimpleName) {
				String varName = exp.getOperand().toString(); 
				VariableOperation op = null;
				if(exp.getOperator() == PrefixExpression.Operator.INCREMENT)
					op = new VariableOperation(varName, VariableOperation.Type.INC);

				else if(exp.getOperator() == PrefixExpression.Operator.DECREMENT)
					op = new VariableOperation(varName, VariableOperation.Type.DEC);

				if(op != null)
					current.addOperation(op);
			}
			return true;
		}

		
		private void checkBounds(Expression exp) {
			exp.accept(new BoundVisitor());
		}

		class BoundVisitor extends ASTVisitor {
			public boolean visit(InfixExpression exp) {
				Operator op = exp.getOperator();
				if(isCompareOperator(op)) {
					String leftExp = exp.getLeftOperand().toString();
					String rightExp = exp.getRightOperand().toString();

					Set<String> incVars = current.getOperations(VariableOperation.Type.INC, VariableOperation.Type.DEC);

					if(exp.getLeftOperand() instanceof SimpleName && incVars.contains(leftExp))
						aux(leftExp, op, exp.getRightOperand());

					if(exp.getRightOperand() instanceof SimpleName && incVars.contains(rightExp))
						aux(rightExp, op, exp.getLeftOperand());
				}
				return true;
			}
		}

		private boolean isCompareOperator(InfixExpression.Operator op) {
			return
					op == InfixExpression.Operator.LESS || 
					op == InfixExpression.Operator.LESS_EQUALS ||
					op == InfixExpression.Operator.GREATER ||
					op == InfixExpression.Operator.GREATER_EQUALS ||
					op == InfixExpression.Operator.NOT_EQUALS;
		}

		private boolean isOpen(InfixExpression.Operator op){
			return
					op == InfixExpression.Operator.LESS ||
					op == InfixExpression.Operator.GREATER ||
					op == InfixExpression.Operator.NOT_EQUALS;
		}


		private void aux(String var, InfixExpression.Operator operator, Expression exp) {

			String type = (isOpen(operator) ? IArrayIndexModel.BoundType.OPEN : IArrayIndexModel.BoundType.CLOSE).name();
			VariableOperation op = new VariableOperation(var, VariableOperation.Type.BOUNDED, exp.toString(), type);
			current.addOperation(op);
		}

		class CheckBoundExpression extends ASTVisitor {
			boolean ok = true;

			@Override
			public void preVisit(ASTNode node) {
				if(!(node instanceof SimpleName || node instanceof QualifiedName || node instanceof NumberLiteral))
					ok = false;
			}
		}

		class SearchSimpleVarVisitor extends ASTVisitor {
			final List<String> vars = new ArrayList<>(5);
			@Override
			public boolean visit(SimpleName node) {
				if(!(node.getParent() instanceof QualifiedName))
					vars.add(node.getIdentifier());
				return true;
			}

			boolean contains(String var) {
				return vars.contains(var);
			}
		}



		@Override
		public boolean visit(WhileStatement node) {
			current = createBlock(node);
			return true;
		}

		@Override
		public void endVisit(WhileStatement node) {
			checkBounds(node.getExpression());
			current = current.getParent();
		}

		@Override
		public boolean visit(ForStatement node) {
			current = createBlock(node);
			return true;
		}

		@Override
		public void endVisit(ForStatement node) {
			checkBounds(node.getExpression());

			current = current.getParent();
		}

		@Override
		public boolean visit(IfStatement node) {
			current = createBlock(node);
			return true;
		}

		@Override
		public void endVisit(IfStatement node) {
			current = current.getParent();
		}



		// TODO pre and postfix i++
		@Override
		public boolean visit(ArrayAccess node) {
			if(node.getArray() instanceof SimpleName || node.getArray() instanceof ArrayAccess) {
				String arrayRef = arrayRef(node);
				int dim = indexDepth(node);
				if(node.getIndex() instanceof SimpleName) {
					VariableOperation op = new VariableOperation(node.getIndex().toString(), VariableOperation.Type.INDEX, arrayRef, dim);
					current.addOperation(op);
				}
				class NoInvocationVisitor extends ASTVisitor {
					boolean ok = true;
					public boolean visit(MethodInvocation node) {
						ok = false;
						return false;
					}
				};

				//TODO: 2 dim
				NoInvocationVisitor v = new NoInvocationVisitor();
				node.getIndex().accept(v);
				if(v.ok) {
					VariableOperation op = new VariableOperation(arrayRef, VariableOperation.Type.ACCESS, node.getIndex(), dim);
					current.addOperation(op);
				}
				//				SearchSimpleVarVisitor v = new SearchSimpleVarVisitor();
				//				node.getIndex().accept(v);
				//				for(String varName : v.vars) {
				//					int dim = indexDepth(node);
				//					VariableOperation op = new VariableOperation(varName, VariableOperation.Type.INDEX, arrayRef, dim);
				//					current.addOperation(op);
				//				}
			}
			return true;
		}

		private String arrayRef(ArrayAccess node) {
			ArrayAccess a = node;
			while(a.getArray() instanceof ArrayAccess) {
				a = (ArrayAccess) a.getArray();
			}
			return a.getArray().toString();
		}

		private int indexDepth(ArrayAccess node) {
			int i = 0;
			ArrayAccess a = node;
			while(a.getArray() instanceof ArrayAccess) {
				a = (ArrayAccess) a.getArray();
				i++;
			}
			return i;
		}

		@Override
		public boolean visit(MethodInvocation node) {
			System.out.println("INV: " + node.getName() + " " + node.arguments());
			List arguments = node.arguments();
			for(int i = 0; i < arguments.size(); i++) {
				Object arg = arguments.get(i);
				if(arg instanceof SimpleName) {
					VariableInfo varInfo = current.getVariable(arg.toString());
					if(varInfo != null)
						varInfo.addOperation(VariableOperation.Type.PARAM, node.getName().toString(), i);
				}
			}
//			IMethodBinding method = node.resolveMethodBinding();
//			if(method == null)
//				return true;
//			ITypeBinding[] parameterTypes = method.getParameterTypes();
			return super.visit(node);
		}

		//		private void handleFor(ForStatement forStatement) {
		//			for(Object init : forStatement.initializers()) {
		//				VariableDeclarationExpression exp = (VariableDeclarationExpression) init;
		//				for(Object frag : exp.fragments()) {
		//					String varName = ((VariableDeclarationFragment) frag).getName().toString();
		//					current.addVar(varName);
		//				}
		//			}
		//
		//			for(Object up : forStatement.updaters()) {
		//				if(up instanceof PostfixExpression) {
		//					PostfixExpression post = (PostfixExpression) up;
		//					if(post.getOperand() instanceof SimpleName) {
		//						String varName = post.getOperand().toString();
		//						if(post.getOperator() == PostfixExpression.Operator.INCREMENT)
		//							current.addOperation(new VariableOperation(varName, VariableOperation.Type.INC));
		//						else if(post.getOperator() == PostfixExpression.Operator.DECREMENT)
		//							current.addOperation(new VariableOperation(varName, VariableOperation.Type.DEC));
		//					}
		//
		//				}
		//			}
		//		}


		//		class ProgressVisitor extends ASTVisitor {
		//			Set<String> vars = new HashSet<>();
		//
		//			@Override
		//			public boolean visit(PostfixExpression node) {
		//				if(node.getOperand() instanceof SimpleName) {
		//					String varName = node.getOperand().toString(); 
		//					VariableOperation op = null;
		//					if(node.getOperator() == PostfixExpression.Operator.INCREMENT)
		//						op = new VariableOperation(varName, VariableOperation.Type.INC);
		//
		//					else if(node.getOperator() == PostfixExpression.Operator.DECREMENT)
		//						op = new VariableOperation(varName, VariableOperation.Type.DEC);
		//
		//					if(op != null)
		//						current.addOperation(op);
		//				}
		//				return true;
		//
		//			}
		//
		//			@Override
		//			public boolean visit(Assignment node) {
		//				VariableOperation.Type op = null;
		//				if(node.getOperator() == Assignment.Operator.PLUS_ASSIGN && node.getRightHandSide() instanceof NumberLiteral || 
		//						node.getOperator() == Assignment.Operator.ASSIGN && isAcumulationLiteral(node, InfixExpression.Operator.PLUS))
		//					op = VariableOperation.Type.INC;
		//
		//				else if(node.getOperator() == Assignment.Operator.MINUS_ASSIGN && node.getRightHandSide() instanceof NumberLiteral ||
		//						node.getOperator() == Assignment.Operator.ASSIGN && isAcumulationLiteral(node, InfixExpression.Operator.PLUS))
		//					op = VariableOperation.Type.DEC;
		//
		//				if(op != null) {
		//					String varName = node.getLeftHandSide().toString(); 
		//					vars.add(varName);
		//					current.addOperation(new VariableOperation(varName, op));
		//				}
		//				return true;
		//			}
		//
		//			
		//		}

		//		@Override
		//		public boolean visit(ExpressionStatement node) {
		//			ASTNode parent = node.getParent();
		//			if(parent instanceof ForStatement) {
		//				
		//			}
		//			return true;
		//		}













	}

	private static boolean isAcumulationAssign(Assignment assignment, InfixExpression.Operator op, Predicate<Expression> acceptExpression) {
		if(!(
				assignment.getRightHandSide() instanceof InfixExpression && 
				assignment.getLeftHandSide() instanceof SimpleName &&
				assignment.getOperator() == Assignment.Operator.ASSIGN))
			return false;

		InfixExpression exp = (InfixExpression) assignment.getRightHandSide();
		if(exp.getOperator() != op)
			return false;

		String assignVar = assignment.getLeftHandSide().toString();
		if(	exp.getLeftOperand() instanceof SimpleName &&
				exp.getLeftOperand().toString().equals(assignVar) &&
				acceptExpression.test(exp.getRightOperand()))
			return true;

		if(	exp.getRightOperand() instanceof SimpleName && 
				exp.getRightOperand().toString().equals(assignVar) &&
				acceptExpression.test(exp.getLeftOperand()))
			return true;

		return false;
	}

	public void print() {
		for(BlockInfo b : visitor.roots)
			b.print();
	}




}
