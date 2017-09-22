package pt.iscte.pandionj.figures;

import static pt.iscte.pandionj.Constants.OBJECT_PADDING;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;


import pt.iscte.pandionj.extensibility.IObservableModel;

public class PandionJFigure<T extends IObservableModel<?>> extends Figure {
	protected final T model;

	public PandionJFigure(T model, boolean includeMargin) {
		assert model != null;
		this.model = model;
		setBorder(includeMargin ? new MarginBorder(OBJECT_PADDING) : null);
		setLayoutManager(new GridLayout(2, false));
	}

	public T getModel() {
		return model;
	}
	
	public IFigure getInnerFigure() {
		return this;
	}

	public ConnectionAnchor getIncommingAnchor() {
		return new ChopboxAnchor(this);
	}
	
	public static class Extension extends PandionJFigure<IObservableModel<?>> {
		private final IFigure innerFigure;
		public Extension(IFigure innerFigure, IObservableModel<?> model) {
			super(model, true);
			this.innerFigure = innerFigure;
			GridLayout layout = new GridLayout();
			layout.marginWidth = OBJECT_PADDING;
			layout.marginHeight = OBJECT_PADDING;
			setLayoutManager(layout);
			add(innerFigure);
		}
		
		public IFigure getInnerFigure() {
			return innerFigure;
		}
		
//		public ConnectionAnchor getIncommingAnchor() {
//			return new ChopboxAnchor(innerFigure);
//			return innerFigure instanceof PandionJFigure ? ((PandionJFigure<?>) innerFigure).getIncommingAnchor() : new ChopboxAnchor(innerFigure);
//		}
	}
	
}
