package pt.iscte.pandionj.extensibility;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;

public class Histogram implements ArrayWidgetExtension {

	private List<Label> labels = new ArrayList<Label>();
	
	@Override
	public boolean accept(Object[] array, String type, int dimensions) {
		
		return false;
	}
	
	
	@Override
	public IFigure createFigure(Object[] array, String type, int dimensions) {

		Figure fig = new Figure();
		fig.setLayoutManager(new FlowLayout());
		fig.setOpaque(true);
		fig.setBackgroundColor(ColorConstants.yellow);

		for(int i = 0; i < array.length; i++) {
			Label label = new Label(array[i] + "");
			labels.add(label);
			fig.add(label);
		}
		
		return fig;
	}

	


}
