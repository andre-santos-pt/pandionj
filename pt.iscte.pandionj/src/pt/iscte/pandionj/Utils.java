package pt.iscte.pandionj;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;

import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.extensibility.PandionJConstants;

public interface Utils {

	static String toSimpleName(String qualifiedName) {
		String name = qualifiedName;
		int dot = name.lastIndexOf('.');
		if(dot != -1) 
			name = name.substring(dot+1);

		int dollar = name.lastIndexOf('$');
		if(dollar != -1)
			name = name.substring(dollar+1);

		name = name.replaceFirst("", "");
		return name;
	}

	static void stripQualifiedNames(List<String> list) {
		for(int i = 0; i < list.size(); i++)
			list.set(i, toSimpleName(list.get(i)));
	}

	static void addArrowDecoration(PolylineConnection pointer) {
		PolylineDecoration decoration = new PolylineDecoration();
		PointList points = new PointList();
		points.addPoint(-1, -1);
		points.addPoint(0, 0);
		points.addPoint(-1, 1);
		decoration.setTemplate(points);
		decoration.setScale(PandionJConstants.ARROW_EDGE, PandionJConstants.ARROW_EDGE);
		decoration.setLineWidth(PandionJConstants.ARROW_LINE_WIDTH);
		decoration.setOpaque(true);
		pointer.setTargetDecoration(decoration);
	}

	//	private static void addNullDecoration(PolylineConnection pointer) {
	//	PolygonDecoration decoration = new PolygonDecoration();
	//	PointList points = new PointList();
	//	points.addPoint(0,-1); // 1
	//	points.addPoint(0, 1); // -1
	//	decoration.setTemplate(points);
	//	decoration.setScale(Constants.ARROW_EDGE, Constants.ARROW_EDGE);
	//	decoration.setLineWidth(Constants.ARROW_LINE_WIDTH);
	//	decoration.setOpaque(true);
	//	pointer.setTargetDecoration(decoration);	
	//}
	
	static String getTooltip(IVariableModel model) {
		IJavaVariable javaVariable = model.getJavaVariable();
		String owner = null;
		if(javaVariable instanceof IJavaFieldVariable)
			try {
				owner = ((IJavaFieldVariable) javaVariable).getDeclaringType().getName();
			} catch (DebugException e) { }
		String tooltip = model.isStatic() ? "static field" : (owner == null ? "local variable" : "field");
		
		if(model.isStatic() && owner != null)
			tooltip += " of " + owner;
		
		tooltip += " (" + model.getTypeName() + ")";
		return tooltip;
	}
	

	static void saveImageToPNGandDispose(Image image, IFile file) {
		ImageLoader imageLoader = new ImageLoader();
		imageLoader.data = new ImageData[] {image.getImageData()};
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		imageLoader.save(result, SWT.IMAGE_PNG);
		InputStream source = new ByteArrayInputStream(result.toByteArray());
		try {
			if(file.exists())
				file.delete(true, new NullProgressMonitor());
			file.create(source, true, new NullProgressMonitor());
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		image.dispose();
	}
	
	static void copyToClipBoardAndDispose(Image image) {
		Clipboard clipboard = new Clipboard(Display.getDefault());
		clipboard.setContents(new Object[]{image.getImageData()}, new Transfer[]{ ImageTransfer.getInstance()}); 
		image.dispose();
	}
	
	static void sendEmail(String subject, StringBuffer buf) {
		String text = buf.toString();
		try {
			text = URLEncoder.encode(text, "UTF-8").replace("+", "%20");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException();
		}
		Program.launch("mailto:" + PandionJConstants.ERROR_REPORT_MAIL + "?subject=" + subject + "&body=" + text);
	}
}
