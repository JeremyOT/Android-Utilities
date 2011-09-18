package org.ext.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.res.AssetManager;

public final class FileUtilities {

    public static void copyAssetIfNecessary(String assetFile, String dest, AssetManager manager)
            throws IOException {
        if (!new File(dest).exists()) {
            copyAsset(assetFile, dest, manager);
        }
    }
    
	/**
	 * Copies the specified asset to the destination path. All files matching the pattern
	 * assetFile(-<number>) will be concatenated in lexical order, this feature allows large
	 * assets on older versions of Android that have a per-file size limit for asset files.
	 */
    public static void copyAsset(String assetFile, String dest, AssetManager manager)
            throws IOException {
        String assetPath = assetFile.lastIndexOf(File.pathSeparator) > -1 ? assetFile.substring(0,
                assetFile.lastIndexOf(File.pathSeparator)) : "";
        List<String> sourceFiles = new ArrayList<String>();
        for (String assetComponent : manager.list(assetPath)) {
            // only store paths that either match the path, or are a component of the desired asset
            // if over sized.
            if (assetComponent.matches(String.format("%s(-\\d+)?", assetFile))) {
                sourceFiles.add(FileUtilities.appendFilePath(assetPath, assetComponent));
            }
        }
        // Sort to make sure the components are read in order.
        Collections.sort(sourceFiles);

        new File(dest).getParentFile().mkdirs();

        FileOutputStream outStream = new FileOutputStream(dest);
        InputStream inStream = null;
        try {
            for (String component : sourceFiles) {
                inStream = manager.open(component);
                int read;
                byte[] buf = new byte[4096];
                while ((read = inStream.read(buf)) > 0)
                    outStream.write(buf, 0, read);
                inStream.close();
                inStream = null;
            }
        } finally {
            if (inStream != null)
                inStream.close();
            outStream.close();
        }
    }
    
    public static String appendFilePath(String path, String append) {
    	if(path.length() == 0)
    		return append;
        return new File(path, append).getPath();
    }

    private FileUtilities() {
        // Prevent instantiation, this is for static methods only
    }
}
