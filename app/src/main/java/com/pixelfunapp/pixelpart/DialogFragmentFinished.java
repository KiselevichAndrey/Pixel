package com.pixelfunapp.pixelpart;

import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Locale;

public class DialogFragmentFinished extends DialogFragment implements View.OnClickListener {
    public static final int DELAY_MILLIS = 500;
    public static final String PACKAGE_NAME_INSTAGRAM_ANDROID = "com.instagram.android";
    private View dialogView;
    private CategoryAdapter mCurrentCategoryAdapter;
    private LinkedList<ImageAdapter> listToRemoveWork, listToAddFavorite, listToRemoveFavorite;
    private boolean mShowColoredImage;
    private Bitmap mBitmap;

    private TextView mStatisticsFirstLine;
    private TextView mStatisticsSecondLine;
    private TextView mStatisticsThirdLine;
    private TextView mFirstLine;
    private TextView mSecondLine;
    private TextView mThirdLine;
    private TextView mFourthLine;

    private View mRestartButton;
    private View mCloseButton;
    private View mLeftButton;
    private View mRightButton;
    private ImageView mImage;
    private View mSharedButton;
    private View mInstaButton;
    private View mDownloadButton;
    private View mColoringButton;
    private View mFavoriteAddButton;
    private View mFavoriteRemoveButton;
    private View mRemoveButton;
    private View mYesButton;
    private View mNoButton;
    private View mRecoveryButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        dialogView = inflater.inflate(R.layout.dialog_preview, container);
        listToRemoveWork = new LinkedList<>();
        listToAddFavorite = new LinkedList<>();
        listToRemoveFavorite = new LinkedList<>();

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //FULL_SCREEN
        getDialog().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        mStatisticsFirstLine = dialogView.findViewById(R.id.preview_statisrics_first_line);
        mStatisticsSecondLine = dialogView.findViewById(R.id.preview_statisrics_second_line);
        mStatisticsThirdLine = dialogView.findViewById(R.id.preview_statisrics_third_line);
        mFirstLine = dialogView.findViewById(R.id.preview_first_line);
        mSecondLine = dialogView.findViewById(R.id.preview_second_line);
        mThirdLine = dialogView.findViewById(R.id.preview_third_line);
        mFourthLine = dialogView.findViewById(R.id.preview_fourth_line);

        mRestartButton = dialogView.findViewById(R.id.preview_restart_btn);
        mCloseButton = dialogView.findViewById(R.id.preview_close_btn);
        mLeftButton = dialogView.findViewById(R.id.preview_left_btn);
        mRightButton =  dialogView.findViewById(R.id.preview_right_btn);
        mImage = dialogView.findViewById(R.id.preview_image);
        mSharedButton = dialogView.findViewById(R.id.preview_shared_btn);
        mInstaButton = dialogView.findViewById(R.id.preview_insta_btn);
        mDownloadButton = dialogView.findViewById(R.id.preview_download_btn);
        mColoringButton = dialogView.findViewById(R.id.preview_coloring_btn);
        mFavoriteAddButton = dialogView.findViewById(R.id.preview_favorite_add_btn);
        mFavoriteRemoveButton = dialogView.findViewById(R.id.preview_favorite_remove_btn);
        mRemoveButton = dialogView.findViewById(R.id.preview_remove_btn);
        mYesButton = dialogView.findViewById(R.id.preview_yes_btn);
        mNoButton = dialogView.findViewById(R.id.preview_no_btn);
        mRecoveryButton =  dialogView.findViewById(R.id.preview_recovery_btn);

        mRestartButton.setOnClickListener(this);
        mCloseButton.setOnClickListener(this);
        mLeftButton.setOnClickListener(this);
        mRightButton.setOnClickListener(this);
        mImage.setOnClickListener(this);
        mSharedButton.setOnClickListener(this);
        mInstaButton.setOnClickListener(this);
        mDownloadButton.setOnClickListener(this);
        mColoringButton.setOnClickListener(this);
        mFavoriteAddButton.setOnClickListener(this);
        mFavoriteRemoveButton.setOnClickListener(this);
        mRemoveButton.setOnClickListener(this);
        mYesButton.setOnClickListener(this);
        mNoButton.setOnClickListener(this);
        mRecoveryButton.setOnClickListener(this);

