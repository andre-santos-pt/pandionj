package pt.iscte.pandionj.figures;

import org.eclipse.draw2d.Label;

import pt.iscte.pandionj.extensibility.IEntityModel;

public final class NullFigure extends PandionJFigure<IEntityModel> {
	public NullFigure(IEntityModel model) {
		super(model);
		Label label = new Label("  ");
		label.setOpaque(false);
		label.setSize(-1,-1);
		label.setToolTip(new Label("null"));
		add(label);
	}
}
