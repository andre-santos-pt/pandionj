package pt.iscte.pandionj.extensibility;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Label;

import pt.iscte.pandionj.model.ArrayPrimitiveModel;

public class Histogram implements ArrayPrimitiveWidgetExtension {

	@Override
	public boolean qualifies(ArrayPrimitiveModel arrayModel) {

		return true; // TODO numeric / positive
	}

	@Override
	public Figure createFigure(ArrayPrimitiveModel model) {

		Figure fig = new Figure();
		fig.setLayoutManager(new FlowLayout());
		fig.setOpaque(true);
		fig.setBackgroundColor(ColorConstants.yellow);

		for(int i = 0; i < model.getLength(); i++) {
			Label label = new Label(model.getInt(i) + "");
			fig.add(label);
		}

		model.addObserver(new Observer() {
			
			@Override
			public void update(Observable o, Object arg) {
				fig.setBackgroundColor(ColorConstants.red);
			}
		});
		
		return fig;
	}

	@Override
	public boolean accept(Object[] array, int dimensions) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void positionChanged(Object oldValue, Object newValue, int i, int indexes) {
		// TODO Auto-generated method stub
		
	}

}
