package pt.iscte.pandionj.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class Visitor extends ASTVisitor {

	public ClassInfo info;

	@Override
	public boolean visit(TypeDeclaration node) {
		if(info == null)
			info = new ClassInfo(node.resolveBinding().getQualifiedName(), VisibilityInfo.from(node));

		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		boolean instanceMember = !node.isConstructor() && !Modifier.isStatic(node.getModifiers());
		if(((TypeDeclaration) node.getParent()).isPackageMemberTypeDeclaration()) {
			AssignmentVisitor v = new AssignmentVisitor();
			node.accept(v);

			if(instanceMember) {
				MethodInfo m = new MethodInfo(
						node.getName().getIdentifier(), 
						VisibilityInfo.from(node), 
						node.getReturnType2().resolveBinding().isParameterizedType() ? Object.class.toString() : node.getReturnType2().resolveBinding().getQualifiedName(), 
								v.params,
								v.containsFieldAssignments);
				info.addMethod(m);
			}
		}
		return false;
	}


	private class AssignmentVisitor extends ASTVisitor {


		boolean containsFieldAssignments = false;
		List<String> params = new ArrayList<>();

		@Override
		public boolean visit(SingleVariableDeclaration node) {
			if(node.getParent() instanceof MethodDeclaration)
				params.add(node.resolveBinding().getType().getQualifiedName());


			return true;
		}

		public boolean visit(Assignment node) {
			Expression leftHandSide = node.getLeftHandSide();
			if(leftHandSide instanceof SimpleName)
				containsFieldAssignments = true;
			return true;
		}

		@Override
		public boolean visit(LineComment node) {
			int lineNumber = ((CompilationUnit)node.getRoot()).getLineNumber(node.getStartPosition());
			System.out.println(lineNumber + ":" + node);
			return super.visit(node);
		}
	}
}
