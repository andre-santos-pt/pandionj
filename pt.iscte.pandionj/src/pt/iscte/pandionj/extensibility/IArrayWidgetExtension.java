package pt.iscte.pandionj.extensibility;

import org.eclipse.draw2d.IFigure;


public interface IArrayWidgetExtension extends IWidgetExtension<IArrayModel<?>> {

	boolean accept(IArrayModel<?> e);
	
	IArrayWidgetExtension NULL_EXTENSION = new IArrayWidgetExtension() {
		@Override
		public boolean accept(IArrayModel<?> e) {
			return false;
		}

		@Override
		public IFigure createFigure(IArrayModel<?> e, IPropertyProvider args) {
			return null;
		}		
	};
}
