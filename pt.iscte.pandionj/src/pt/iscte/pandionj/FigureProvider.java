package pt.iscte.pandionj;


import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObservableModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IValueModel;
import pt.iscte.pandionj.extensibility.IValueWidgetExtension;
import pt.iscte.pandionj.extensibility.PandionJConstants;
import pt.iscte.pandionj.figures.ArrayPrimitiveFigure;
import pt.iscte.pandionj.figures.ArrayReferenceFigure;
import pt.iscte.pandionj.figures.IllustrationBorder;
import pt.iscte.pandionj.figures.ObjectFigure;
import pt.iscte.pandionj.figures.PandionJFigure;
import pt.iscte.pandionj.figures.ReferenceFigure;
import pt.iscte.pandionj.figures.ValueFigure;
import pt.iscte.pandionj.model.RuntimeModel;

public class FigureProvider  {
	private Map<IObservableModel<?>, PandionJFigure<?>> figCache = new WeakHashMap<>();
	private Map<IObservableModel<?>, PandionJFigure<?>> figCacheExtensions = new WeakHashMap<>();
	
	private RuntimeModel runtime;

	FigureProvider(RuntimeModel runtime) {
		assert runtime != null;
		this.runtime = runtime;
	}

	public PandionJFigure<?> getFigure(Object element, boolean findExtensions) {
		assert element != null;
		Map<IObservableModel<?>, PandionJFigure<?>>  map = findExtensions ? figCacheExtensions : figCache;
		PandionJFigure<?> fig = map.get(element);
		if(fig == null) {
			IObservableModel<?> model = (IObservableModel<?>) element;
			fig = createFigure(model, findExtensions);
			map.put(model, fig);
		}
		return fig;		
	}

	private PandionJFigure<?> createFigure(IObservableModel<?> model, boolean findExtensions) {
		PandionJFigure<?> fig = null;

		if(model instanceof IValueModel) {
			IValueModel v = (IValueModel) model;
			IValueWidgetExtension valueExtension = ExtensionManager.getValueExtension(v, v.getTags());
			fig = new PandionJFigure.Extension(valueExtension.createFigure(v), model);
//			fig = new ValueFigure((IValueModel) model);
		}
		else if(model instanceof IReferenceModel) {
			fig = new ReferenceFigure((IReferenceModel) model);
		}
		else if(model instanceof IEntityModel) {
			IEntityModel entity = (IEntityModel) model;
			assert !entity.isNull();
			Set<String> tags = getEntityTags(entity);

			if(model instanceof IArrayModel) {
				IArrayModel aModel = (IArrayModel) model;
				IFigure extFig = findExtensions ? 
						ExtensionManager.getArrayExtension(aModel, tags).createFigure(aModel) : null;
				
				if(extFig == null) {
					if(aModel.isPrimitiveType()) {
						fig = new ArrayPrimitiveFigure(aModel);
						fig.setBorder(new MarginBorder(IllustrationBorder.getInsets(fig, true)));
					}
					else {
						fig = new ArrayReferenceFigure(aModel);
						fig.setBorder(new MarginBorder(IllustrationBorder.getInsets(fig, true)));
					}
				}
				else {
					fig = new PandionJFigure.Extension(extFig, model);
					extFig.setBorder(new MarginBorder(PandionJConstants.OBJECT_PADDING));
				}
			}
			else if(model instanceof IObjectModel) {
				IObjectModel oModel = (IObjectModel) model; 
				IFigure extensionFigure = ExtensionManager.getObjectExtension(oModel).createFigure(oModel);
				fig = new ObjectFigure(oModel, extensionFigure);
			}
		}
		assert fig != null : model;
		return fig;
	}


	private Set<String> getEntityTags(IEntityModel e) {
		
		Set<String> tags = new HashSet<String>();
		for(IReferenceModel r : runtime.findReferences(e))
			tags.addAll(r.getTags());
		return tags;
	}

}
