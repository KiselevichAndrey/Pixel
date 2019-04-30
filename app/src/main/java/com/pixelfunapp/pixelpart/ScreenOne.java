package com.pixelfunapp.pixelpart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

public class ScreenOne extends AppCompatActivity implements PurchasesUpdatedListener, BillingClientStateListener {
    private static final String COLORING_FULL_PATH = "/Android/data/com.pixelfunapp.pixelpart/Files/coloring";
    public static final int COLORING_REQUEST_CODE = 11;
    public static final int CAMERA_REQUEST_CODE = 12;
    public static final String PATTERN_COLORING_NAME = "pixelfun_([a-z_]+)_\\d{1,20}$";
    public static final boolean DEBUG_ALWAYS_PREPARE_COLORING = false;
    public static final String ADMOB_APP_ID = "ca-app-pub-5688016172099091~6383387241";

    static public String[] mCategoriesTitles;

    private View mRootCategories, mRootWorkshop;
    private Spinner mCategoriesSpinner;
    private ViewGroup mCategoriesContainer, mWorkshopContainer;
    private TextView mTvMyWorks, mTvFavorites;

    private View WorkshopTopButtonsView;
    private ViewPager mViewPager;
    private SharedPreferencesHelper mSharedPreferencesHelper;
    private HashMap<String, String> mMapCategoriesNamesToTitles;
    public CategoryAdapter mInProcess, mFinished, mFavorites;
    private AdView mAdView;
    private BillingClient mBillingClient;
    public boolean adIsOff = true;
    public InterstitialAd mInterstitialAd;




