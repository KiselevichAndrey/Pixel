package com.pixelfunapp.pixelpart;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableRow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ImageAdapter {
    private static final int MAX_SIDE_COLORER = 40;
    private static final int IMAGE_DP = 112;
    private static final int MARGIN_DP = 0;
    private static final String COLORING_DIRECTORY_NAME = "Files/coloring";

    static LinkedList<ImageAdapter> AllImageAdapterList;
//    static LinkedList<ImageAdapter> FavoriteList;
    static HashMap<String, LinkedList<ImageAdapter>> CategoryDict;
    private String mFullPathToOriginal;

    public boolean mIsWorkStarted;
    public boolean mIsFavorite;
    public boolean mIsFinished;

    public ViewGroup mCategoryIcon, mWorkedIcon, mFavoriteIcon;
    private ImageView mCategoryMainImage, mCategorySubImage;
    private ImageView mFavoriteMainImage, mFavoriteSubImage;
    private ImageView mWorkedMainImage;


    private Bitmap mBitmap;
    static private TableRow.LayoutParams tableLayoutParams;
    private String mColoringPNGFileName;
    private String mCategoryName;
    private LinkedList<Integer> mColoredPixelsPairs;
    public int pxsCount, colorsCount;
    public static ImageAdapter currentColoringImageAdapter;

//    private View.OnClickListener mainImageGaleryClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//
//        }
//    };

    private ImageAdapter getImageAdapterByMainImageView(View v) {
        for (ImageAdapter ia: AllImageAdapterList) { if (ia.checkThisIsMainImageView(v)) return ia; }
        return null;
    }

    public int getColoredPixelsCount() {
        return mColoredPixelsPairs.size()/2;
    }


    public void startColoring() {
        if (!mIsWorkStarted) setWorkStarted();
        //todo: need rebuild this...!
        mColoredPixelsPairs = new LinkedList<>();
        String wrkContents = readFromFileByPathToString(getWorkedFullFilePath());
        String[] lines = wrkContents.split("\n");
        for (String s : Arrays.asList(lines).subList(1, lines.length)
                ) {
            mColoredPixelsPairs.add(Integer.parseInt(s.split(",")[0]));
            mColoredPixelsPairs.add(Integer.parseInt(s.split(",")[1]));
        }

        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra("coloredPixels", mColoredPixelsPairs);
        intent.putExtra("fullPathWRK", getWorkedFullFilePath());
        intent.putExtra("fullPathFIN", getFinishedFullFilePath());
        intent.putExtra("fullPathToColoring", getFullFilePathForColoring());
        getActivity().startActivityForResult(intent, ScreenOne.COLORING_REQUEST_CODE);
    }

    ImageAdapter(Activity activity, String coloringName, String category) {
        LayoutInflater inflater = activity.getLayoutInflater();
        initTableLayoutParams(inflater);
        mColoringPNGFileName = coloringName + ".png";
        mCategoryName = category;
        mIsWorkStarted = false;
        mIsFinished = false;
        mIsFavorite = false;

        mCategoryIcon = (ViewGroup)inflater.inflate(R.layout.image_icon, null);
        mCategoryIcon.setLayoutParams(tableLayoutParams);
        mCategoryMainImage = mCategoryIcon.findViewById(R.id.main_image);
        mCategoryMainImage.setOnClickListener(new MainImageGalleryClickListener(this, (ScreenOne) getActivity()));
        mCategorySubImage = mCategoryIcon.findViewById(R.id.sub_image);
        mCategorySubImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsFavorite) setFavorite();
            }
        });

        mFavoriteIcon = (ViewGroup) inflater.inflate(R.layout.image_icon, null);
        mFavoriteIcon.setLayoutParams(tableLayoutParams);
        mFavoriteMainImage = mFavoriteIcon.findViewById(R.id.main_image);
        mFavoriteMainImage.setOnClickListener(new MainImageGalleryClickListener(this, (ScreenOne) getActivity()));
        mFavoriteSubImage = mFavoriteIcon.findViewById(R.id.sub_image);
        mFavoriteSubImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsFavorite) unsetFavorite();
            }
        });

        mWorkedIcon = (ViewGroup) inflater.inflate(R.layout.image_icon, null);
        mWorkedIcon.setLayoutParams(tableLayoutParams);
        mWorkedMainImage = mWorkedIcon.findViewById(R.id.main_image);
        mWorkedMainImage.setOnClickListener(new MainImageGalleryClickListener(this, (ScreenOne) getActivity()));
        //hide subImage
        mWorkedIcon.findViewById(R.id.sub_image).setVisibility(View.GONE);

        //?
        mFavoriteMainImage.setImageBitmap(mBitmap);
        mFavoriteSubImage.setImageResource(R.drawable.fav_remove_btn);
        mWorkedMainImage.setImageBitmap(mBitmap);
        //

