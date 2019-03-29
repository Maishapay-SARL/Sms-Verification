package com.maishapay.accountkit;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.accountkit.AccountKit;

public class CodeActivity extends Activity {
    static final String HELLO_CODE_ACTIVITY_CODE_EXTRA = "HELLO_CODE_ACTIVITY_CODE_EXTRA";
    static final String HELLO_CODE_ACTIVITY_FINAL_STATE_EXTRA =
            "HELLO_CODE_ACTIVITY_FINAL_STATE_EXTRA";
    static final String HELLO_CODE_ACTIVITY_INITIAL_STATE_EXTRA =
            "HELLO_CODE_ACTIVITY_INITIAL_STATE_EXTRA";

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_code);

        final String code = getIntent().getStringExtra(HELLO_CODE_ACTIVITY_CODE_EXTRA);
        final String finalState =
                getIntent().getStringExtra(HELLO_CODE_ACTIVITY_FINAL_STATE_EXTRA);
        final String initialState =
                getIntent().getStringExtra(HELLO_CODE_ACTIVITY_INITIAL_STATE_EXTRA);

        if (code != null) {
            final TextView codeView = (TextView) findViewById(R.id.code);
            if (codeView != null) {
                codeView.setText(code);
            }
        }

        if (initialState != null) {
            final TextView initialStateView = (TextView) findViewById(R.id.initial_state);
            final TextView initialStateLabelView =
                    (TextView) findViewById(R.id.initial_state_label);
            if (initialStateView != null && initialStateLabelView != null) {
                initialStateView.setText(initialState);
                initialStateView.setVisibility(View.VISIBLE);
                initialStateLabelView.setVisibility(View.VISIBLE);
            }
        }

        if (finalState != null) {
            final TextView finalStateView = (TextView) findViewById(R.id.final_state);
            final TextView finalStateLabelView =
                    (TextView) findViewById(R.id.final_state_label);
            if (finalStateView != null && finalStateLabelView != null) {
                finalStateView.setText(finalState);
                finalStateView.setVisibility(View.VISIBLE);
                finalStateLabelView.setVisibility(View.VISIBLE);
            }
        }

        final Button signOut = (Button) findViewById(R.id.log_out_button);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccountKit.logOut();
                finish();
            }
        });
    }
}
