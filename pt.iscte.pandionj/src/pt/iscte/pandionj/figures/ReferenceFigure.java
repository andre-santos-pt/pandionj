package pt.iscte.pandionj.figures;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.model.NullModel;
import pt.iscte.pandionj.model.ReferenceModel;

public class ReferenceFigure extends Label {

	public ReferenceFigure(ReferenceModel model) {
		super(model.getName());
		setFont(new Font(null, Constants.FONT_FACE, Constants.VAR_FONT_SIZE, model.isInstance() ? SWT.BOLD : SWT.NONE));
		setBorder(new MarginBorder(Constants.OBJECT_PADDING));
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