//        if (FavoriteList == null) FavoriteList = new LinkedList<>();
        if (CategoryDict == null) CategoryDict = new HashMap<>();
        if (AllImageAdapterList == null) AllImageAdapterList = new LinkedList<>();
        LinkedList<ImageAdapter> ll = CategoryDict.get(mCategoryName);
        if (ll == null) {
            ll = new LinkedList<>();
            ll.add(this);
            CategoryDict.put(mCategoryName, ll);
        } else {
            ll.add(this);
        }
        AllImageAdapterList.add(this);
    }

    private void initTableLayoutParams (LayoutInflater inflater) {
        if (tableLayoutParams != null) return;
        //Set width, height for "image"
        float scaleDPMultiply = inflater.getContext().getResources().getDisplayMetrics().density;
        int imagePX = (int)(IMAGE_DP * scaleDPMultiply);
        tableLayoutParams = new TableRow.LayoutParams(imagePX, imagePX);
        //Set margin for "image'
        int marginPX = (int)(MARGIN_DP * scaleDPMultiply);
        tableLayoutParams.setMargins(marginPX, marginPX, marginPX, marginPX);
    }

    public void setFavorite() {
        mCategorySubImage.setImageResource(R.drawable.fav);
        mIsFavorite = true;
        touchFileByPathIfNotExist(getFavoriteFullFilePath());

        ((ScreenOne) getActivity()).mFavorites.addToCategory(this);
    }

    public void unsetFavorite() {
        mCategorySubImage.setImageResource(R.drawable.fav_add_btn);
        mIsFavorite = false;
        ((ScreenOne) getActivity()).mFavorites.removeFromCategory(this);
        removeFileByPathIfExist(getFavoriteFullFilePath());
    }

    private void touchFileByPathIfNotExist(String path) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                if(!file.createNewFile()) {
                    Log.e("touchFile", "Can't create file");
                }
            } catch (IOException e) {
                Log.e("touchFile", "IOException! Can't create file");
            }
        }
    }

    private void removeFileByPathIfExist(String path) {
        File file = new File(path);
        if (file.exists()) {
            if (!file.delete()) {
                Log.e("removeFile", "Can't delete file");
            }
        }
    }

    private void setFavoriteByFile() {if (isExistFavoriteFile()) setFavorite();}

    private boolean isExistFinishedFile() {return new File(getFinishedFullFilePath()).exists();}

    private boolean isExistFavoriteFile() {return new File(getFavoriteFullFilePath()).exists();}

    private boolean isExistConfigFile() {return new File(getConfigFullFilePath()).exists();}

    private boolean isExistOriginalFile() {return new File(mFullPathToOriginal).exists();}

    private boolean isExistLittleFile() {return new File(getLittleFullFilePath()).exists();}

    private void unsetFinished() {
        removeFileByPathIfExist(getFinishedFullFilePath());
        mIsFinished = false;
        ((ScreenOne) getActivity()).mFinished.removeFromCategory(this);
    }

    private void setWorkStarted () {
        mColoredPixelsPairs = new LinkedList<>();
        mIsWorkStarted = true;
        //create .wrk file
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(getFullFilePathForColoring(), options);
        int workspaceWidth = options.outWidth;
        int workspaceHeight = options.outHeight;
        String line = String.valueOf(workspaceWidth) + "," + String.valueOf(workspaceHeight) + "\n";
        try {
            File wrk = new File(getWorkedFullFilePath());
            FileOutputStream fos = new FileOutputStream(wrk);
            fos.write(line.getBytes());
            fos.close();
        } catch (IOException e) {
            Log.e("setWorkStarted", "IOException! Can't create file *.wrk");
        }

        ((ScreenOne) getActivity()).mInProcess.addToCategory(this);
    }

    public void unsetWorkStarted() {
        mColoredPixelsPairs = new LinkedList<>();
        mIsWorkStarted = false;
        //remove .wrk file getWorkedFullFilePath()
        removeFileByPathIfExist(getWorkedFullFilePath());

        ((ScreenOne) getActivity()).mInProcess.removeFromCategory(this);
        setNewIconByProgressColoring();
    }

    private void loadWork() {
        setColoredPixelsPairs();

        //mWorkedIcon
        mWorkedIcon = (ViewGroup)getActivity().getLayoutInflater().inflate(R.layout.image_icon, null);
        mWorkedIcon.setLayoutParams(tableLayoutParams);
        //mainImage
        ImageView workedMainImage = mWorkedIcon.findViewById(R.id.main_image);
        workedMainImage.setOnClickListener(new MainImageGalleryClickListener(this, (ScreenOne) getActivity()));
        workedMainImage.setImageBitmap(mBitmap);
        //subImage
        ImageView subImage = mWorkedIcon.findViewById(R.id.sub_image);
        subImage.setImageBitmap(null);

        mIsWorkStarted = true;
    }

    private void loadWorkByFile() {
        if (mIsWorkedByFile()) loadWork();
    }

    private boolean mIsWorkedByFile() {
        return new File(getWorkedFullFilePath()).exists();
    }

    public CategoryAdapter getCategoryAdapterByIcon(View v) {
        if (mCategoryMainImage == v) return CategoryAdapter.getNotSystemCategoryAdapterByImageAdapter(this);

        ImageView favoriteViewMainImage = null;
        if (mFavoriteIcon != null)
            favoriteViewMainImage = mFavoriteIcon.findViewById(R.id.main_image);
        if (favoriteViewMainImage == v) return ((ScreenOne) getActivity()).mFavorites;

        ImageView workedViewMainImage = null;
        if (mWorkedIcon != null) workedViewMainImage = mWorkedIcon.findViewById(R.id.main_image);
        if (workedViewMainImage == v) {
            if (mIsFinished) return ((ScreenOne) getActivity()).mFinished;
            else return ((ScreenOne) getActivity()).mInProcess;
        }
        return null;
    }

    public ViewGroup getIconForCategoryAdapter(CategoryAdapter categoryAdapter) {
        ScreenOne activity = ((ScreenOne) getActivity());
        if (categoryAdapter == activity.mInProcess ||
                categoryAdapter == activity.mFinished) return mWorkedIcon;
        if (categoryAdapter == activity.mFavorites) return mFavoriteIcon;
        return mCategoryIcon;
    }

    public void addPixelsPairsToWork(LinkedList<Integer> pixelPairs) {
        //add pairs to .wrk file
        File wrk = new File(getWorkedFullFilePath());
        try {
            FileOutputStream fos = new FileOutputStream(wrk, true);
            int x = -1, y = -1;
            boolean isX = true;
            for (Integer i: pixelPairs
                 ) {
                if (isX) {
                    x = i;
                    isX = false;
                } else {
                    y = i;
                    fos.write((String.valueOf(x) + "," + String.valueOf(y) + "\n").getBytes());
                    isX = true;
                }
            }
            fos.close();
        } catch (IOException e) {
            Log.e("addPixelsPairsToWork", "IOException! Can't append to file *.wrk");
        }
    }

    private boolean checkThisIsMainImageView(View v) {
        if (mCategoryMainImage == v) return true;

        ImageView favoriteViewMainImage = null;
        if (mFavoriteIcon != null) favoriteViewMainImage = mFavoriteIcon.findViewById(R.id.main_image);
        if (favoriteViewMainImage == v) return true;

        ImageView workedViewMainImage = null;
        if (mWorkedIcon != null) workedViewMainImage = mWorkedIcon.findViewById(R.id.main_image);
        if (workedViewMainImage == v) return true;
        return false;
    }

    private Activity getActivity() {
        Context context = mCategoryIcon.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

    private String getColoringName() {
        return mColoringPNGFileName.substring(0, mColoringPNGFileName.length()-".png".length());
    }

    private String generateFullPathToCayegoryDirectory() {
        return Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + mCategoryIcon.getContext().getPackageName()
                + "/" + COLORING_DIRECTORY_NAME + "/" + mCategoryName;
    }

    public String getColoringFolderPath() {
        return Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + mCategoryIcon.getContext().getPackageName()
                + "/" + COLORING_DIRECTORY_NAME + "/";
    }

    private String getOriginalFullFilePath() {
        return generateFullPathToCayegoryDirectory()
                + "/" + mColoringPNGFileName;
    }

    private String getLittleFullFilePath() {
        return generateFullPathToCayegoryDirectory()
                + "/" + getColoringName() + ".ltl";
    }

    private String getConfigFullFilePath() {
        return generateFullPathToCayegoryDirectory()
                + "/" + getColoringName() + ".cfg";
    }

    private String getFullFilePathForColoring() {
        if (isExistLittleFile()) return getLittleFullFilePath();
        else return getOriginalFullFilePath();
    }

    private String getFavoriteFullFilePath() {
        return generateFullPathToCayegoryDirectory()
                + "/" + getColoringName() + ".fav";
    }

    private String getWorkedFullFilePath() {
        return generateFullPathToCayegoryDirectory()
                + "/" + getColoringName() + ".wrk";
    }

    private String getFinishedFullFilePath() {
        return generateFullPathToCayegoryDirectory()
                + "/" + getColoringName() + ".fin";
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public void initProgressOfColoring() {
        loadWorkByFile();
        setFavoriteByFile();
        setNewIconByProgressColoring();
        setPXsCountAndColorCountFromConfigFile();
        setIsFinishedByFile();
    }

    public void setImageByPNGFile(File png) {
        mColoringPNGFileName = png.getName();
        //mBitmap ! SCALE VERY BIG IMAGE
        BitmapFactory.Options options = new BitmapFactory.Options();
        mBitmap = BitmapFactory.decodeFile(png.getPath());
        int sampleSize = Math.max(mBitmap.getWidth(), mBitmap.getHeight()) / 320;
        if (sampleSize == 0) sampleSize = 1;
        options.inSampleSize = sampleSize;
        mBitmap = BitmapFactory.decodeFile(png.getPath());

        //Create .ltl if needed
        mFullPathToOriginal = saveImage(mBitmap, mColoringPNGFileName, mCategoryName);
        if (mBitmap.getWidth() > MAX_SIDE_COLORER || mBitmap.getHeight() > MAX_SIDE_COLORER) {
            //LittleImage
            mBitmap = createLittleImage(mBitmap);
            saveImage(mBitmap, getColoringName() + ".ltl", mCategoryName);
        }
    }

    public void setImageByResourceId(int resource) {
        //bitmap ! SCALE VERY BIG IMAGE
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 2;
        Bitmap bitmap = BitmapFactory.decodeResource(mCategoryIcon.getResources(), resource);
        int maxSide = Math.max(bitmap.getHeight(), bitmap.getWidth());
        if(maxSide > MAX_SIDE_COLORER) bitmap = Pixel.trimAlpha(bitmap);
        if (maxSide > 320) {
            float scale = (float)(maxSide / 320.0);
            int width = (int)(bitmap.getWidth() / scale);
            int height = (int)(bitmap.getWidth() / scale);
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        }
        mFullPathToOriginal = getOriginalFilePath(mColoringPNGFileName, mCategoryName);
        if (!isExistOriginalFile()) mFullPathToOriginal = saveImage(bitmap, mColoringPNGFileName, mCategoryName);

        //Create .ltl if needed
        if (bitmap.getWidth() > MAX_SIDE_COLORER || bitmap.getHeight() > MAX_SIDE_COLORER) {
            //LittleImage
            bitmap = createLittleImage(bitmap);
            saveImage(bitmap, getColoringName() + ".ltl", mCategoryName);
        }
    }

    private Bitmap createLittleImage(Bitmap bitmap) {
        Bitmap image = Pixel.trimAlpha(bitmap);
        Bitmap littleImage;
        int maxSide = Math.max(image.getWidth(), image.getHeight());
        int width = image.getWidth(), height = image.getHeight();
        if (maxSide > MAX_SIDE_COLORER) {
            float scale = ((float)maxSide) / ((float) MAX_SIDE_COLORER);
            width = (int) (image.getWidth() / scale);
            height = (int) (image.getHeight() / scale);
        }
        littleImage = Bitmap.createScaledBitmap(image, width, height, false);
        return littleImage;
    }

    private Bitmap getIconImage() {
        if (!isExistConfigFile()) createConfigForImageByFullFilePath();
        if (mIsWorkStarted) return createWorkedIconImageByConfig();
        else return createStartedIconImageByConfig();
    }

    private void setNewIconByProgressColoring() {
        mBitmap = getIconImage();
        ((ImageView)mCategoryIcon.findViewById(R.id.main_image)).setImageBitmap(mBitmap);
        ((ImageView)mFavoriteIcon.findViewById(R.id.main_image)).setImageBitmap(mBitmap);
        ((ImageView)mWorkedIcon.findViewById(R.id.main_image)).setImageBitmap(mBitmap);
    }

    private void createConfigForImageByFullFilePath() {
        Bitmap bitmap = BitmapFactory.decodeFile(getFullFilePathForColoring());
        createConfigFileForImageInPath(bitmap, getConfigFullFilePath());
    }

    private Bitmap createWorkedIconImageByConfig() {
        String cfgContent = readFromFileByPathToString(getConfigFullFilePath());
        String[] lines = cfgContent.split("\n");
        int width = Integer.parseInt(lines[0].split(",")[0]);
        int height = Integer.parseInt(lines[0].split(",")[1]);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Pixel.config);
        LinkedList<String> pairsStrings = convertPairsToStrings(mColoredPixelsPairs);
        LinkedList<Integer> colors = new LinkedList<Integer>();
        for (String s: lines[1].split(",")) {
            colors.add(Integer.parseInt(s));
        }
        for (String s: Arrays.asList(lines).subList(2,lines.length)
                ) {
            int x = Integer.parseInt(s.split(",")[0]);
            int y = Integer.parseInt(s.split(",")[1]);
            int colorNumber = Integer.parseInt(s.split(",")[2]);
            //gray filtert if not in mColoredPixelsPairs
            int pixelColor;
            if (pairsStrings.contains(s.split(",")[0] + "," + s.split(",")[1])) pixelColor = colors.get(colorNumber - 1);
            else pixelColor = convertToGrayShade(colors.get(colorNumber - 1));
            bitmap.setPixel(x, y, pixelColor);
        }
        return bitmap;
    }

    private Bitmap createStartedIconImageByConfig() {
        String cfgContent = readFromFileByPathToString(getConfigFullFilePath());
        String[] lines = cfgContent.split("\n");
        int width = Integer.parseInt(lines[0].split(",")[0]);
        int height = Integer.parseInt(lines[0].split(",")[1]);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Pixel.config);
        LinkedList<Integer> colors = new LinkedList<Integer>();
        for (String s: lines[1].split(",")) {
            colors.add(Integer.parseInt(s));
        }
        for (String s: Arrays.asList(lines).subList(2,lines.length)
                ) {
            int x = Integer.parseInt(s.split(",")[0]);
            int y = Integer.parseInt(s.split(",")[1]);
            int colorNumber = Integer.parseInt(s.split(",")[2]);
            //gray filtert
            int grayShade = convertToGrayShade(colors.get(colorNumber - 1));
            bitmap.setPixel(x, y, grayShade);
        }
        return bitmap;
    }

    private void setPXsCountAndColorCountFromConfigFile() {
        String configContent = readFromFileByPathToString(getConfigFullFilePath());
        String firstLine = configContent.split("\n")[0];
        String secondLine = configContent.split("\n")[1];
        pxsCount = Integer.parseInt(firstLine.split(",")[2]);
        colorsCount = secondLine.split(",").length;
    }

    private void setColoredPixelsPairs () {
        mColoredPixelsPairs = new LinkedList<>();
        String wrkContents = readFromFileByPathToString(getWorkedFullFilePath());
        String[] lines = wrkContents.split("\n");
        for (String s : Arrays.asList(lines).subList(1, lines.length)) {
            mColoredPixelsPairs.add(Integer.parseInt(s.split(",")[0]));
            mColoredPixelsPairs.add(Integer.parseInt(s.split(",")[1]));
        }
    }

    public void setIsFinishedByFile() {
        mIsFinished = isExistFinishedFile();
    }

    private String readFromFileByPathToString(String path) {
        File file = new File(path);
        int bufferSize = (int) file.length();
        byte[] buffer = new byte[bufferSize];
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            try {
                fin.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                fin.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(buffer);
    }

    private void createConfigFileForImageInPath(Bitmap bitmap, String configPath) {
        List<Palette.Swatch> swatchList = Palette.from(bitmap).generate().getSwatches();
        LinkedList<Integer> colors = new LinkedList<Integer>();
        for (Palette.Swatch s: swatchList
                ) {
            int color = s.getRgb();
            if (!MainActivity.colorIsNearByColors(color, colors, MainActivity.MAX_DIFF_BETWEEN_COLORS)) {
                colors.add(color);
            }
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int pxsCount = 0;
        //First line, append width,height,pxsCount line
        String firstLineWithOutEnd = String.valueOf(width) + "," + String.valueOf(height) + ",";

        //Append to CFG colors line and x,y,colorNumber lines
        File cfg = new File(configPath);
        StringBuilder sb = new StringBuilder();
        //colors line
        for (int c: colors) {
            sb.append(c);
            if (c != colors.getLast()) sb.append(",");
        }
        sb.append("\n");
        //x,y,colorNumber lines
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixelColor = bitmap.getPixel(x, y);
                int pixelColorNumber = Pixel.definiteColorNumberInColors(pixelColor, colors);
                if (pixelColorNumber != -1) {
                    sb.append(x).append(",").append(y).append(",").append(pixelColorNumber).append("\n");
                    pxsCount++;
                }
            }
        }
        try {
            FileOutputStream fos = new FileOutputStream(cfg, true);
            fos.write((firstLineWithOutEnd + String.valueOf(pxsCount) + "\n").getBytes());
            fos.write(sb.toString().getBytes());
            fos.close();
        } catch (IOException e) {
            Log.e("createStartedLittleImag", "IOException! Can't append to file *.cfg");
        }
    }

    private static int convertToGrayShade(int color) {
        //StartedGrayBackground
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] = 0f;
        hsv[2] = hsv[2] * 0.8f;
        return Color.HSVToColor(hsv);
    }

    private LinkedList<String> convertPairsToStrings(LinkedList<Integer> pairs) {
        LinkedList<String> ll = new LinkedList<>();
        int x = 0, y = 0;
        boolean isY = false;
        for (Integer p : pairs) {
            if (!isY) {
                x = p;
                isY = true;
            } else {
                y = p;
                ll.add(String.valueOf(x) + "," + String.valueOf(y));
                isY = false;
            }
        }
        return ll;
    }

    private String getOriginalFilePath(String fileName, String categoryName) {
        return ScreenOne.getOutputMediaFile(COLORING_DIRECTORY_NAME + File.separator + categoryName, fileName, mCategoryIcon.getContext()).toString();
    }

    private String saveImage(Bitmap bitmap, String fileName, String categoryName){
        File imageFile = ScreenOne.getOutputMediaFile(COLORING_DIRECTORY_NAME + File.separator + categoryName, fileName, mCategoryIcon.getContext());
        if (imageFile != null)
            if (!imageFile.exists()) {
                ScreenOne.storeImage(bitmap, imageFile);
            }
        return imageFile.toString();
    }

    public void reinitAfterColoring() {
        //todo: this "setColoredPixelsPairs()" need in "setNewIconByProgressColoring()"
        //todo: when "createWorkedIconImageByConfig()" used mColoredPixelsPairs
        setColoredPixelsPairs();

        setNewIconByProgressColoring();
        setIsFinishedByFile();
        if (mIsFinished) {
            ScreenOne activity = ((ScreenOne) getActivity());
            activity.mInProcess.removeFromCategory(this);
            activity.mFinished.addToCategory(this);
        }
    }

    public static LinkedList<String> getAllImageAdaptersByCategory(String categoryTitle) {
        LinkedList<String> ll = new LinkedList<>();
        for (ImageAdapter ia : CategoryDict.get(categoryTitle)) {
            ll.add(ia.getFullFilePathForColoring());
        }
        return ll;
    }

    public Bitmap getBitmap() {return mBitmap;}

    public Bitmap getColoredBitmap() {
        return BitmapFactory.decodeFile(getOriginalFullFilePath());
    }

    public void restartColoring() {
        //!
        unsetFinished();
        setWorkStarted();
        setColoredPixelsPairs();

        Intent intent = new Intent(mCategoryIcon.getContext(), MainActivity.class);
        intent.putExtra("coloredPixels", mColoredPixelsPairs);
        intent.putExtra("fullPathWRK", getWorkedFullFilePath());
        intent.putExtra("fullPathFIN", getFinishedFullFilePath());
        intent.putExtra("fullPathToColoring", getFullFilePathForColoring());
        getActivity().startActivityForResult(intent, ScreenOne.COLORING_REQUEST_CODE);
    }
}
