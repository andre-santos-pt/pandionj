package pt.iscte.pandionj.extensibility;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.jdt.core.IType;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;

public interface IObjectWidgetExtension extends IWidgetExtension<IObjectModel> {

	boolean accept(IType objectType);
	
	IFigure createFigure(IObjectModel e);
	
	default boolean includeMethod(String methodName) {
		return true;
	}
	
	IFigure ERROR = new Label("N/A");
	
	abstract class MatchName implements IObjectWidgetExtension {
		private final String fullQualifiedName;
		
		public MatchName(String fullQualifiedName) {
			this.fullQualifiedName = fullQualifiedName;
		}
		
		public boolean accept(IType objectType) {
			return objectType.getFullyQualifiedName().equals(fullQualifiedName);
		}
	}
	
	
	IObjectWidgetExtension NULL_EXTENSION = new IObjectWidgetExtension() {
		@Override
		public boolean accept(IType objectType) {
			return false;
		}

		@Override
		public IFigure createFigure(IObjectModel e) {
			Label label = new Label();
			label.setForegroundColor(Constants.OBJECT_HEADER_FONT_COLOR);
			FontManager.setFont(label, Constants.OBJECT_HEADER_FONT_SIZE);
			label.setText(e.toStringValue());
			return label;
		}
		
		@Override
		public boolean includeMethod(String methodName) {
			return true;
		}
	};
}
