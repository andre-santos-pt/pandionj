package pt.iscte.pandionj.parser2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
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
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import pt.iscte.pandionj.parser.data.BlockInfo;
import pt.iscte.pandionj.parser.data.VariableInfo;
import pt.iscte.pandionj.parser.data.VariableOperation;
import pt.iscte.pandionj.parser.data.VariableOperation.Type;



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
	}


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
				VariableDeclarationFragment frag = (VariableDeclarationFragment) o;
				current.addVar(frag.getName().toString());
			}
			return true;
		}

		private boolean isLoopBlock(Block block) {
			ASTNode parent = block.getParent();
			return
					parent instanceof WhileStatement ||
					parent instanceof ForStatement ||
					parent instanceof DoStatement ||
					parent instanceof EnhancedForStatement;
		}

		@Override
		public boolean visit(Block node) {
			if(!isLoopBlock(node)) {
				current = createBlock(node.getParent());
				ASTNode parent = node.getParent();

				if(parent instanceof MethodDeclaration)
					handleMethodParams((MethodDeclaration) parent);
			}
			return true;
		}

		@Override
		public void endVisit(Block node) {
			if(!isLoopBlock(node))
				current = current.getParent();
		}

		private void handleMethodParams(MethodDeclaration node) {
			for(Object p : node.parameters()) {
				SingleVariableDeclaration var = (SingleVariableDeclaration) p;
				current.addVar(var.getName().getIdentifier());
			}
		}



		@Override
		public boolean visit(VariableDeclarationExpression node) {
			for(Object var : node.fragments()) {
				VariableDeclarationFragment frag = (VariableDeclarationFragment) var;
				current.addVar(frag.getName().toString());
			}
			return true;
		}


		@Override
		public boolean visit(VariableDeclarationStatement node) {
			for(Object var : node.fragments()) {
				VariableDeclarationFragment frag = (VariableDeclarationFragment) var;
				current.addVar(frag.getName().toString());
			}
			return true;
		}


		@Override
		public boolean visit(Assignment node) {
			if(node.getLeftHandSide() instanceof SimpleName) {
				String varName = node.getLeftHandSide().toString(); 
				VariableOperation.Type op = null;

				if(node.getOperator() == Assignment.Operator.PLUS_ASSIGN && node.getRightHandSide() instanceof NumberLiteral || 
						node.getOperator() == Assignment.Operator.ASSIGN && isAcumulationLiteral(node, InfixExpression.Operator.PLUS))
					op = VariableOperation.Type.INC;

				else if(node.getOperator() == Assignment.Operator.MINUS_ASSIGN && node.getRightHandSide() instanceof NumberLiteral ||
						node.getOperator() == Assignment.Operator.ASSIGN && isAcumulationLiteral(node, InfixExpression.Operator.PLUS))
					op = VariableOperation.Type.DEC;

				if(op != null)
					current.addOperation(new VariableOperation(varName, op));
				else {
					op = Type.SUBS;
					if(node.getOperator() == Assignment.Operator.PLUS_ASSIGN || node.getOperator() == Assignment.Operator.MINUS_ASSIGN)
						op = Type.ACC;
					
					current.addOperation(new VariableOperation(varName, op));
				}
			}
			return true;
		}

		@Override
		public boolean visit(PostfixExpression node) {
			if(node.getOperand() instanceof SimpleName) {
				String varName = node.getOperand().toString(); 
				VariableOperation op = null;
				if(node.getOperator() == PostfixExpression.Operator.INCREMENT)
					op = new VariableOperation(varName, VariableOperation.Type.INC);

				else if(node.getOperator() == PostfixExpression.Operator.DECREMENT)
					op = new VariableOperation(varName, VariableOperation.Type.DEC);

				if(op != null)
					current.addOperation(op);
			}
			return true;
		}





		//		private void handleWhile(WhileStatement whileStatement) {
		//			Expression expression = whileStatement.getExpression();
		//			ProgressVisitor progressVisitor = new ProgressVisitor();
		//			whileStatement.accept(progressVisitor);
		//			handleCondition(expression, progressVisitor.vars);
		//		}

		private void handleCondition(Expression expression) {
			Set<String> incVars = current.getVarsModified(VariableOperation.Type.INC);
			if(expression instanceof InfixExpression) {
				InfixExpression exp = (InfixExpression) expression;
				if(exp.getLeftOperand() instanceof SimpleName) {
					String var = exp.getLeftOperand().toString();
					if(incVars.contains(var)) {
						SearchVarVisitor v = new SearchVarVisitor();
						exp.getRightOperand().accept(v);
						if(v.vars.size() == 1) {
							String varName = v.vars.get(0);
							VariableOperation op = new VariableOperation(var, VariableOperation.Type.BOUNDED, varName);
							current.addOperation(op);
						}
					}
				}
				else if(exp.getRightOperand() instanceof SimpleName) {
					String var = exp.getRightOperand().toString();
					if(incVars.contains(var)) {
						SearchVarVisitor v = new SearchVarVisitor();
						exp.getLeftOperand().accept(v);
						if(v.vars.size() == 1) {
							String varName = v.vars.get(0);
							VariableOperation op = new VariableOperation(var, VariableOperation.Type.BOUNDED, varName);
							current.addOperation(op);
						}
					}
				}
			}
		}








		@Override
		public boolean visit(WhileStatement node) {
			current = createBlock(node);
			return true;
		}

		@Override
		public void endVisit(WhileStatement node) {
			handleCondition(node.getExpression());
			current = current.getParent();
		}



		@Override
		public boolean visit(ForStatement node) {
			current = createBlock(node);
			return true;
		}

		@Override
		public void endVisit(ForStatement node) {
			handleCondition(node.getExpression());
			current = current.getParent();
		}




		// TODO bug dimensions
		@Override
		public boolean visit(ArrayAccess node) {
			if(node.getArray() instanceof SimpleName || node.getArray() instanceof ArrayAccess) {
				String arrayRef = arrayRef(node.getArray());
				SearchVarVisitor v = new SearchVarVisitor();
				node.getIndex().accept(v);
				for(String varName : v.vars) {
					VariableOperation op = new VariableOperation(varName, VariableOperation.Type.INDEX, arrayRef, Integer.toString(indexDepth(node)));
					current.addOperation(op);
				}
			}

			return super.visit(node);
		}

		private String arrayRef(Expression node) {
			while(node.getParent() instanceof ArrayAccess) {
				node = (ArrayAccess) node.getParent();
			}
			return ((ArrayAccess) node).getArray().toString();
		}

		private int indexDepth(Expression node) {
			int i = 0;
			while(node.getParent() instanceof ArrayAccess) {
				i++;
				node = (ArrayAccess) node.getParent();
			}
			return i;
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












		private boolean containsVar(Expression expression, String varName) {
			SearchVarVisitor v = new SearchVarVisitor();
			expression.accept(v);
			return v.vars.contains(varName);
		}

		class SearchVarVisitor extends ASTVisitor {
			final List<String> vars = new ArrayList<>(5);
			@Override
			public boolean visit(SimpleName node) {
				vars.add(node.getIdentifier());
				return true;
			}
		}
	}

	private static boolean isAcumulationLiteral(Assignment assignment, InfixExpression.Operator op) {
		if(!(assignment.getRightHandSide() instanceof InfixExpression) || 
				!(assignment.getLeftHandSide() instanceof SimpleName))
			return false;

		InfixExpression exp = (InfixExpression) assignment.getRightHandSide();
		if(exp.getOperator() != op)
			return false;

		String assignVar = assignment.getLeftHandSide().toString();
		if(	exp.getLeftOperand() instanceof SimpleName &&
				exp.getLeftOperand().toString().equals(assignVar) &&
				exp.getRightOperand() instanceof NumberLiteral)
			return true;

		if(	exp.getRightOperand() instanceof SimpleName && 
				exp.getRightOperand().toString().equals(assignVar) &&
				exp.getLeftOperand() instanceof NumberLiteral)
			return true;

		return false;
	}




}
