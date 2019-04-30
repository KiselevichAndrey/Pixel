package com.pixelfunapp.pixelpart;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.HashMap;
import java.util.LinkedList;

public class CategoryAdapter {
    private static final int ROWS = 2;

    private static HashMap<String, CategoryAdapter> MapCategoryAdaptersByTitle = new HashMap<>();
    private static LinkedList<CategoryAdapter> SystemCategoryAdapters = new LinkedList<>();

    private View mCategoryView;
    private TextView mTitleTextView;
    private LinkedList<ImageAdapter> mImageAdaptersList;



    CategoryAdapter(View v, String name) {
        init(v);
        setTitle(name);
        MapCategoryAdaptersByTitle.put(name, this);
    }

    public void setCategoryAdapterAsSystem() {
        SystemCategoryAdapters.push(this);
        mTitleTextView.setVisibility(View.GONE);
    }

    public static CategoryAdapter getNotSystemCategoryAdapterByImageAdapter(ImageAdapter ia) {
        for (CategoryAdapter ca : MapCategoryAdaptersByTitle.values()) {
            //exclude system categories
            if (SystemCategoryAdapters.contains(ca)) continue;

            if (ca.mImageAdaptersList.contains(ia)) return ca;
        }
        return null;
    }

    public ImageAdapter getNext(ImageAdapter currentImageAdapter) {
        if (mImageAdaptersList.getLast() == currentImageAdapter) return mImageAdaptersList.getFirst();
        boolean needNext = false;
        for (ImageAdapter ia : mImageAdaptersList) {
            if (needNext) return ia;
            if (ia == currentImageAdapter) needNext = true;
        }
        return null;
    }

    public ImageAdapter getPrevious(ImageAdapter currentImageAdapter) {
        if (mImageAdaptersList.getFirst() == currentImageAdapter) return mImageAdaptersList.getLast();
        ImageAdapter previousImageAdapter = null;
        for (ImageAdapter ia : mImageAdaptersList) {
            if (ia == currentImageAdapter) return previousImageAdapter;
            previousImageAdapter = ia;
        }
        return null;
    }

    public void Hide() {
        mCategoryView.setVisibility(View.GONE);
    }

    public void Show() {
        mCategoryView.setVisibility(View.VISIBLE);
    }

    private void init(View v) {
        mCategoryView = v;
        mTitleTextView = mCategoryView.findViewById(R.id.category_title);
        mImageAdaptersList = new LinkedList<>();
    }

    public boolean isTop() {
        return mCategoryView == ((ViewGroup) mCategoryView.getParent()).getChildAt(0);
    }


    //todo: change this construction!
    static public ImageAdapter addGalleryImageIcon(Activity activity, String coloringName, String coloringCategory) {
        //Definition coloringCategory
        CategoryAdapter ca = MapCategoryAdaptersByTitle.get(coloringCategory);
        //Definition row
        TableRow row = ca.getRow(ca.mImageAdaptersList.size() % ROWS);

        ImageAdapter imageAdapter = new ImageAdapter(activity, coloringName ,coloringCategory);
        assert row != null;
        row.addView(imageAdapter.mCategoryIcon);

        ca.mImageAdaptersList.add(imageAdapter);
        return imageAdapter;
    }

    public void addToCategory(ImageAdapter imageAdapter) {
        TableRow row = getRow(mImageAdaptersList.size() % ROWS);
        row.addView(imageAdapter.getIconForCategoryAdapter(this));
        mImageAdaptersList.add(imageAdapter);
        if (mTitleTextView.getVisibility() == mTitleTextView.GONE) mTitleTextView.setVisibility(View.VISIBLE);
    }

    public void removeFromCategory(ImageAdapter imageAdapter) {
        mImageAdaptersList.remove(imageAdapter);
        clearRows();
        int i = 0;
        TableRow row;
        for (ImageAdapter ia : mImageAdaptersList) {
            row = getRow(i % ROWS);
            row.addView(ia.getIconForCategoryAdapter(this));
            i++;
        }
        if (mImageAdaptersList.size() == 0) mTitleTextView.setVisibility(View.GONE);
    }

    private ViewGroup getRowsContainer() {
        return mCategoryView.findViewById(R.id.table_rows_container);
    }
    private int getRowsNumber() {
        return getRowsContainer().getChildCount();
    }

    private void clearRows() {
        for (int i = 0; i < getRowsNumber(); i++) {
            getRow(i).removeAllViews();
        }
    }

    private void setTitle(String n) {
        mTitleTextView.setText(n);
    }

    private TableRow getRow(int rowNumber) {
        return (TableRow)(rowNumber > getRowsNumber() ? null : getRowsContainer().getChildAt(rowNumber));
    }

}
