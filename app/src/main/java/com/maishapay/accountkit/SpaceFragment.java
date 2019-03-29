package com.maishapay.accountkit;

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SpaceFragment extends InflateFragment {

    private static final String HEIGHT_ATTRIBUTE_KEY = "heightAttribute";

    private AttributeSet attributes;

    public static SpaceFragment create(final int heightAttribute) {
        final SpaceFragment fragment = new SpaceFragment();
        final Bundle arguments = new Bundle();
        arguments.putInt(HEIGHT_ATTRIBUTE_KEY, heightAttribute);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_space, container, false);
        }
        updateHeight(view);
        return view;
    }

    @Override
    protected void handleAttributes(final AttributeSet attrs) {
        attributes = attrs;
        updateHeight(getView());
    }

    private void updateHeight(final View view) {
        if (view == null) {
            return;
        }
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        final Bundle arguments = getArguments();
        if (arguments == null) {
            return;
        }

        final int heightAttribute = arguments.getInt(HEIGHT_ATTRIBUTE_KEY, -1);
        if (heightAttribute >= 0) {
            final TypedArray a = activity.obtainStyledAttributes(
                    attributes,
                    R.styleable.Theme_AccountKitSample_Style);
            final int heightAttributeValue = a.getDimensionPixelSize(heightAttribute, -1);
            a.recycle();
            if (heightAttributeValue >= 0) {
                view.getLayoutParams().height = heightAttributeValue;
            }
        }
    }
}
