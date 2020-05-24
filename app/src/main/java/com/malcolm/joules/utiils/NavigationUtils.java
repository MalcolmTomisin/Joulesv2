package com.malcolm.joules.utiils;

import android.app.Activity;
import android.util.Pair;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class NavigationUtils {
    public static void navigateToAlbum(Activity context, long albumID, Pair<View, String> transitionViews){
        FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
        Fragment fragment;
    }
}
