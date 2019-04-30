package com.pixelfunapp.pixelpart;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static android.view.View.TEXT_ALIGNMENT_CENTER;

public class Pixel {
    private static final int GRID_COLOR = Color.rgb( 200, 218, 254); //#c8dafe
    private static final int NUMBER_COLOR = Color.rgb( 115, 139, 168); //#738ba8
    private static final int STARTED_BACKGROUND_COLOR = Color.WHITE;
    private static final int DARKED_BACKGROUND_COLOR = Color.LTGRAY;
    private static final boolean CHECK_IS_COLORED_ON_DOUBLE_TAP = false;
    private static final int BUTTON_MARGIN_DP = 4;

    public static final int PIXEL_SIZE = 17;


    public static HashMap<Integer, LinkedList<Pixel>> pxsByNumberMap;
    public static HashMap<Integer, LinkedList<Pixel>> pxsByNumberNotColoredMap;
    private static LinkedList<Pixel> pxs;
    private boolean isColored, isWrongColored;
    private int pixelColor, x, y, pixelNumber;
    private Context mContext;

    static Bitmap.Config config = Bitmap.Config.ARGB_4444;

    Pixel(int color, int buttonNumber, Context context) {
        this.pixelColor = color;
        this.pixelNumber = buttonNumber;
        this.mContext = context;
    }

