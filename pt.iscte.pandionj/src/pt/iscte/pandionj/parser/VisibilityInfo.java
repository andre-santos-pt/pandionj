package pt.iscte.pandionj.parser;

import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Modifier;

public enum VisibilityInfo {
		PUBLIC, PROTECTED, PACKAGE_PRIVATE, PRIVATE;
		
		public static VisibilityInfo from(BodyDeclaration node) {
			int mod = node.getModifiers();
			if(Modifier.isPublic(mod)) return PUBLIC;
			if(Modifier.isProtected(mod)) return PROTECTED;
			if(Modifier.isPrivate(mod)) return PRIVATE;
			else return PACKAGE_PRIVATE;
		}
	}