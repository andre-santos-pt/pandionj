package pt.iscte.pandionj.extensibility;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.jdt.core.IType;

import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.IObjectModel.InvocationResult;

public interface ITypeWidgetExtension extends IWidgetExtension<IObjectModel> {

	boolean accept(IType objectType);

	default boolean includeMethod(String methodName) {
		return true;
	}

	IFigure ERROR = new Label("N/A");

	abstract class MatchName implements ITypeWidgetExtension {
		private final String fullQualifiedName;

		public MatchName(String fullQualifiedName) {
			this.fullQualifiedName = fullQualifiedName;
		}

		public boolean accept(IType objectType) {
			return objectType.getFullyQualifiedName().equals(fullQualifiedName);
		}
	}


	ITypeWidgetExtension NULL_EXTENSION = new ITypeWidgetExtension() {
		@Override
		public boolean accept(IType objectType) {
			return false;
		}

		@Override
		public IFigure createFigure(IObjectModel e, IPropertyProvider args) {
			Label label = new Label();
			label.setForegroundColor(PandionJConstants.Colors.OBJECT_HEADER_FONT);
			FontManager.setFont(label, PandionJConstants.OBJECT_HEADER_FONT_SIZE);
			IType type = e.getType();
			if(e.isToStringDefined() && !e.isLibraryClass()) {
				invokeToString(e, label);
				label.setToolTip(new Label("returned by toString()"));
				e.getRuntimeModel().registerDisplayObserver((event) -> {
					if(event.type == IRuntimeModel.Event.Type.STEP ||event.type == IRuntimeModel.Event.Type.EVALUATION) {
						invokeToString(e, label);
//						label.setText(e.getStringValue());
					}
				});
			}
			else {
				label.setText(":" + (type != null ? type.getElementName() : "") );
				return label;
			}
				
//			if(type != null) {
//				List<IMethod> visibleMethods = e.getVisibleMethods();
//				
//				IMethod method = type.getMethod("toString", new String[0]);
//				method.getParameterTypes()
//				if(!method.exists()) {
//					label.setText(":" + type.getElementName());
//					return label;
//				}
//			}
			
			return label;
		}

		private void invokeToString(IObjectModel e, Label label) {
			e.invoke("toString", new InvocationResult() { 
				public void valueReturn(Object o) {
					PandionJUI.executeUpdate(() -> {
						label.setText(o == null ? "null" : ((IObjectModel) o).getStringValue());
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
