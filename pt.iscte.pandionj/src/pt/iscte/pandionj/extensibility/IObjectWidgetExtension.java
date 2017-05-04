package pt.iscte.pandionj.extensibility;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.core.IType;

public interface IObjectWidgetExtension extends IWidgetExtension<IObjectModel> {

	boolean accept(IType objectType);
	
	IFigure createFigure(IObjectModel e);
	
	default boolean includeMethod(String methodName) {
		return true;
	}
	
	
	abstract class MatchName implements IObjectWidgetExtension {
		private final String fullQualifiedName;
		
		public MatchName(String fullQualifiedName) {
			this.fullQualifiedName = fullQualifiedName;
		}
		
		public boolean accept(IType objectType) {
			return objectType.getFullyQualifiedName().equals(fullQualifiedName);
		}
	}
}
