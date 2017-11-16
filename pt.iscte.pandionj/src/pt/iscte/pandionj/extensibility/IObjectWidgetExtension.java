package pt.iscte.pandionj.extensibility;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.IObjectModel.InvocationResult;

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
			label.setForegroundColor(Constants.Colors.OBJECT_HEADER_FONT);
			FontManager.setFont(label, Constants.OBJECT_HEADER_FONT_SIZE);
			IType type = e.getType();
			if(type != null) {
				IMethod method = type.getMethod("toString", new String[0]);
				if(!method.exists()) {
					label.setText(":" + type.getElementName());
					return label;
				}
			}
			label.setToolTip(new Label("returned by toString()"));
			invokeToString(e, label);
			e.getRuntimeModel().registerDisplayObserver((event) -> {
				if(event.type == IRuntimeModel.Event.Type.STEP ||event.type == IRuntimeModel.Event.Type.EVALUATION)
					invokeToString(e, label);
			});
			return label;
		}

		private void invokeToString(IObjectModel e, Label label) {
			e.invoke("toString", new InvocationResult() { 
				public void valueReturn(Object o) {
					PandionJUI.executeUpdate(() -> {
						label.setText(o.toString());
					});
				}
			});
		}

		@Override
		public boolean includeMethod(String methodName) {
			return true;
		}
	};
}
