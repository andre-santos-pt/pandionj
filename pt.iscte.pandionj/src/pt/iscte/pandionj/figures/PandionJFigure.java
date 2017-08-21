package pt.iscte.pandionj.figures;

import static pt.iscte.pandionj.Constants.OBJECT_PADDING;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.geometry.Dimension;

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

//	@Override
//	public Dimension getPreferredSize(int wHint, int hHint) {
//		System.out.println("* " + getLayoutManager().getPreferredSize(this, wHint, hHint));
//		return super.getPreferredSize(wHint, hHint).getExpanded(OBJECT_PADDING*2, OBJECT_PADDING*2);
//	}

	public static class Extension extends PandionJFigure<IObservableModel<?>> {
		public Extension(IFigure innerFigure, IObservableModel<?> model) {
			super(model);
			add(innerFigure);
		}
	}
}