    private View.OnClickListener myworksClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mTvMyWorks.setTypeface(null, Typeface.BOLD);
            mTvFavorites.setTypeface(null, Typeface.NORMAL);
            mInProcess.Show();
            mFinished.Show();
            mFavorites.Hide();
        }
    };

    private View.OnClickListener favoritesClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mTvMyWorks.setTypeface(null, Typeface.NORMAL);
            mTvFavorites.setTypeface(null, Typeface.BOLD);
            mInProcess.Hide();
            mFinished.Hide();
            mFavorites.Show();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBillingClient.endConnection();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int i = 0;
        for (String permission : permissions ) {
            if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    findViewById(R.id.splash).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadGallery();
                            findViewById(R.id.splash).setVisibility(View.GONE);
                        }
                    }, 100);
                }
                else ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            i++;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferencesHelper = new SharedPreferencesHelper(this);

        //CHECK VERSION APP
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            if (mSharedPreferencesHelper.getPreferencesAppVersion() == 0) mSharedPreferencesHelper.setPreferencesAppVersion(pInfo.versionCode);
            else if (mSharedPreferencesHelper.getPreferencesAppVersion() < pInfo.versionCode) doPreferencesUpdate();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        mMapCategoriesNamesToTitles = new HashMap<>();
        String[] categoriesMapper = getResources().getStringArray(R.array.categories_map);
        for (int i = 0; i < categoriesMapper.length; i+=2) {
            mMapCategoriesNamesToTitles.put(categoriesMapper[i], categoriesMapper[i+1]);
        }
        //SCREEN_ONE ACTIVITY
        setContentView(R.layout.activity_screen_one);

        //FULL_SCREEN
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //BillingClient
        mBillingClient = BillingClient.newBuilder(this).setListener(this).build();
        mBillingClient.startConnection(this);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.ad_interstitial_id_test));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());


        //CategoriesTitles
        if (mCategoriesTitles == null)
            mCategoriesTitles = getResources().getStringArray(R.array.categories_titles);

        LayoutInflater inflater = getLayoutInflater();

        if (mRootCategories == null) mRootCategories = inflater.inflate(R.layout.vertical_scroll, new LinearLayout(this));
        if (mCategoriesContainer == null) mCategoriesContainer = mRootCategories.findViewById(R.id.vertical_scroll_container);
        for (String categoryName: mCategoriesTitles) {
            View categoryView = inflater.inflate(R.layout.category, mCategoriesContainer, false);
            mCategoriesContainer.addView(categoryView);
            CategoryAdapter categoryAdapter = new CategoryAdapter(categoryView, categoryName);
        }

        //CategoriesSpinner
        mCategoriesSpinner = findViewById(R.id.categories_spinner);
        mCategoriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((ScrollView)(mCategoriesContainer.getParent())).smoothScrollTo(0, mCategoriesContainer.getChildAt(position).getTop());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mCategoriesSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_item, Arrays.asList(mCategoriesTitles)));

        if (mRootWorkshop == null)
            mRootWorkshop = inflater.inflate(R.layout.vertical_scroll, new LinearLayout(this));
        if (mWorkshopContainer == null)
            mWorkshopContainer = mRootWorkshop.findViewById(R.id.vertical_scroll_container);
        if (WorkshopTopButtonsView == null)
            WorkshopTopButtonsView = inflater.inflate(R.layout.workshop, mWorkshopContainer, false);

        //set bold to selected section textView
        if (mTvMyWorks == null)
            mTvMyWorks = WorkshopTopButtonsView.findViewById(R.id.workshop_tv_my_works);
        mTvMyWorks.setOnClickListener(myworksClickListener);
        mTvMyWorks.post(new Runnable() {
            @Override
            public void run() {
                mTvMyWorks.callOnClick();
            }
        });
        if (mTvFavorites == null) {
            mTvFavorites = WorkshopTopButtonsView.findViewById(R.id.menu_second_sub_text);
        }
        mTvFavorites.setOnClickListener(favoritesClickListener);
        mWorkshopContainer.addView(WorkshopTopButtonsView);

        if (mInProcess == null) {
            View cv = getLayoutInflater().inflate(R.layout.category, mWorkshopContainer, false);
            mWorkshopContainer.addView(cv);
            mInProcess = new CategoryAdapter(cv, "В процессе");
            mInProcess.setCategoryAdapterAsSystem();
        }
        if (mFinished == null) {
            View cv = getLayoutInflater().inflate(R.layout.category, mWorkshopContainer, false);
            mWorkshopContainer.addView(cv);
            mFinished = new CategoryAdapter(cv, "Завершенные");
            mFinished.setCategoryAdapterAsSystem();
        }
        if (mFavorites == null) {
            View cv = getLayoutInflater().inflate(R.layout.category, mWorkshopContainer, false);
            mWorkshopContainer.addView(cv);
            mFavorites = new CategoryAdapter(cv, "Избранные");
            mFavorites.setCategoryAdapterAsSystem();
        }


        //PageAdapter
        mViewPager = findViewById(R.id.page_container);
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new ScreenOnePagerAdapter(supportFragmentManager));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                //reset all btns
                Button gallery = findViewById(R.id.button_gallery);
                gallery.setBackground(getResources().getDrawable(R.drawable.bg_unpressed_btn));
                gallery.setTextColor(getResources().getColor(R.color.colorNotActiveButtonText));
                Button workshop = findViewById(R.id.button_workshop);
                workshop.setBackground(getResources().getDrawable(R.drawable.bg_unpressed_btn));
                workshop.setTextColor(getResources().getColor(R.color.colorNotActiveButtonText));
                switch (position) {
                    //! Need use active state
                    case (0):
                        gallery.setBackground(getResources().getDrawable(R.drawable.bg_pressed_btn));
                        gallery.setTextColor(Color.WHITE);
                        mCategoriesSpinner.setVisibility(View.VISIBLE);
                        break;
                    case (1):
                        workshop.setBackground(getResources().getDrawable(R.drawable.bg_pressed_btn));
                        workshop.setTextColor(Color.WHITE);
                        mCategoriesSpinner.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void doPreferencesUpdate() {
    }

    public void showAd() {
        if (mInterstitialAd.isLoaded()) mInterstitialAd.show();
        else Toast.makeText(this,"Not loaded Ad", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        Log.i("purch", "updated");
        if (purchases == null) adIsOff = false;
        else {
            if (purchases.size() == 0) adIsOff = false;
            else adIsOff = true;
        }
        //AdMob
        mAdView = findViewById(R.id.ad_view);
        if (adIsOff) mAdView.setVisibility(View.GONE);
        else {
            MobileAds.initialize(this, ADMOB_APP_ID);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getResources().getString(R.string.ad_banner_id_test));
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
        }
    }

    @Override
    public void onBillingSetupFinished(int responseCode) {
        {
            if (responseCode == BillingClient.BillingResponse.OK) {
                // The billing client is ready. You can query purchases here.
                Purchase.PurchasesResult result = mBillingClient.queryPurchases(BillingClient.SkuType.SUBS);
                if (result.getPurchasesList().size() == 0) adIsOff = false;
                //AdMob
                mAdView = findViewById(R.id.ad_view);

                if (adIsOff) mAdView.setVisibility(View.GONE);
                else {
                    MobileAds.initialize(this, ADMOB_APP_ID);
                    AdRequest adRequest = new AdRequest.Builder().build();
                    mAdView.loadAd(adRequest);

                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                }
            }
        }
    }

    @Override
    public void onBillingServiceDisconnected() {
            // Try to restart the connection on the next request to
            // Google Play by calling the startConnection() method.
    }

    public void subscribeOneMonth() {
        BillingFlowParams.Builder builder = BillingFlowParams
                .newBuilder()
                .setSku("subscribe_month_001").setType(BillingClient.SkuType.SUBS);
        int responseCode = mBillingClient.launchBillingFlow(this, builder.build());
        if (responseCode == BillingClient.BillingResponse.OK) {
            Log.i("s", "s");
        }
    }

    public class ScreenOnePagerAdapter extends FragmentPagerAdapter {

        ScreenOnePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";
        public static LinkedList<PlaceholderFragment> pages = new LinkedList<>();
        public PlaceholderFragment() {
        }
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            pages.add(fragment);
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            int pageNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            final ScreenOne activity = (ScreenOne)getActivity();
            //todo: move init and inflate to onCreate!
            switch (pageNumber) {
                //
                case(1):
                    activity.mRootCategories.setLayoutParams(container.getLayoutParams());
                    container.addView(activity.mRootCategories);
                    boolean permissionOnWrite = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                    if (!permissionOnWrite){
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                    } else {
                        activity.findViewById(R.id.splash).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                activity.loadGallery();
                                activity.findViewById(R.id.splash).setVisibility(View.GONE);
                            }
                        }, 100);
                    }
                    return activity.mRootCategories;
                case(2):
                    activity.mRootWorkshop.setLayoutParams(container.getLayoutParams());
                    container.addView(activity.mRootWorkshop);
                    return activity.mRootWorkshop;
            }
            return new View(getContext());
        }

        @Override
        public void onStart() {
            super.onStart();

        }
    }

    private boolean checkIsColoringNameInExistCategories(String coloringName) {
        //parse category name
        Matcher matcher = Pattern.compile(PATTERN_COLORING_NAME).matcher(coloringName);
        if (matcher.find()) {
            String categoryName = matcher.group(1);
            return mMapCategoriesNamesToTitles.containsKey(categoryName);
        } else {
            return false;
        }
    }

    private String parseCategoryInColoringName(String coloringName) {
        //parse category name
        Matcher matcher = Pattern.compile(PATTERN_COLORING_NAME).matcher(coloringName);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    public void loadGallery() {
        //Collect all include(res)
        ArrayList<String> allColorings = new ArrayList<>();
        Field[] fields=R.drawable.class.getFields();
        for (Field field : fields) {
            String name = field.getName();
            if (checkIsColoringNameInExistCategories(name)) allColorings.add(name);
        }

        //todo: Collect all new on server images and load
        //todo: All add to list allColorings
        //todo: ..

        //Prepare all images in list allColorings
        LayoutInflater inflater = getLayoutInflater();
        for (String coloringName : allColorings) {
            int resource = getResources().getIdentifier(coloringName, "drawable", this.getPackageName());
            String categoryName = parseCategoryInColoringName(coloringName);
            ImageAdapter ia = CategoryAdapter.addGalleryImageIcon(this, coloringName, mMapCategoriesNamesToTitles.get(categoryName));
            //Prepare or not prepare..
            if (!mSharedPreferencesHelper.isColoringPrepared(coloringName) || DEBUG_ALWAYS_PREPARE_COLORING) {
                ia.setImageByResourceId(resource);
                mSharedPreferencesHelper.setColoringIsPrepared(coloringName);
            }

            ia.initProgressOfColoring();
            if (ia.mIsWorkStarted) {
                if (ia.mIsFinished)
                    mFinished.addToCategory(ia);
                else mInProcess.addToCategory(ia);
            }
        }
    }

    public void tabClick(View v) {
        int tabNumber = 0;
        switch (v.getId()) {
            case(R.id.button_gallery):
                tabNumber = 0;
                break;
            case(R.id.button_workshop):
                tabNumber = 1;
                break;
        }
        mViewPager.setCurrentItem(tabNumber);
    }

    public void addClick(View v) {
        // close existing dialog fragments
        android.app.FragmentManager manager = getFragmentManager();
        android.app.Fragment frag = manager.findFragmentByTag("add_fragment");
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        switch (v.getId()) {
            case R.id.add_btn:
                DialogFragmentAdd addDialog = new DialogFragmentAdd();
                addDialog.show(manager, "add_fragment");
                break;
        }
    }

    public void showPreview(CategoryAdapter categoryAdapter) {
        // close existing dialog fragments
        android.app.FragmentManager manager = getFragmentManager();
        android.app.Fragment frag = manager.findFragmentByTag("preview_fragment");
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        DialogFragmentPreview previewDialog = new DialogFragmentPreview();
        previewDialog.setmCurrentCategoryAdapter(categoryAdapter);
        previewDialog.show(manager, "preview_fragment");
    }

    public void menuClick(View v) {
        // close existing dialog fragments
        android.app.FragmentManager manager = getFragmentManager();
        android.app.Fragment frag = manager.findFragmentByTag("menu_fragment");
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        switch (v.getId()) {
            case R.id.menu_btn:
                DialogFragmentMenu menuDialog = new DialogFragmentMenu();
                menuDialog.show(manager, "menu_fragment");
                break;
        }
    }

    public void cameraClick() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    public void loadFromGalleryClick() {
    }

    static public File getOutputMediaFile(String directoryName, String mImageName, Context context){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + context.getPackageName()
                + "/" + directoryName);

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }


        File mediaFile;
        String path = mediaStorageDir.getPath() + File.separator + mImageName;
        mediaFile = new File(path);
        //Log.i("eve", "Touch file:" + path);
        return mediaFile;
    }

    static public void storeImage(Bitmap image, File pictureFile) {
        String TAG = "storeImage";
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case COLORING_REQUEST_CODE:
                if(resultCode == RESULT_OK){
                    boolean hasNewColoredPixels = data.getBooleanExtra("hasNewColoredPixels", false);
                    if (hasNewColoredPixels) {
                        ImageAdapter.currentColoringImageAdapter.reinitAfterColoring();
                        showAd();
                    }
                }
                break;
        }
    }
}