    public View generatePalleteButton() {
        //Inflate
        View palleteButtonRL = LayoutInflater.from(mContext).inflate(R.layout.pallete_button, null);
        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        int marginPX = convertDPsToPXs(BUTTON_MARGIN_DP);
        llParams.setMargins(marginPX, marginPX, marginPX, marginPX);

        palleteButtonRL.setLayoutParams(llParams);


        palleteButtonRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newNumber = Integer.parseInt(((TextView)v.findViewById(R.id.pallete_button_number)).getText().toString());

                MainActivity.mPixelPalette.setNewSelectedColorNumber(newNumber);
            }
        });
        //SetButtonColor
        TextView palleteButtonTextView = palleteButtonRL.findViewById(R.id.pallete_button_number);
        Drawable d = palleteButtonRL.getResources().getDrawable(R.drawable.pallete_button_unselected);
        setFillColorForDrawable(d, pixelColor);
        palleteButtonTextView.setBackground(d);
        palleteButtonTextView.setText(String.valueOf(pixelNumber));

        //BlackAndWhite TextColor
        int difWhite = 255 - Color.red(pixelColor) + 255 - Color.green(pixelColor) + 255 - Color.blue(pixelColor);
        if (difWhite < 255/3) palleteButtonTextView.setTextColor(Color.BLACK);
        else palleteButtonTextView.setTextColor(Color.WHITE);

        return palleteButtonRL;
    }

    private static View getParentById(View v, int resourceId) {
        View parent = (View)v.getParent();
        while (true) {
            if (parent != null) {
                if (parent.getId() == resourceId) return parent;
                else parent = (View)parent.getParent();
            } else {
                return null;
            }
        }
    }

    private Pixel(int color, int x, int y, boolean isColored) {
        this.pixelColor = color;
        this.x = x;
        this.y = y;
        this.isColored = isColored;
        this.pixelNumber = definiteColorNumber(this.pixelColor);
        if (this.pixelNumber != -1) putToPXsByNumberMap();
    }

    private void putToPXsByNumberMap() {
        LinkedList<Pixel> ll = pxsByNumberMap.get(pixelNumber);
        if (ll == null) {
            ll = new LinkedList<>();
            pxsByNumberMap.put(pixelNumber, ll);
        }
        ll.add(this);
        //isColored
        if (!isColored) {
            LinkedList<Pixel> llNotColored = pxsByNumberNotColoredMap.get(pixelNumber);
            if (llNotColored == null) {
                llNotColored = new LinkedList<>();
                pxsByNumberNotColoredMap.put(pixelNumber, llNotColored);
            }
            llNotColored.add(this);
        }
    }

    private static LinkedList<Pixel> getNotColoredPixelsByNumber(int colorNumber) {
        LinkedList<Pixel> ll = pxsByNumberMap.get(colorNumber);
        LinkedList<Pixel> res = new LinkedList<>();
        for (Pixel p : ll) {
            if (!p.isColored) res.add(p);
        }
        return res;
    }

    private static LinkedList<Pixel> getNotWrongColoredPixelsByNumber(int colorNumber) {
        LinkedList<Pixel> ll = getNotColoredPixelsByNumber(colorNumber);
        LinkedList<Pixel> res = new LinkedList<>();
        for (Pixel p : ll) {
            if (!p.isWrongColored) res.add(p);
        }
        return res;
    }

    public static void setFillColorForDrawable(Drawable d, int color) {
        if (d instanceof ShapeDrawable) {
            // cast to 'ShapeDrawable'
            ShapeDrawable sd = (ShapeDrawable) d;
            sd.getPaint().setColor(color);
        } else if (d instanceof GradientDrawable) {
            // cast to 'GradientDrawable'
            GradientDrawable gd = (GradientDrawable) d;
            gd.setColor(color);
        } else if (d instanceof ColorDrawable) {
            // alpha value may need to be set again after this call
            ColorDrawable cd = (ColorDrawable) d;
            cd.setColor(color);
        }
    }

    private static int definiteColorNumber(int pixel) {
        if (Color.alpha(pixel) != 255) return -1;
        int colorNumber = -1;
        int pR, pG, pB;
        pR = Color.red(pixel);
        pG = Color.green(pixel);
        pB = Color.blue(pixel);
        int R, G, B;
        int minimalDif = 255 + 255 + 255 + 1;
        for (int i = 1; i <= PixelPalette.mPalleteColorsMap.size(); i++) {
            int c = PixelPalette.mPalleteColorsMap.get(i);
            R = Color.red(c);
            G = Color.green(c);
            B = Color.blue(c);
            int difference = 0;
            difference += Math.abs(R - pR);
            difference += Math.abs(G - pG);
            difference += Math.abs(B - pB);
            if (difference < minimalDif) {
                minimalDif = difference;
                colorNumber = i;
            }
        }
        return colorNumber;
    }

    public static int definiteColorNumberInColors(int color , LinkedList<Integer> colors) {
        if (Color.alpha(color) != 255) return -1;
        int colorNumber = -1;
        int pR, pG, pB;
        pR = Color.red(color);
        pG = Color.green(color);
        pB = Color.blue(color);
        int R, G, B;
        int minimalDif = 1276;
        for (int i = 1; i <= colors.size(); i++) {
            int c = colors.get(i-1);
            R = Color.red(c);
            G = Color.green(c);
            B = Color.blue(c);
            int difference = 0;
            difference += Math.abs(R - pR);
            difference += Math.abs(G - pG);
            difference += Math.abs(B - pB);
            if (difference < minimalDif) {
                minimalDif = difference;
                colorNumber = i;
            }
        }
        return colorNumber;
    }

    private static List<String> getXYLinesFromFilePath(String path) {
        File wrk = new File(path);
        int length = (int) wrk.length();
        byte[] bytes = new byte[length];
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(wrk);
            try {
                fin.read(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                fin.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String contents = new String(bytes);
        String[] ss = contents.split("\n");
        return Arrays.asList(ss).subList(1,ss.length);
    }

    public static Bitmap createBitmapNumber(int number) {
        TextView textView = new TextView(MainActivity.mPixelPalette.getContext());
        //textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.layout(0, 0, 150, 150);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 100);
        textView.setTypeface(Typeface.SANS_SERIF);
        //NumberTextColor
        int numberColor = NUMBER_COLOR;
        //make grayColor from Pallete color by number
//        int color = Pallete.mPalleteColorsMap.get(number);
//        float[] hsv = new float[3];
//        Color.colorToHSV(color, hsv);
//        hsv[1] = 1f;
//        hsv[2] = hsv[2] * 0.3f;
//        int numberColor = Color.HSVToColor(hsv);

        textView.setTextColor(numberColor);
        textView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        //textView.setShadowLayer(5, 2, 2, Color.CYAN); //text shadow
        textView.setText(String.valueOf(number));
        textView.setDrawingCacheEnabled(true);
        Bitmap trimedNumber = Pixel.trimAlpha(textView.getDrawingCache());
        int width = 150, height = 150;
        Bitmap preScaled = Bitmap.createBitmap(width, height, config);
        int targetX = (width - trimedNumber.getWidth()) / 2;
        int targetY = (height - trimedNumber.getHeight()) / 2;
        drawBitmapInTargetBitmap(preScaled, trimedNumber, targetX, targetY);
        return Bitmap.createScaledBitmap(preScaled, PIXEL_SIZE, PIXEL_SIZE, false);
    }

    public static Bitmap createEmptyPixelBitmap() {
        return Bitmap.createBitmap(PIXEL_SIZE, PIXEL_SIZE, config);
    }

    public static Bitmap createFullColoredBitmapByNumber(int number) {
        int color = PixelPalette.mPalleteColorsMap.get(number);
        int[] pixels = new int[PIXEL_SIZE * PIXEL_SIZE];
        Arrays.fill(pixels, color);
        Bitmap fullColored = Bitmap.createBitmap(pixels, PIXEL_SIZE, PIXEL_SIZE, config);
        return fullColored;
    }

    public static Bitmap generateBitmapWorked(Bitmap image) {
        //create BitmapWorked
        int width = image.getWidth();
        int height = image.getHeight();
        Bitmap res = Bitmap.createBitmap(width * PIXEL_SIZE, height * PIXEL_SIZE, config);
        int i = 0;
        for (Pixel px: pxs
                ) {
            if (px.pixelNumber != -1) {
                drawPixelBitmapInTargetBitmap(res, px.getPixelBitmapFull(), px.x, px.y);
                Log.i("Pixel No.", String.valueOf(i));
            }
            i++;
        }
        return res;
    }

    public static Pixel getPixelByFloatCoordinate(float x, float y){
        int pxX = (int)x / PIXEL_SIZE;
        int pxY = (int)y / PIXEL_SIZE;
        return getPixelByIntCoordinate(pxX, pxY);
    }

    private static Pixel getPixelByIntCoordinate(int x, int y) {
        for (Pixel px: pxs
                ) {
            if (px.x == x && px.y == y && px.pixelNumber != -1) {
                return px;
            }
        }
        return null;
    }

    public static int convertToGrayShade(int color) {
        //StartedGrayBackground
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] = 0f;
        hsv[2] = hsv[2] + (1.0f - hsv[2]) * 0.7f;
        return Color.HSVToColor(hsv);
    }

    private Bitmap getPixelBitmapFull() {
        if (pixelNumber == -1) return PixelPalette.mEmptyPixelBitmap;
        if (isColored) return PixelPalette.mFullColoredBitmap.get(pixelNumber);
        return PixelPalette.mStartedNotColoredBitmap.get(pixelNumber);
    }

    private static void savePixelPairToWRKFile(int x, int y) {
        if (!MainActivity.hasNewColoredPixels) MainActivity.hasNewColoredPixels = true;
        //add pairs to .wrk file
        File wrk = new File(MainActivity.fullPathWRK);
        try {
            FileOutputStream fos = new FileOutputStream(wrk, true);
            fos.write((String.valueOf(x) + "," + String.valueOf(y) + "\n").getBytes());
            fos.close();
        } catch (IOException e) {
            Log.e("addPixelsPairsToWork", "IOException! Can't append to file *.wrk");
        }
    }

    public static void savePixelsPairsToWRKFile(LinkedList<Pixel> pixels) {
        if (!MainActivity.hasNewColoredPixels) MainActivity.hasNewColoredPixels = true;
        StringBuilder sb = new StringBuilder();
        for (Pixel p : pixels) {
            sb.append(String.valueOf(p.x)).append(",").append(String.valueOf(p.y)).append("\n");
        }
        //add pairs to .wrk file
        File wrk = new File(MainActivity.fullPathWRK);
        try {
            FileOutputStream fos = new FileOutputStream(wrk, true);
            fos.write(sb.toString().getBytes());
            fos.close();
        } catch (IOException e) {
            Log.e("addPixelsPairsToWork", "IOException! Can't append to file *.wrk");
        }
    }


    private static void drawGridAndFillBackgroundAndSetTransNumber(Bitmap bitmap, int backgroundColor, Bitmap bitmapNumber) {
        int gridColor = GRID_COLOR;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        for (int y = 0; y < width; ++y) {
            for (int x = 0; x < height; ++x) {
                if (x == 0 | y == 0 | x == height-1 | y == width -1)
                    //draw grid
                    bitmap.setPixel(x, y, gridColor);
                else{
                    //get pixel from number bitmap
                    int numberPxColor = bitmapNumber.getPixel(x, y);
                    if (Color.alpha(numberPxColor) > 100)
                        //draw number
                        bitmap.setPixel(x, y, numberPxColor);
                    else
                        //draw background
                        bitmap.setPixel(x, y, backgroundColor);
                }
            }
        }
    }

    private static void drawBitmapInTargetBitmap(Bitmap target, Bitmap src, int targetX, int targetY) {
        for (int y = 0; y < src.getWidth(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                target.setPixel(x + targetX, y + targetY, src.getPixel(x, y));
            }
        }
    }

    private static void drawPixelBitmapInTargetBitmap(Bitmap target, Bitmap src, int targetX, int targetY) {
        int startX = targetX * PIXEL_SIZE;
        int startY = targetY * PIXEL_SIZE;

        for (int y = startY; y < startY + PIXEL_SIZE; ++y) {
            for (int x = startX; x < startX + PIXEL_SIZE; ++x) {
                target.setPixel(x, y, src.getPixel(x - startX, y - startY));
            }
        }
    }

    private void drawPixelBitmapInImageWorkspaceViewAndSetAndInvalidate(Bitmap src) {
        drawPixelBitmapInTargetBitmap(MainActivity.imageWorkspace, src, x, y);
        MainActivity.mImageWorkspaceView.setImageBitmap(MainActivity.imageWorkspace);
    }

    private void drawPixelBitmapInImageWorkspaceView(Bitmap src) {
        drawPixelBitmapInTargetBitmap(MainActivity.imageWorkspace, src, x, y);
        MainActivity.mImageWorkspaceView.setImageBitmap(MainActivity.imageWorkspace);
    }

    public static void initPXsByBitmap(Bitmap image, LinkedList<String> coloredPixelsPairsStrings) {
        int width = image.getWidth();
        int height = image.getHeight();
        pxs = new LinkedList<>();
        pxsByNumberMap = new HashMap<>();
        pxsByNumberNotColoredMap = new HashMap<>();
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                int color = image.getPixel(x, y);
                Pixel px = new Pixel(color, x, y, coloredPixelsPairsStrings.contains(String.valueOf(x) + "," + String.valueOf(y)));
                pxs.add(px);
            }
        }
    }

    private static void checkPixelIsColoredAndNumberThenAddToLL(Pixel pixel, int number, LinkedList<Pixel> ll) {
        if (pixel != null)
            if (pixel.pixelNumber == number)
                if (CHECK_IS_COLORED_ON_DOUBLE_TAP) {if (!pixel.isColored) ll.add(pixel);}
                else ll.add(pixel);
    }

    private LinkedList<Pixel> findNearedNotColoredNeighbors() {
        LinkedList<Pixel> res = new LinkedList<>();
        Pixel pxNeighbor;
        pxNeighbor = Pixel.getPixelByIntCoordinate(x + 1, y);
        checkPixelIsColoredAndNumberThenAddToLL(pxNeighbor, pixelNumber, res);
        pxNeighbor = Pixel.getPixelByIntCoordinate(x - 1, y);
        checkPixelIsColoredAndNumberThenAddToLL(pxNeighbor, pixelNumber, res);
        pxNeighbor = Pixel.getPixelByIntCoordinate(x, y + 1);
        checkPixelIsColoredAndNumberThenAddToLL(pxNeighbor, pixelNumber, res);
        pxNeighbor = Pixel.getPixelByIntCoordinate(x, y - 1);
        checkPixelIsColoredAndNumberThenAddToLL(pxNeighbor, pixelNumber, res);
        return res;
    }

    private void fillLLByNotColoredNeighbors(LinkedList<Pixel> ll) {
        for (Pixel px : findNearedNotColoredNeighbors()) {
            if (!ll.contains(px)) {
                ll.add(px);
                px.fillLLByNotColoredNeighbors(ll);
            }
        }
    }

    private static int convertColorToLightColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] = 0.25f;
        hsv[2] = 0.90f;
        return Color.HSVToColor(hsv);
    }

    public void coloringByHisNumber() {
        coloringByNumber(pixelNumber);
    }

    public void coloringByNumber(int coloringNumber) {
        if (!isColored) {
            Log.i("XY", "Number : " + String.valueOf(pixelNumber));
            if (coloringNumber == pixelNumber) {
                //Coloring
                isColored = true;
                isWrongColored = false;
                //! Save to file
                savePixelPairToWRKFile(x, y);
                Bitmap newPixelBitmap = PixelPalette.mFullColoredBitmap.get(pixelNumber);
                drawPixelBitmapInImageWorkspaceViewAndSetAndInvalidate(newPixelBitmap);
                //Remove from list
                PixelPalette.RemoveFromNotColoredMap(this);
                //check finishedColor?
                PixelPalette.checkColoredByNumber(pixelNumber);
            } else {
                //WrongColoring
                isWrongColored = true;
                //ColoringBackground
                Bitmap newPixelBitmap = PixelPalette.mWrongColoredBitmapNoNumber.get(pixelNumber).get(coloringNumber);
                drawPixelBitmapInImageWorkspaceViewAndSetAndInvalidate(newPixelBitmap);
            }
        }
    }

    public void coloringByNumberDoubleTap(int coloringNumber) {
        if (coloringNumber == pixelNumber) {
            //Coloring
//            if (!isColored) {
//                isColored = true;
//                isWrongColored = false;
//                //! Save to file
//                savePixelPairToWRK(x, y);
//                //redraw imageWorkspace
//                Bitmap newPixelBitmap = Pallete.mFullColoredBitmap.get(pixelNumber);
//                drawPixelBitmapInImageWorkspaceView(newPixelBitmap);
//            }
            //find neighbors
            LinkedList<Pixel> neighbors = new LinkedList<>();
            fillLLByNotColoredNeighbors(neighbors);
            //coloring neighbors
            for (Pixel px : neighbors) {
                px.isColored = true;
                px.isWrongColored = false;
                Bitmap newPixelBitmap = PixelPalette.mFullColoredBitmap.get(px.pixelNumber);
                px.drawPixelBitmapInImageWorkspaceView(newPixelBitmap);
                //Remove from list
                PixelPalette.RemoveFromNotColoredMap(px);
            }
            //save to file
            savePixelsPairsToWRKFile(neighbors);
            //check finishedColor?
            PixelPalette.checkColoredByNumber(coloringNumber);
            //?
            MainActivity.mImageWorkspaceView.invalidate();
        }
    }

    public static Bitmap trimAlpha(Bitmap image) {
        //found xStart, xFinish, yStart, yFinish
        int imageWidth = image.getWidth(), imageHeight = image.getHeight();
        int xStart = 0, xFinish = image.getWidth(), yStart = 0, yFinish = image.getHeight();
        //find xStart
        for (int x = 0; x < imageWidth; x++) {
            boolean isAlphaColumn = true;
            for (int y = 0; y < imageHeight; y++) {
                int px = image.getPixel(x, y);
                if (Color.alpha(px) > 0) {
                    xStart = x;
                    isAlphaColumn = false;
                    break;
                }
            }
            if (!isAlphaColumn) break;
        }
        //find xFinish
        for (int x = imageWidth - 1; x >= 0; x--) {
            boolean isAlphaColumn = true;
            for (int y = 0; y < imageHeight; y++) {
                int px = image.getPixel(x, y);
                if (Color.alpha(px) > 0) {
                    xFinish = x;
                    isAlphaColumn = false;
                    break;
                }
            }
            if (!isAlphaColumn) break;
        }
        //find yStart
        for (int y = 0; y < imageHeight; y++) {
            boolean isAlphaRow = true;
            for (int x = 0; x < imageWidth; x++) {
                int px = image.getPixel(x, y);
                if (Color.alpha(px) > 0) {
                    yStart = y;
                    isAlphaRow = false;
                    break;
                }
            }
            if (!isAlphaRow) break;
        }
        //find yFinish
        for (int y = imageHeight - 1; y >= 0 ; y--) {
            boolean isAlphaRow = true;
            for (int x = 0; x < imageWidth; x++) {
                int px = image.getPixel(x, y);
                if (Color.alpha(px) > 0) {
                    yFinish = y;
                    isAlphaRow = false;
                    break;
                }
            }
            if (!isAlphaRow) break;
        }
        //calculate new image xPivot y Pivot sideSize newX newY
        int newImageWidth = xFinish - xStart, newImageHeight = yFinish - yStart;
        int xPivot = xStart + newImageWidth / 2;
        int yPivot = yStart + newImageHeight / 2;
        int sideSize = Math.max(newImageWidth, newImageHeight);
        int newX = xPivot - sideSize / 2 + newImageWidth % 2;
        int newY = yPivot - sideSize / 2 + newImageHeight % 2;
        Bitmap res = Bitmap.createBitmap(image, newX , newY, sideSize, sideSize);
        return res;
    }

    public static Bitmap createBitmapFullByNumber(int number) {
        Bitmap pixelBitmap = Bitmap.createBitmap(PIXEL_SIZE, PIXEL_SIZE, config);
        //int backgroundColor = convertToGrayShade(color);
        int backgroundColor = STARTED_BACKGROUND_COLOR;
        Bitmap pixelNumberBitmap = PixelPalette.mBitmapNumbersMap.get(number);
        drawGridAndFillBackgroundAndSetTransNumber(pixelBitmap, backgroundColor, pixelNumberBitmap);
        return pixelBitmap;
    }

    public static Bitmap createDarkedBitmapByNumber(int number) {
        Bitmap pixelBitmap = Bitmap.createBitmap(PIXEL_SIZE, PIXEL_SIZE, config);
        //int backgroundColor = convertToGrayShade(color);
        int backgroundColor = DARKED_BACKGROUND_COLOR;
        Bitmap pixelNumberBitmap = PixelPalette.mBitmapNumbersMap.get(number);
        drawGridAndFillBackgroundAndSetTransNumber(pixelBitmap, backgroundColor, pixelNumberBitmap);
        return pixelBitmap;
    }

    public static LinkedHashMap<Integer, Bitmap> createWrongColoredCollectionByNumber(int number) {
        LinkedHashMap<Integer, Bitmap> res = new LinkedHashMap<>();
        for (int i = 1; i <= PixelPalette.mPalleteColorsMap.size(); i++) {
            int color = PixelPalette.mPalleteColorsMap.get(i);
            Bitmap newPixelBitmap = Bitmap.createBitmap(PIXEL_SIZE, PIXEL_SIZE, config);
            Bitmap pixelNumberBitmap = PixelPalette.mBitmapNumbersMap.get(number);
            int lightColoringColor = convertColorToLightColor(color);
            drawGridAndFillBackgroundAndSetTransNumber(newPixelBitmap, lightColoringColor, pixelNumberBitmap);
            res.put(i, newPixelBitmap);
        }
        return res;
    }

    public static void returnStartedPixelsByNumber(int selectedColorNumber) {
        LinkedList<Pixel> llToRedraw = Pixel.getNotWrongColoredPixelsByNumber(selectedColorNumber);
        for (Pixel p : llToRedraw) {
            p.drawPixelBitmapInImageWorkspaceView(PixelPalette.mStartedNotColoredBitmap.get(p.pixelNumber));
        }
        MainActivity.mImageWorkspaceView.setImageBitmap(MainActivity.imageWorkspace);
    }

    public static void setDarkBackgroundForPixelsByNumber(int selectedColorNumber) {
        LinkedList<Pixel> llToRedraw = Pixel.getNotWrongColoredPixelsByNumber(selectedColorNumber);
        for (Pixel p : llToRedraw) {
            p.drawPixelBitmapInImageWorkspaceView(PixelPalette.mDarkedNotColoredBitmap.get(p.pixelNumber));
        }
        MainActivity.mImageWorkspaceView.setImageBitmap(MainActivity.imageWorkspace);
    }

    public static int getNotFinishedColorNumberNearNumber(int currentNumber) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        for ( Map.Entry<Integer, LinkedList<Pixel>> entry: pxsByNumberNotColoredMap.entrySet()) {
            if (entry.getValue().size() != 0) arrayList.add(entry.getKey());
        }
        int nearNumber = 0;
        int diff = Integer.MAX_VALUE;
        for (Integer number: arrayList) {
            if (Math.abs(currentNumber-number) < diff) {
                diff = Math.abs(currentNumber-number);
                nearNumber = number;
            }
        }
        return nearNumber;
    }

    public int getNumber() {
        return pixelNumber;
    }

    public int convertPXsToDPs(int px) {
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, dm);
        return (int)dp;
    }

    public int convertDPsToPXs(int dp) {
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
        return (int)px;
    }
}
