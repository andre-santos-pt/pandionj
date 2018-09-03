package pt.iscte.pandionj;


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
import pt.iscte.pandionj.extensibility.IPropertyProvider;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.ITag;
import pt.iscte.pandionj.extensibility.IValueModel;
import pt.iscte.pandionj.extensibility.IValueWidgetExtension;
import pt.iscte.pandionj.extensibility.PandionJConstants;
import pt.iscte.pandionj.figures.ArrayPrimitiveFigure;
import pt.iscte.pandionj.figures.ArrayReferenceFigure;
import pt.iscte.pandionj.figures.IllustrationBorder;
import pt.iscte.pandionj.figures.ObjectFigure;
import pt.iscte.pandionj.figures.PandionJFigure;
import pt.iscte.pandionj.figures.ReferenceFigure;
import pt.iscte.pandionj.figures.ValueExtensionFigure;
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
			
			IValueWidgetExtension valueExtension = ExtensionManager.getValueExtension(v, v.getTag());
			IFigure f = valueExtension.createFigure(v, v.getTag());
			if(f instanceof ValueFigure)
				fig = (PandionJFigure<?>) f;
			else
				fig = new ValueExtensionFigure(v, f);
		}
		else if(model instanceof IReferenceModel) {
			fig = new ReferenceFigure((IReferenceModel) model);
		}
		else if(model instanceof IEntityModel) {
			IEntityModel entity = (IEntityModel) model;
			assert !entity.isNull();
			Set<ITag> tags = getEntityTags(entity);
			ITag tag = null;
			if(!tags.isEmpty())
				tag = tags.iterator().next();
			
			IPropertyProvider args = tag == null ? IPropertyProvider.NULL_PROPERTY_PROVIDER : tag;
			
			if(model instanceof IArrayModel) {
				IArrayModel aModel = (IArrayModel) model;
				// uses first tag of all tagged references, if available
				IFigure extFig = findExtensions && tag != null ? 
						ExtensionManager.getArrayExtension(aModel, tag).createFigure(aModel, args) : null;
				
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
				IFigure extensionFigure = ExtensionManager.getObjectExtension(oModel).createFigure(oModel, tag);
				fig = new ObjectFigure(oModel, extensionFigure);
			}
		}
		assert fig != null : model;
		return fig;
	}


	private Set<ITag> getEntityTags(IEntityModel e) {
		Set<ITag> tags = new HashSet<>();
		for(IReferenceModel r : runtime.findReferences(e))
			if(r.hasTag())
				tags.add(r.getTag());
		return tags;
	}

}
