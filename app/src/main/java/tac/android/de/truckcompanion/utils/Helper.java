package tac.android.de.truckcompanion.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.Nullable;
import com.google.common.io.ByteStreams;

import java.io.*;

/**
 * Created by Jonas Miederer.
 * Date: 08.05.16
 * Time: 16:40
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class Helper {
    public static int clip(int value, int lower, int upper) {
        return Math.max(lower, Math.min(upper, value));
    }

    @Nullable
    public static String getJsonStringFromAssets(Context context, String filename){
        String json = null;
        try{
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    /**
     * Copies files from assets to destination folder
     * @param assetManager
     * @param sourceFolder
     * @param destinationFolder
     * @throws IOException
     */
    public static void copyAssetsToFolder(AssetManager assetManager, String sourceFolder, String destinationFolder)
            throws IOException {
        final String[] assets = assetManager.list(sourceFolder);

        final File destFolderFile = new File(destinationFolder);
        if (!destFolderFile.exists()) {
            destFolderFile.mkdirs();
        }
        copyAsset(assetManager, sourceFolder, destinationFolder, assets);
    }

    /**
     * Copies files from assets to destination folder
     * @param assetManager
     * @param sourceFolder
     * @param destinationFolder
     * @param assetsNames
     * @throws IOException
     */
    public static void copyAsset(AssetManager assetManager, String sourceFolder, String destinationFolder,
                                 String... assetsNames) throws IOException {

        for (String assetName : assetsNames) {
            OutputStream destinationStream = new FileOutputStream(new File(destinationFolder + "/" + assetName));
            String[] files = assetManager.list(sourceFolder + "/" + assetName);
            if (files == null || files.length == 0) {

                InputStream asset = assetManager.open(sourceFolder + "/" + assetName);
                try {
                    ByteStreams.copy(asset, destinationStream);
                } finally {
                    asset.close();
                    destinationStream.close();
                }
            }
        }
    }

    /**
     * Copies files from assets to destination folder.
     * @param assetManager
     * @param assetName the asset that needs to be copied
     * @param destinationFolder path to folder where you want to store the asset
     * archive
     * @throws IOException
     */
    public static void copyAsset(AssetManager assetManager, String assetName, String destinationFolder)
            throws IOException {

        OutputStream destinationStream = new FileOutputStream(new File(destinationFolder + "/" + assetName));
        InputStream asset = assetManager.open(assetName);
        try {
            ByteStreams.copy(asset, destinationStream);
        } finally {
            asset.close();
            destinationStream.close();
        }
    }
}
