package pt.iscte.pandionj.figures;

import static pt.iscte.pandionj.Constants.OBJECT_PADDING;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;


import pt.iscte.pandionj.extensibility.IObservableModel;

public class PandionJFigure<T extends IObservableModel<?>> extends Figure {
	protected final T model;

	public PandionJFigure(T model) {
		assert model != null;
		this.model = model;
		setBorder(new MarginBorder(OBJECT_PADDING));
//		setBorder(new LineBorder(ColorConstants.blue, 1));
		setLayoutManager(new GridLayout());
//		setBackgroundColor(ColorConstants.yellow);
//		setOpaque(true);
	}

	public T getModel() {
		return model;
	}
	
	public IFigure getInnerFigure() {
		return this;
	}

	public static class Extension extends PandionJFigure<IObservableModel<?>> {
		private final IFigure innerFigure;
		public Extension(IFigure innerFigure, IObservableModel<?> model) {
			super(model);
			this.innerFigure = innerFigure;
			add(innerFigure);
		}
		
		public IFigure getInnerFigure() {
			return innerFigure;
		}
	}
	
}
