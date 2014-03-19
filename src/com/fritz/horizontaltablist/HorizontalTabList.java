package com.fritz.horizontaltablist;

/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (C) 2011 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;


/**
 * 
 * Extends TabPageIndicator by Jake Wharton. 
 * 
 * It allows to create a horizontal tab list which center the selected
 * item.
 * Each tab view can be customized.
 * 
 * 
 * @author Mauro Frezza
 * 
 * @version 0.1
 * 
 * */
public abstract class HorizontalTabList extends HorizontalScrollView  {
    

    /** Tablist layout */
    protected final IcsLinearLayout mTabLayout;

    /** Index of the selected tab */
    protected int mSelectedTabIndex;

    /** Listener for click on a tab */
    protected OnTabClickListener tabListener;
    
    /** List of items */
    protected ArrayList<?> items;
    
    /** Thread to animate the selection of a tab */
    protected Runnable mTabSelector;

    /**
     * Interface for a callback when the selected tab has been reselected.
     */
    public interface OnTabClickListener {
        /**
         * Callback when the selected tab has been reselected.
         *
         * @param position Position of the current center item.
         */
        void onTabReselected(int position);
        
        
        void onTabClick(int position);
    }

    

    /**
     * Callback when a tab has been clicked
     * 
     * */
    private final OnClickListener mTabClickListener = new OnClickListener() {
        public void onClick(View view) {
                    
            final int oldSelected = mSelectedTabIndex;
            
            // Getting the position of the view in the list
            final int newSelected = (Integer) view.getTag();
            
            if (oldSelected == newSelected && tabListener != null) {
                tabListener.onTabReselected(newSelected);
            } else {
            	tabListener.onTabClick(newSelected);
            }
            
            setCurrentItem(newSelected);
        }
    };


    public HorizontalTabList(Context context) {
        this(context, null);
    }

    public HorizontalTabList(Context context, AttributeSet attrs) {
        super(context, attrs);
        setHorizontalScrollBarEnabled(false);

        mTabLayout = new IcsLinearLayout(context, R.attr.vpiTabPageIndicatorStyle);
        addView(mTabLayout, new ViewGroup.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
    }

    public void setOnTabReselectedListener(OnTabClickListener listener) {
        tabListener = listener;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final boolean lockedExpanded = widthMode == MeasureSpec.EXACTLY;
        setFillViewport(lockedExpanded);

        final int oldWidth = getMeasuredWidth();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int newWidth = getMeasuredWidth();

        if (lockedExpanded && oldWidth != newWidth) {
            // Recenter the tab display if we're at a new (scrollable) size.
            setCurrentItem(mSelectedTabIndex);
        }
    }

    /**
     * Deselect item at a certain position
     * 
     * @param position Position of the item to deselect
     * 
     * */
    private void deselectItemAtPosition(int position){
    	LinearLayout view = (LinearLayout) mTabLayout.getChildAt(position);
        if(view == null){
        	return;
        }
        removeHighlightFromView(view);
    }
    
    /**
     * Select item at a certain position
     * 
     * @param position Position of the item to select
     * 
     * */
    protected void selectItemAtPosition(int position){
    	LinearLayout view = (LinearLayout) mTabLayout.getChildAt(position);
    	if(view == null){
    		return;
    	}
    	highlightView(view);
    }
    
    /**
     * This method highlight the selected view
     * 
     * @param view View to hightlight
     * 
     * */
    protected void highlightView(View view){
    	
    }
    
    /**
     * Called when a previous selected tab lose selection
     * 
     * @param view View that lose selection
     * 
     * */
    protected void removeHighlightFromView(View view){
    	
    }
    
    private void animateToTab(final int position) {
        final View tabView = mTabLayout.getChildAt(position);
        if (mTabSelector != null) {
            removeCallbacks(mTabSelector);
        }
        mTabSelector = new Runnable() {
            public void run() {
                final int scrollPos = tabView.getLeft() - (getWidth() - tabView.getWidth()) / 2;
                smoothScrollTo(scrollPos, 0);
                mTabSelector = null;
            }
        };
        post(mTabSelector);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mTabSelector != null) {
            // Re-post the selector we saved
            post(mTabSelector);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mTabSelector != null) {
            removeCallbacks(mTabSelector);
        }
    }

    /**
     * Adds a tab with the selected view at the index position
     * 
     * @param index Indice della view all'interno della lista
     * 
     * */
    protected void addTab(int index, View view, LayoutParams lp) {
    	view.setOnClickListener(mTabClickListener);
        mTabLayout.addView(view, lp);
    }
    
    protected abstract View getView(int position, Object item);
    
    protected abstract LayoutParams getTabViewLayoutParams();

    
    /**
     * Returns the selected item
     * 
     * @return the current selected item 
     * 
     * */
    public Object getSelectedItem(){
    	if(items != null && items.size() > 0){
    		return items.get(mSelectedTabIndex);
    	} else {
    		return null;
    	}
    	
    }
    
    /**
     * Returns the item at the position
     * 
     * @param position Position of the item to select
     * 
     * @return item at the selected position
     * 
     * */
    public Object getItemAtPosition(int position){
    	   	
    	if(items != null && items.size() > 0){
    		
    		if(position < 0 || position > items.size()){
        		throw new ArrayIndexOutOfBoundsException("Invalid position " + position + " on a list of size " + items.size());
        	}
    		
    		return items.get(position);
    	} else {
    		throw new NullPointerException("No items in list");
    	}
    }
    
    /**
     * Returns all the items in the list
     * 
     * @return items
     * 
     * */
    public ArrayList<?> getListItems(){
    	return items;
    }
    
    /**
     * Set the items to the list and update it
     * 
     * @param items Items to insert in the list
     * 
     * */
    public void setItems(ArrayList<?> items){
    	this.items = items;
    	notifyDataSetChanged();
    }
    
    /**
     * Set items into the list and set the list to a position
   	 * 
   	 * @param items Items to insert in the list
   	 * @param currentPosition Position to set the list
     * 
     * */
    public void setItems(ArrayList<?> items, int currentPosition){
    	if(currentPosition < 0 || currentPosition > items.size()){
    		throw new ArrayIndexOutOfBoundsException("Invalid position " + currentPosition + " on a list of size " + items.size());
    	}
    	
    	this.items = items;
    	setCurrentItem(currentPosition);
    	notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        mTabLayout.removeAllViews();
       
        int count = items.size();
        for (int i = 0; i < count; i++) {
            addTab(i, getView(i, items.get(i)), getTabViewLayoutParams());
        }
        if (mSelectedTabIndex > count) {
            mSelectedTabIndex = count - 1;
        }
        setCurrentItem(mSelectedTabIndex);
        requestLayout();
    }
  

    /**
     * Set the list to a selected position
     * 
     * @param position Position on which to set the list
     * 
     * */
    public void setCurrentItem(int position) { 
    	deselectItemAtPosition(mSelectedTabIndex);
        mSelectedTabIndex = position;
   
        for (int i = 0, tabCount = mTabLayout.getChildCount(); i < tabCount; i++) {
            final View child = mTabLayout.getChildAt(i);
            final boolean isSelected = (i == position);
            child.setSelected(isSelected);
            if (isSelected) {
                animateToTab(position);
            }
        }
        // Evidenzio l'elemento
        selectItemAtPosition(position);
    }

  
}
