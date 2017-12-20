package pt.iscte.pandionj.extensibility;

import org.eclipse.draw2d.IFigure;

import pt.iscte.pandionj.figures.ValueFigure;

public interface IValueWidgetExtension {
	boolean accept(IValueModel v);
	IFigure createFigure(IValueModel v);
	
	IValueWidgetExtension NULL_EXTENSION = new IValueWidgetExtension() {
		@Override
		public boolean accept(IValueModel v) {
			return true;
		}

		@Override
		public IFigure createFigure(IValueModel v) {
			return new ValueFigure(v);
		}
	};
}
