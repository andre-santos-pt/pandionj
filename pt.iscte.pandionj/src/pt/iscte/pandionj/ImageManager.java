package pt.iscte.pandionj;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

import pt.iscte.pandionj.extensibility.PandionJConstants;

public class ImageManager {
	private static final List<Image> images = new ArrayList<>();
	
	public static Image getImage(String name) {
		Bundle bundle = Platform.getBundle(PandionJConstants.PLUGIN_ID);
		URL imagePath = FileLocator.find(bundle, new Path(PandionJConstants.IMAGE_FOLDER + "/" + name), null);
		ImageDescriptor imageDesc = ImageDescriptor.createFromURL(imagePath);
		Image img = imageDesc.createImage();
		images.add(img);
		return img;
	}
	
	public static void disposeAll() {
		images.forEach(i -> i.dispose());
	}
}