        mLeftButton.setVisibility(View.GONE);
        mRightButton.setVisibility(View.GONE);
        mStatisticsFirstLine.setVisibility(View.GONE);
        mStatisticsSecondLine.setVisibility(View.GONE);
        mFirstLine.setVisibility(View.VISIBLE);
        mSecondLine.setVisibility(View.VISIBLE);
        mThirdLine.setVisibility(View.VISIBLE);
        mFourthLine.setVisibility(View.VISIBLE);
        mSharedButton.setVisibility(View.VISIBLE);
        mInstaButton.setVisibility(View.VISIBLE);
        mDownloadButton.setVisibility(View.VISIBLE);
        mBitmap = Bitmap.createScaledBitmap(((BitmapDrawable)MainActivity.mImageWorkspaceView.getDrawable()).getBitmap(), 600, 600, false);
        mImage.setImageBitmap(mBitmap);


        return dialogView;
    }

    private void DeactivateAllMaybeActiveElements() {
        //- deactivate all maybe activated elements:
        //statistics_3rd
        mStatisticsThirdLine.setVisibility(View.GONE);
        //line_1st
        mFirstLine.setVisibility(View.GONE);
        //line_2nd
        mSecondLine.setVisibility(View.GONE);
        //line_4th
        mFourthLine.setVisibility(View.GONE);
        //shared_button
        mSharedButton.setVisibility(View.GONE);
        //insta_button
        mInstaButton.setVisibility(View.GONE);
        //download_button
        mDownloadButton.setVisibility(View.GONE);
        //coloring_btn
        mColoringButton.setVisibility(View.GONE);
        //favorite_add_btn
        mFavoriteAddButton.setVisibility(View.GONE);
        //favorite_remove_btn
        mFavoriteRemoveButton.setVisibility(View.GONE);
        //remove_btn
        mRemoveButton.setVisibility(View.GONE);
        //yes_btn
        mYesButton.setVisibility(View.GONE);
        //no_btn
        mNoButton.setVisibility(View.GONE);
        //recovery_btn
        mRecoveryButton.setVisibility(View.GONE);
    }

    private void initFirstType(boolean isFavorite, int pxsCount, int colorsCount) {
        //1Type
        //statistics_1st (on)
        //statistics_2nd (on)
        //line_3rd (on)
        //line_4th
        mFourthLine.setVisibility(View.VISIBLE);
        //coloring_btn
        mColoringButton.setVisibility(View.VISIBLE);
        //if (mIsFavorite) favorite_remove_btn
        //else favorite_add_btn
        if (isFavorite) mFavoriteRemoveButton.setVisibility(View.VISIBLE);
        else mFavoriteAddButton.setVisibility(View.VISIBLE);

        //init()
        mStatisticsFirstLine.setText(generateStringNumberOfPixels(pxsCount));
        mStatisticsSecondLine.setText(generateStringNumberOfColors(colorsCount));
        mThirdLine.setText("Нажмите на картинку, чтобы увидеть,");
        mFourthLine.setText("как она выглядит в цвете.");

    }

    private void initSecondType(int pxsCount, int colorsCount, int coloredPXsCount) {
        //2Type
        //statistics_1st (on)
        //statistics_2nd
        //statistics_3rd
        mStatisticsThirdLine.setVisibility(View.VISIBLE);
        //line_3rd (on)
        //line_4th
        mFourthLine.setVisibility(View.VISIBLE);
        //coloring_btn
        mColoringButton.setVisibility(View.VISIBLE);
        //remove_btn
        mRemoveButton.setVisibility(View.VISIBLE);

        //init()
        mStatisticsFirstLine.setText(generateStringNumberOfPixels(pxsCount));
        mStatisticsSecondLine.setText(generateStringNumberOfColors(colorsCount));
        int percents = 100 * coloredPXsCount / pxsCount;
        if (percents == 0) percents = 1;
        //todo: problem when ~99% colored pixels
        if (percents >= 100) percents = 99;
        mStatisticsThirdLine.setText(String.valueOf(percents) + "%");
        mThirdLine.setText("Нажмите на картинку, чтобы увидеть,");
        mFourthLine.setText("как она выглядит в цвете.");
    }

    private void initThirdType(int pxsCount, int colorsCount) {
        //3Type
        //statistics_1st (on)
        //statistics_2nd
        //line_1st
        mFirstLine.setVisibility(View.VISIBLE);
        //line_3rd (on)
        //shared_button
        mSharedButton.setVisibility(View.VISIBLE);
        //insta_button
        mInstaButton.setVisibility(View.VISIBLE);
        //download_button
        mDownloadButton.setVisibility(View.VISIBLE);

        //init()
        mStatisticsFirstLine.setText(generateStringNumberOfPixels(pxsCount));
        mStatisticsSecondLine.setText(generateStringNumberOfColors(colorsCount));
        mFirstLine.setText("Картинка заполнена на 100%");
        mThirdLine.setText("Хотите поделиться с друзьями?");
    }

    private void onChangeCurrentColoringImageAdapter(ImageAdapter newImageAdapter) {
        DeactivateAllMaybeActiveElements();
        //if getNext() == current => deactivate left-right listening buttons
        if (mCurrentCategoryAdapter.getNext(newImageAdapter) == newImageAdapter) {

        } else {
            mLeftButton.setVisibility(View.VISIBLE);
            mRightButton.setVisibility(View.VISIBLE);
        }

        //initialize Type
        initThirdType(newImageAdapter.pxsCount, newImageAdapter.colorsCount);

        //todo: when finished coloring check and adapted
        //4Type (Finished coloring)
        //not category, not listening
        //line_1st
        //line_2nd
        //line_3rd
        //line_4th
        //shared_button
        //insta_button
        //download_button
        //todo: need custom view with scaled source bitmap
        mImage.setImageBitmap(Bitmap.createScaledBitmap(newImageAdapter.getBitmap(), 600, 600, false));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.MyDialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        for (ImageAdapter ia : listToRemoveWork) {
            ia.unsetWorkStarted();
        }
        for (ImageAdapter ia : listToAddFavorite) {
            ia.setFavorite();
        }
        for (ImageAdapter ia : listToRemoveFavorite) {
            ia.unsetFavorite();
        }
    }

    @Override
    public void onClick(View v) {
        final ImageAdapter ia = ImageAdapter.currentColoringImageAdapter;
        switch (v.getId()) {
            case(R.id.preview_restart_btn):
                dismiss();
                ia.restartColoring();
                break;
            case (R.id.preview_close_btn):
                dismiss();
                getActivity().onBackPressed();
                break;
            case (R.id.preview_left_btn):
                leftClick();
                break;
            case (R.id.preview_right_btn):
                rightClick();
                break;
            case (R.id.preview_image):
                if (mShowColoredImage) mImage.setImageBitmap(Bitmap.createScaledBitmap(ia.getBitmap(), 600, 600, false));
                else mImage.setImageBitmap(Bitmap.createScaledBitmap(ia.getColoredBitmap(), 600, 600, false));
                mShowColoredImage = !mShowColoredImage;
                break;
            case (R.id.preview_shared_btn):
                shareClick();
                break;
            case (R.id.preview_insta_btn):
                instaClick();
                break;
            case (R.id.preview_download_btn):
                //todo: to save
                saveClick();
                break;
            case (R.id.preview_coloring_btn):
                dismiss();
                final ScreenOne activity = ((ScreenOne) getActivity());
                if (activity.adIsOff) ia.startColoring();
                else {
                    activity.mInterstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            ia.startColoring();
                            activity.mInterstitialAd.loadAd(new AdRequest.Builder().build());
                        }
                    });
                    activity.showAd();
                }
                break;
            case (R.id.preview_favorite_add_btn):
                if (listToRemoveFavorite.contains(ia)) listToRemoveFavorite.remove(ia);
                else listToAddFavorite.add(ia);
                favoriteAddClick();
                break;
            case (R.id.preview_favorite_remove_btn):
                if (listToAddFavorite.contains(ia)) listToAddFavorite.remove(ia);
                else listToRemoveFavorite.add(ia);
                favoriteRemoveClick();
                break;
            case (R.id.preview_remove_btn):
                removeClick();
                break;
            case (R.id.preview_yes_btn):
                listToRemoveWork.add(ia);
                yesRemoveClick();
                break;
            case (R.id.preview_no_btn):
                noRemoveClick();
                break;
            case (R.id.preview_recovery_btn):
                listToRemoveWork.remove(ia);
                recoveryClick();
                break;
        }
    }

    private void saveClick() {
        Bitmap bitmap = mBitmap;
        OutputStream fOut = null;
        String fileName = "PX_"+ System.currentTimeMillis() +".jpg";
        File file = ScreenOne.getOutputMediaFile("PixelFun", fileName, getActivity());
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        transparentColorToWhite(bitmap);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "PixelFun Coloring");
//        values.put(MediaStore.Images.Media.DESCRIPTION, this.getString(R.string.picture_description));
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis ());
        values.put(MediaStore.Images.ImageColumns.BUCKET_ID, file.toString().toLowerCase(Locale.US).hashCode());
        values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, file.getName().toLowerCase(Locale.US));
        values.put("_data", file.getAbsolutePath());

        ContentResolver cr = getActivity().getContentResolver();
        cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Uri uri =  Uri.fromFile(file);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        String mime = "image/*";
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        if (mimeTypeMap.hasExtension(
                mimeTypeMap.getFileExtensionFromUrl(uri.toString())))
            mime = mimeTypeMap.getMimeTypeFromExtension(
                    mimeTypeMap.getFileExtensionFromUrl(uri.toString()));
        intent.setDataAndType(uri, mime);
        startActivity(intent);
    }

    private void shareClick() {
        Uri imageUri;
        Intent intent;
        String fileToSendPath = Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getActivity().getPackageName()
                + "/Files/coloring/" + "pixelfun_coloring.png";
        saveBitmapToPNGFile(Bitmap.createScaledBitmap(mBitmap, 600, 600, false), fileToSendPath);
        imageUri = Uri.parse("file://" + fileToSendPath);
        intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, "Посмотри что у меня получилось! Мне нравится, попробуй #PixelFun. У тебя тоже будет крутая раскраска.");
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        intent.setType("image/*");
        startActivity(intent);
    }

    private void instaClick() {
        if (verificaInstagram()) {
            Uri imageUri;
            Intent intent;
            String fileToSendPath = Environment.getExternalStorageDirectory()
                    + "/Android/data/"
                    + getActivity().getPackageName()
                    + "/Files/coloring/" + "pixelfun_coloring.png";
            saveBitmapToPNGFile(Bitmap.createScaledBitmap(mBitmap, 600, 600, false), fileToSendPath);
            imageUri = Uri.parse("file://" + fileToSendPath);
            intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
            intent.putExtra(Intent.EXTRA_TEXT, "Посмотри что у меня получилось! Мне нравится, попробуй #PixelFun. У тебя тоже будет крутая раскраска.");
//            intent.putExtra("InstagramCaption", "Посмотри что у меня получилось! Мне нравится, попробуй #PixelFun. У тебя тоже будет крутая раскраска.");
//            intent.putExtra("caption", "Посмотри что у меня получилось! Мне нравится, попробуй #PixelFun. У тебя тоже будет крутая раскраска.");
            intent.setPackage(PACKAGE_NAME_INSTAGRAM_ANDROID);
            startActivity(intent);
        }
        else startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + PACKAGE_NAME_INSTAGRAM_ANDROID)));
    }

    private boolean verificaInstagram(){
        boolean installed = false;

        try {
            ApplicationInfo info = getActivity().getPackageManager().getApplicationInfo("com.instagram.android", 0);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    private void transparentColorToWhite(Bitmap bitmap) {
        //Transparent color to color
        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                int px = bitmap.getPixel(x, y) ;
                if (px == 0) bitmap.setPixel(x, y, ~px);
                else bitmap.setPixel(x, y, px | 0xFF000000);
            }
        }
    }

    private void saveBitmapToPNGFile(Bitmap bitmap, String path) {
        transparentColorToWhite(bitmap);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) out.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void favoriteAddClick() {
        //Hide
        mFavoriteAddButton.setVisibility(View.GONE);
        //Show
        final Button acceptButton = dialogView.findViewById(R.id.preview_favorite_accept_btn);
        acceptButton.setVisibility(View.VISIBLE);
        acceptButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                acceptButton.setVisibility(View.GONE);
                mFavoriteRemoveButton.setVisibility(View.VISIBLE);
            }
        }, DELAY_MILLIS);
    }

    private void favoriteRemoveClick() {
        //Hide
        mFavoriteRemoveButton.setVisibility(View.GONE);
        //Show
        final Button acceptButton = dialogView.findViewById(R.id.preview_favorite_accept_btn);
        acceptButton.setVisibility(View.VISIBLE);
        acceptButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                acceptButton.setVisibility(View.GONE);
                mFavoriteAddButton.setVisibility(View.VISIBLE);
            }
        }, DELAY_MILLIS);
    }

    private void removeClick() {
        //Hide
        mColoringButton.setVisibility(View.GONE);
        mRemoveButton.setVisibility(View.GONE);
        mFourthLine.setVisibility(View.INVISIBLE);
        //Show
        mYesButton.setVisibility(View.VISIBLE);
        mNoButton.setVisibility(View.VISIBLE);
        //Set
        mThirdLine.setText("Вы уверены, что хотите удалить эту работу?");
        mThirdLine.setTypeface(null, Typeface.BOLD);
    }

    private void yesRemoveClick() {
        //Hide
        mYesButton.setVisibility(View.GONE);
        mNoButton.setVisibility(View.GONE);
        //Show
        mRecoveryButton.setVisibility(View.VISIBLE);
        //Set
        mThirdLine.setText("Работа удалена!");
    }

    private void noRemoveClick() {
        //Hide
        mYesButton.setVisibility(View.GONE);
        mNoButton.setVisibility(View.GONE);
        //Show
        mColoringButton.setVisibility(View.VISIBLE);
        mRemoveButton.setVisibility(View.VISIBLE);
        mFourthLine.setVisibility(View.VISIBLE);
        //Set
        mThirdLine.setText("Нажмите на картинку, чтобы увидеть,");
        mThirdLine.setTypeface(null, Typeface.NORMAL);
    }

    private void recoveryClick() {
        //Hide
        mRecoveryButton.setVisibility(View.GONE);
        //Show and Set
        noRemoveClick();
    }


    private void leftClick() {
        ImageAdapter.currentColoringImageAdapter = mCurrentCategoryAdapter.getPrevious(ImageAdapter.currentColoringImageAdapter);
        onChangeCurrentColoringImageAdapter(ImageAdapter.currentColoringImageAdapter);
    }

    private void rightClick() {
        ImageAdapter.currentColoringImageAdapter = mCurrentCategoryAdapter.getNext(ImageAdapter.currentColoringImageAdapter);
        onChangeCurrentColoringImageAdapter(ImageAdapter.currentColoringImageAdapter);
    }

    private String generateStringNumberOfPixels(int pxsCount) {
        StringBuilder sb = new StringBuilder();
        sb.append(pxsCount);
        if (pxsCount % 100 > 10 && pxsCount % 100 < 20) sb.append(" квадратов");
        else
            switch (pxsCount % 10) {
                case 1:
                    sb.append(" квадрат");
                    break;
                case 2:
                case 3:
                case 4:
                    sb.append(" квадрата");
                    break;
                default:
                    sb.append(" квадратов");
            }
        return sb.toString();
    }

    private String generateStringNumberOfColors(int colorsCount) {
        StringBuilder sb = new StringBuilder();
        sb.append(colorsCount);
        if (colorsCount % 100 > 10 && colorsCount % 100 < 20) sb.append(" цветов");
        else
            switch (colorsCount % 10) {
                case 1:
                    sb.append(" цвет");
                    break;
                case 2:
                case 3:
                case 4:
                    sb.append(" цвета");
                    break;
                default:
                    sb.append(" цветов");
            }
        return sb.toString();
    }

    public void setmCurrentCategoryAdapter(CategoryAdapter ca) {
        mCurrentCategoryAdapter = ca;
    }
}
