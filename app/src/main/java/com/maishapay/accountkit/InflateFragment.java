package com.maishapay.accountkit;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;

@SuppressWarnings("All")
class InflateFragment extends Fragment {
    @TargetApi(11)
    @Override
    public void onInflate(final AttributeSet attrs, final Bundle savedInstanceState) {
        super.onInflate(attrs, savedInstanceState);
        handleAttributes(attrs);
    }

    @TargetApi(21)
    @Override
    public void onInflate(
            final Activity activity,
            final AttributeSet attrs,
            final Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        handleAttributes(attrs);
    }

    @TargetApi(23)
    @Override
    public void onInflate(
            final Context context,
            final AttributeSet attrs,
            final Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        handleAttributes(attrs);
    }

    protected void handleAttributes(final AttributeSet attrs) {
    }
}
