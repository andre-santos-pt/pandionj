package pt.iscte.pandionj.figures;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.draw2d.Label;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.model.NullModel;
import pt.iscte.pandionj.model.ReferenceModel;

public class ReferenceFigure extends Label {

	public ReferenceFigure(ReferenceModel model) {
		super(model.getName());
		if(model.isInstance())
			FontManager.setFont(this, Constants.VAR_FONT_SIZE, FontManager.Style.BOLD);
		else
			FontManager.setFont(this, Constants.VAR_FONT_SIZE);
		
		model.registerObserver(new Observer() {
			public void update(Observable o, Object target) {
				if(target instanceof NullModel)
					setToolTip(new Label("null reference"));
				else
					setToolTip(null);
			}
		});
	}
}
