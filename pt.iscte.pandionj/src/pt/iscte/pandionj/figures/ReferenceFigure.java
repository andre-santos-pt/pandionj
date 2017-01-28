package pt.iscte.pandionj.figures;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

import pt.iscte.pandionj.model.ReferenceModel;

public class ReferenceFigure extends Label {

	public ReferenceFigure(ReferenceModel model) {
		super(model.getName());
		setFont(new Font(null, "Arial", 16, SWT.NONE));
		setBorder(new MarginBorder(10));
	}
}
