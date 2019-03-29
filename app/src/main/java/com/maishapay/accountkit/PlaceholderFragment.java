package com.maishapay.accountkit;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PlaceholderFragment extends Fragment {
    private static final String HEIGHT_KEY = "height";
    private static final String TEXT_KEY = "text";
    private static final String TEXT_RESOURCE_ID_KEY = "textResourceId";

    private View.OnClickListener onClickListener;

    public static PlaceholderFragment create(final int height, final String text) {
        final PlaceholderFragment fragment = new PlaceholderFragment();
        final Bundle arguments = new Bundle();
        arguments.putInt(HEIGHT_KEY, height);
        arguments.putString(TEXT_KEY, text);
        fragment.setArguments(arguments);
        return fragment;
    }

    public static PlaceholderFragment create(final int height, final int textResourceId) {
        final PlaceholderFragment fragment = new PlaceholderFragment();
        final Bundle arguments = new Bundle();
        arguments.putInt(HEIGHT_KEY, height);
        arguments.putInt(TEXT_RESOURCE_ID_KEY, textResourceId);
        fragment.setArguments(arguments);
        return fragment;
    }

    public void setOnClickListener(final View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        final View view = getView();
        if (view != null) {
            view.setOnClickListener(onClickListener);
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            final LayoutInflater inflater,
            final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_placeholder, container, false);
        }

        final Bundle arguments = getArguments();
        if (arguments != null) {
            final int height = arguments.getInt(HEIGHT_KEY, -1);
            if (height >= 0) {
                view.getLayoutParams().height = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        height,
                        getResources().getDisplayMetrics());
            }

            final String text = arguments.getString(TEXT_KEY);
            final int textResourceId = arguments.getInt(TEXT_RESOURCE_ID_KEY, -1);
            final TextView textView = (TextView) view.findViewById(R.id.text_view);
            if (textView != null) {
                if (text != null) {
                    textView.setText(text);
                } else if (textResourceId > 0) {
                    textView.setText(textResourceId);
                }
            }
        }

        if (onClickListener != null) {
            view.setOnClickListener(onClickListener);
        }

        return view;
    }
}
