package pt.iscte.pandionj;


import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.draw2d.IFigure;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObservableModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.IValueModel;
import pt.iscte.pandionj.figures.ArrayPrimitiveFigure;
import pt.iscte.pandionj.figures.ArrayReferenceFigure;
import pt.iscte.pandionj.figures.NullFigure;
import pt.iscte.pandionj.figures.ObjectFigure;
import pt.iscte.pandionj.figures.PandionJFigure;
import pt.iscte.pandionj.figures.ReferenceFigure;
import pt.iscte.pandionj.figures.ValueFigure;

public class FigureProvider  {
	private Map<IObservableModel<?>, PandionJFigure<?>> figCache = new WeakHashMap<>();

	private IStackFrameModel stackFrame;

	public FigureProvider(IStackFrameModel stackFrame) {
		this.stackFrame = stackFrame;
	}

	public PandionJFigure<?> getFigure(Object element) {
		PandionJFigure<?> fig = figCache.get(element);
		if(fig == null) {
			IObservableModel<?> model = (IObservableModel<?>) element;
			//			fig = model.createFigure();
			fig = createFigure(model);
			figCache.put(model, fig);
		}
		return fig;		
	}

	private PandionJFigure<?> createFigure(IObservableModel<?> model) {
		PandionJFigure<?> fig = null;
		
		if(model instanceof IValueModel) {
			fig = new ValueFigure((IValueModel) model);
		}
		else if(model instanceof IReferenceModel) {
			fig = new ReferenceFigure((IReferenceModel) model);
		}
		else if(model instanceof IEntityModel) {
			IEntityModel entity = (IEntityModel) model;
			if(entity.isNull())
				fig = new NullFigure(entity);
			else {
				// TODO repor
//				Set<String> tags = getEntityTags(entity);
				Set<String> tags = Collections.emptySet();
				
				if(model instanceof IArrayModel) {
					IArrayModel aModel = (IArrayModel) model;
					IArrayWidgetExtension arrayExtension = ExtensionManager.getArrayExtension(aModel, tags);
					IFigure extFig = arrayExtension.createFigure(aModel);
					if(extFig == null) {
						if(aModel.isPrimitiveType()) {
							fig = new ArrayPrimitiveFigure(aModel);
						}
						else {
							fig = new ArrayReferenceFigure(aModel);
						}
					}
					else {
						fig = new PandionJFigure.Extension(extFig, model);
					}
				}
				else if(model instanceof IObjectModel) {
					IObjectModel oModel = (IObjectModel) model; 
					IFigure extensionFigure = ExtensionManager.getObjectExtension(oModel).createFigure(oModel);
					fig = new ObjectFigure(oModel, extensionFigure, true);
				}
			}
		}
		assert fig != null : model;
		return fig;
	}


	private Set<String> getEntityTags(IEntityModel e) {
		Set<String> tags = new HashSet<String>();
		for(IReferenceModel r : stackFrame.getReferencesTo(e))
			tags.addAll(r.getTags());
		return tags;
	}
}
