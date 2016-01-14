package com.aluxian.butler.main;

import com.aluxian.butler.MainActivity;

/**
 * Abstract class to be extended by classes which need a reference to MainActivity
 */
public abstract class MainActivityDelegate {

    /** MainActivity instance */
    public transient final MainActivity mainActivity;

    public MainActivityDelegate(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

}
