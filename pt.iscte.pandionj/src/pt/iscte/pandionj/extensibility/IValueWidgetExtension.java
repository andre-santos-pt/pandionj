package pt.iscte.pandionj.extensibility;

import org.eclipse.draw2d.IFigure;

import pt.iscte.pandionj.figures.ValueFigure;

public interface IValueWidgetExtension extends IWidgetExtension<IValueModel>{
	boolean accept(IValueModel v);
	
	IValueWidgetExtension NULL_EXTENSION = new IValueWidgetExtension() {
		@Override
		public boolean accept(IValueModel v) {
			return true;
		}

		@Override
		public IFigure createFigure(IValueModel v, IPropertyProvider args) {
			return new ValueFigure(v);
		}
		
	};
}
