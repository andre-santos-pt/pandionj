package pt.iscte.pandionj.figures;

import java.util.Collection;

import org.eclipse.draw2d.Label;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.IReferenceModel;

public class ReferenceFigure extends PandionJFigure<IReferenceModel> {

	public ReferenceFigure(IReferenceModel model) {
		super(model);
		Label label = new Label(model.getName());
		if(model.isInstance())
			FontManager.setFont(label, Constants.VAR_FONT_SIZE, FontManager.Style.BOLD);
		else
			FontManager.setFont(label, Constants.VAR_FONT_SIZE);
		
		Collection<String> tags = model.getTags();
		if(!tags.isEmpty())
			label.setToolTip(new Label("tags: " + tags.toString()));
		
		add(label);
	}
}
