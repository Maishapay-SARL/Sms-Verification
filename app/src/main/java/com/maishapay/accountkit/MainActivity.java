/*
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 * copy, modify, and distribute this software in source code or binary form for use
 * in connection with the web services and APIs provided by Facebook.
 *
 * As with any software that integrates with the Facebook platform, your use of
 * this software is subject to the Facebook Developer Principles and Policies
 * [http://developers.facebook.com/policy/]. This copyright notice shall be
 * included in all copies or substantial portions of the software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.maishapay.accountkit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.PhoneNumber;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.ButtonType;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.accountkit.ui.SkinManager;
import com.facebook.accountkit.ui.TextPosition;
import com.facebook.accountkit.ui.ThemeUIManager;
import com.facebook.accountkit.ui.UIManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends Activity {
    private static final int FRAMEWORK_REQUEST_CODE = 1;

    private static final @DrawableRes
    int DEFAULT_SKIN_BACKGROUND_IMAGE = R.drawable.dog;
    private static final int TINT_SEEKBAR_ADJUSTMENT = 55;

    private Switch advancedUISwitch;
    private SkinManager.Skin skin;
    private Switch skinBackgroundImage;
    private TextView skinBackgroundTintIntensityTitle;
    private SeekBar skinBackgroundTintIntensity;
    private Spinner skinTintSpinner;
    private ButtonType confirmButton;
    private ButtonType entryButton;
    private String initialStateParam;
    private int nextPermissionsRequestCode = 4000;
    private final SparseArray<OnCompleteListener> permissionsListeners = new SparseArray<>();
    private @StyleRes
    int selectedThemeId = -1;
    private @DrawableRes
    int selectedBackgroundId = -1;
    private BroadcastReceiver switchLoginTypeReceiver;
    private TextPosition textPosition;

    private interface OnCompleteListener {
        void onComplete();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if we're logged in already when the app is first started.
        if (AccountKit.getCurrentAccessToken() != null && savedInstanceState == null) {
            showHelloActivity(null);
        }

        final View skinUIOptionsLayout = setupSkinUIOptions();
        skinBackgroundTintIntensityTitle =
                (TextView) skinUIOptionsLayout.findViewById(R.id.tint_intensity_title);
        skinBackgroundTintIntensity =
                (SeekBar) skinUIOptionsLayout.findViewById(R.id.tint_intensity_field);
        skinBackgroundTintIntensity.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(
                            final SeekBar seekBar,
                            final int progress,
                            final boolean fromUser) {
                        skinBackgroundTintIntensityTitle.setText(getString(
                                R.string.config_tint_intensity_label,
                                progress + TINT_SEEKBAR_ADJUSTMENT));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) { /* no op */ }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) { /* no op */ }
                });
        skinBackgroundTintIntensity.setProgress(75 - TINT_SEEKBAR_ADJUSTMENT);

        skinBackgroundImage = (Switch) findViewById(R.id.background_image);
        skinBackgroundImage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                skinUIOptionsLayout.setVisibility(
                        (isSkinSelected() && getBackgroundImage() >= 0) ?
                                View.VISIBLE :
                                View.GONE);
            }
        });
        final Spinner themeSpinner = (Spinner) findViewById(R.id.theme_spinner);
        if (themeSpinner != null) {
            final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    this,
                    R.array.theme_options,
                    android.R.layout.simple_spinner_dropdown_item);
            themeSpinner.setAdapter(adapter);
            themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @SuppressLint("ResourceType")
                @Override
                public void onItemSelected(
                        final AdapterView<?> parent,
                        final View view,
                        final int position,
                        final long id) {

                    // init defaults
                    skin = SkinManager.Skin.NONE;
                    selectedThemeId = -1;
                    selectedBackgroundId = -1;

                    switch (position) {
                        case 0:
                            skin = SkinManager.Skin.CLASSIC;
                            advancedUISwitch.setChecked(false);
                            break;
                        case 1:
                            skin = SkinManager.Skin.CONTEMPORARY;
                            advancedUISwitch.setChecked(false);
                            break;
                        case 2:
                            selectedThemeId = -1;
                            skin = SkinManager.Skin.TRANSLUCENT;
                            advancedUISwitch.setChecked(false);
                            break;
                        case 3:
                            selectedThemeId = R.style.AppLoginTheme_Salmon;
                            break;
                        case 4:
                            selectedThemeId = R.style.AppLoginTheme_Yellow;
                            break;
                        case 5:
                            selectedThemeId = R.style.AppLoginTheme_Red;
                            break;
                        case 6:
                            skin = SkinManager.Skin.CLASSIC;
                            advancedUISwitch.setChecked(false);
                            selectedBackgroundId = R.drawable.dog;
                            break;
                        case 7:
                            selectedThemeId = R.style.AppLoginTheme_Bicycle;
                            break;
                        case 8:
                            selectedThemeId = R.style.AppLoginTheme_Reverb_A;
                            advancedUISwitch.setChecked(true);
                            break;
                        case 9:
                            selectedThemeId = R.style.AppLoginTheme_Reverb_B;
                            advancedUISwitch.setChecked(true);
                            break;
                        case 10:
                            selectedThemeId = R.style.AppLoginTheme_Reverb_C;
                            advancedUISwitch.setChecked(true);
                            break;
                        default:
                            advancedUISwitch.setChecked(false);
                            break;
                    }

                    skinBackgroundImage.setVisibility(isSkinSelected() ? View.VISIBLE : View.GONE);
                    skinUIOptionsLayout.setVisibility(
                            (isSkinSelected() && getBackgroundImage() >= 0) ?
                                    View.VISIBLE :
                                    View.GONE);
                }

                @Override
                public void onNothingSelected(final AdapterView<?> parent) {
                    selectedThemeId = -1;
                    selectedBackgroundId = -1;
                    skin = SkinManager.Skin.NONE;
                    advancedUISwitch.setChecked(false);
                    skinUIOptionsLayout.setVisibility(View.GONE);
                    skinBackgroundImage.setVisibility(View.GONE);
                }
            });
        }
        setupAdvancedUIOptions();
    }


    private void setupAdvancedUIOptions() {

        advancedUISwitch = (Switch) findViewById(R.id.advanced_ui_switch);

        final View advancedUIOptionsLayout = findViewById(R.id.advanced_ui_options);

        final List<CharSequence> buttonNames = new ArrayList<>();
        buttonNames.add("Default");
        for (ButtonType buttonType : ButtonType.values()) {
            buttonNames.add(buttonType.name());
        }
        final ArrayAdapter<CharSequence> buttonNameAdapter
                = new ArrayAdapter<>(
                MainActivity.this,
                android.R.layout.simple_spinner_dropdown_item,
                buttonNames);

        advancedUISwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (isSkinSelected()) {
                        advancedUISwitch.setChecked(false);
                        Toast.makeText(
                                MainActivity.this,
                                R.string.skin_ui_required,
                                Toast.LENGTH_LONG)
                                .show();
                        return;
                    }

                    advancedUIOptionsLayout.setVisibility(View.VISIBLE);

                    final Spinner entryButtonSpinner =
                            (Spinner) findViewById(R.id.entry_button_spinner);
                    if (entryButtonSpinner != null) {
                        entryButtonSpinner.setAdapter(buttonNameAdapter);
                        entryButtonSpinner.setOnItemSelectedListener(
                                new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(
                                            final AdapterView<?> parent,
                                            final View view,
                                            final int position,
                                            final long id) {
                                        // First position is empty, so anything past that
                                        if (position > 0) {
                                            entryButton = ButtonType.valueOf(
                                                    entryButtonSpinner
                                                            .getSelectedItem()
                                                            .toString());
                                        } else {
                                            entryButton = null;
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected(final AdapterView<?> parent) {
                                        entryButton = null;
                                    }
                                });
                    }

                    final Spinner confirmButtonSpinner =
                            (Spinner) findViewById(R.id.confirm_button_spinner);
                    if (confirmButtonSpinner != null) {
                        confirmButtonSpinner.setAdapter(buttonNameAdapter);
                        confirmButtonSpinner.setOnItemSelectedListener(
                                new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(
                                            final AdapterView<?> parent,
                                            final View view,
                                            final int position,
                                            final long id) {
                                        // First position is empty, so anything past
                                        // that
                                        if (position > 0) {
                                            confirmButton = ButtonType.valueOf(
                                                    confirmButtonSpinner
                                                            .getSelectedItem()
                                                            .toString());
                                        } else {
                                            confirmButton = null;
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected(
                                            final AdapterView<?> parent) {
                                        confirmButton = null;
                                    }
                                });
                    }

                    final Spinner textPositionSpinner =
                            (Spinner) findViewById(R.id.text_position_spinner);
                    if (textPositionSpinner != null) {
                        final List<CharSequence> textPositions = new ArrayList<>();
                        textPositions.add("Default");
                        for (TextPosition textPosition : TextPosition.values()) {
                            textPositions.add(textPosition.name());
                        }
                        final ArrayAdapter<CharSequence> textPositionAdapter
                                = new ArrayAdapter<>(
                                MainActivity.this,
                                android.R.layout.simple_spinner_dropdown_item,
                                textPositions);

                        textPositionSpinner.setAdapter(textPositionAdapter);
                        textPositionSpinner.setOnItemSelectedListener(
                                new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(
                                            final AdapterView<?> parent,
                                            final View view,
                                            final int position,
                                            final long id) {
                                        // First position is empty, so anything past
                                        // that
                                        if (position > 0) {
                                            textPosition = TextPosition.valueOf(
                                                    textPositionSpinner
                                                            .getSelectedItem()
                                                            .toString());
                                        } else {
                                            textPosition = null;
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected(
                                            final AdapterView<?> parent) {
                                        textPosition = null;
                                    }
                                });
                    }
                } else if (isReverbThemeSelected()) {
                    advancedUISwitch.setChecked(true);
                    Toast.makeText(
                            MainActivity.this,
                            R.string.reverb_advanced_ui_required,
                            Toast.LENGTH_LONG)
                            .show();
                } else {
                    advancedUIOptionsLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    private View setupSkinUIOptions() {
        final View skinLayout = findViewById(R.id.skin_ui_options);
        skinTintSpinner = (Spinner) skinLayout.findViewById(R.id.skin_tint_spinner);

        final List<CharSequence> tints = new ArrayList<>();
        for (SkinManager.Tint tint : SkinManager.Tint.values()) {
            tints.add(tint.toString());
        }

        final ArrayAdapter<CharSequence> skinTintAdapter = new ArrayAdapter<>(
                MainActivity.this,
                android.R.layout.simple_spinner_dropdown_item,
                tints);
        skinTintSpinner.setAdapter(skinTintAdapter);

        return skinLayout;
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(switchLoginTypeReceiver);

        super.onDestroy();
    }

    public void onLoginEmail(final View view) {
        onLogin(LoginType.EMAIL);
    }

    public void onLoginPhone(final View view) {
        onLogin(LoginType.PHONE);
    }

    @Override
    protected void onActivityResult(
            final int requestCode,
            final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != FRAMEWORK_REQUEST_CODE) {
            return;
        }

        final String toastMessage;
        final AccountKitLoginResult loginResult = AccountKit.loginResultWithIntent(data);
        if (loginResult == null || loginResult.wasCancelled()) {
            toastMessage = "Login Cancelled";
        } else if (loginResult.getError() != null) {
            toastMessage = loginResult.getError().getErrorType().getMessage();
            showErrorActivity(loginResult.getError());
        } else {
            final AccessToken accessToken = loginResult.getAccessToken();
            final String authorizationCode = loginResult.getAuthorizationCode();
            final long tokenRefreshIntervalInSeconds =
                    loginResult.getTokenRefreshIntervalInSeconds();
            if (accessToken != null) {
                toastMessage = "Success:" + accessToken.getAccountId()
                        + tokenRefreshIntervalInSeconds;
                showHelloActivity(loginResult.getFinalAuthorizationState());
            } else if (authorizationCode != null) {
                toastMessage = String.format(
                        "Success:%s...",
                        authorizationCode.substring(0, 10));
                showHelloActivity(authorizationCode, loginResult.getFinalAuthorizationState());
            } else {
                toastMessage = "Unknown response type";
            }
        }

        Toast.makeText(
                this,
                toastMessage,
                Toast.LENGTH_LONG)
                .show();
    }

    private AccountKitActivity.ResponseType getResponseType() {
        final Switch responseTypeSwitch = (Switch) findViewById(R.id.response_type_switch);
        if (responseTypeSwitch != null && responseTypeSwitch.isChecked()) {
            return AccountKitActivity.ResponseType.TOKEN;
        } else {
            return AccountKitActivity.ResponseType.CODE;
        }
    }

    private AccountKitConfiguration.AccountKitConfigurationBuilder createAccountKitConfiguration(
            final LoginType loginType) {
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder
                = new AccountKitConfiguration.AccountKitConfigurationBuilder(
                loginType,
                getResponseType());
        final Switch stateParamSwitch = (Switch) findViewById(R.id.state_param_switch);
        final Switch facebookNotificationsSwitch =
                (Switch) findViewById(R.id.facebook_notification_switch);
        final Switch voiceCallSwitch =
                (Switch) findViewById(R.id.voice_call_switch);
        final Switch useManualWhiteListBlacklist =
                (Switch) findViewById(R.id.whitelist_blacklist_switch);
        final Switch readPhoneStateSwitch =
                (Switch) findViewById(R.id.read_phone_state_switch);
        final Switch receiveSMS =
                (Switch) findViewById(R.id.receive_sms_switch);

        final UIManager uiManager;
        if (advancedUISwitch != null && advancedUISwitch.isChecked()) {
            if (isReverbThemeSelected()) {
                if (switchLoginTypeReceiver == null) {
                    switchLoginTypeReceiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(final Context context, final Intent intent) {
                            final String loginTypeString
                                    = intent.getStringExtra(ReverbUIManager.LOGIN_TYPE_EXTRA);
                            if (loginTypeString == null) {
                                return;
                            }

                            final LoginType loginType = LoginType.valueOf(loginTypeString);
                            onLogin(loginType);
                        }
                    };
                    LocalBroadcastManager.getInstance(getApplicationContext())
                            .registerReceiver(
                                    switchLoginTypeReceiver,
                                    new IntentFilter(ReverbUIManager.SWITCH_LOGIN_TYPE_EVENT));
                }
                uiManager = new ReverbUIManager(
                        confirmButton,
                        entryButton,
                        loginType,
                        textPosition,
                        selectedThemeId);
            } else {
                uiManager = new AccountKitSampleAdvancedUIManager(
                        confirmButton,
                        entryButton,
                        textPosition,
                        loginType);
            }
        } else if (isSkinSelected()) {
            final @ColorInt int primaryColor = ContextCompat.getColor(this, R.color.default_color);
            if (getBackgroundImage() >= 0) {
                uiManager = new SkinManager(
                        skin,
                        primaryColor,
                        getBackgroundImage(),
                        getSkinTintOption(),
                        getSkinBackgroundTintIntensity());
            } else {
                uiManager = new SkinManager(skin, primaryColor);
            }
        } else {
            uiManager = new ThemeUIManager(selectedThemeId);
        }

        configurationBuilder.setUIManager(uiManager);

        if (stateParamSwitch != null && stateParamSwitch.isChecked()) {
            initialStateParam = UUID.randomUUID().toString();
            configurationBuilder.setInitialAuthState(initialStateParam);
        }

        if (facebookNotificationsSwitch != null && !facebookNotificationsSwitch.isChecked()) {
            configurationBuilder.setFacebookNotificationsEnabled(false);
        }
        if (voiceCallSwitch != null && !voiceCallSwitch.isChecked()) {
            configurationBuilder.setVoiceCallbackNotificationsEnabled(false);
        }

        if (useManualWhiteListBlacklist != null && useManualWhiteListBlacklist.isChecked()) {
            final String[] blackList
                    = getResources().getStringArray(R.array.blacklistedSmsCountryCodes);
            final String[] whiteList
                    = getResources().getStringArray(R.array.whitelistedSmsCountryCodes);
            configurationBuilder.setSMSBlacklist(blackList);
            configurationBuilder.setSMSWhitelist(whiteList);
        }

        if (readPhoneStateSwitch != null && !(readPhoneStateSwitch.isChecked())) {
            configurationBuilder.setReadPhoneStateEnabled(false);
        }

        if (receiveSMS != null && !receiveSMS.isChecked()) {
            configurationBuilder.setReceiveSMS(false);
        }

        return configurationBuilder;
    }

    private double getSkinBackgroundTintIntensity() {
        return (double) (skinBackgroundTintIntensity.getProgress() + TINT_SEEKBAR_ADJUSTMENT) / 100;
    }

    private SkinManager.Tint getSkinTintOption() {
        return SkinManager.Tint.valueOf((String) skinTintSpinner.getSelectedItem());
    }

    @SuppressLint("ResourceType")
    private @DrawableRes
    int getBackgroundImage() {
        @DrawableRes int bgId = selectedBackgroundId;
        if (bgId < 0) {
            bgId = DEFAULT_SKIN_BACKGROUND_IMAGE;
        }
        return skinBackgroundImage.isChecked() ? bgId : -1;
    }

    private boolean isReverbThemeSelected() {
        return selectedThemeId == R.style.AppLoginTheme_Reverb_A
                || selectedThemeId == R.style.AppLoginTheme_Reverb_B
                || selectedThemeId == R.style.AppLoginTheme_Reverb_C;
    }

    private boolean isSkinSelected() {
        return skin != SkinManager.Skin.NONE;
    }

    private void onLogin(final LoginType loginType) {
        final Intent intent = new Intent(this, AccountKitActivity.class);
        final AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder
                = createAccountKitConfiguration(loginType);
        PhoneNumber phoneNumber = new PhoneNumber("243", "996980422", "CD");
        configurationBuilder.setInitialPhoneNumber(phoneNumber);
        configurationBuilder.setDefaultCountryCode("CD");

        final AccountKitConfiguration configuration = configurationBuilder.build();
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configuration);
        OnCompleteListener completeListener = new OnCompleteListener() {
            @Override
            public void onComplete() {
                startActivityForResult(intent, FRAMEWORK_REQUEST_CODE);
            }
        };
        switch (loginType) {
            case EMAIL:
                if (!isGooglePlayServicesAvailable()) {
                    final OnCompleteListener getAccountsCompleteListener = completeListener;
                    completeListener = new OnCompleteListener() {
                        @Override
                        public void onComplete() {
                            requestPermissions(
                                    Manifest.permission.GET_ACCOUNTS,
                                    R.string.permissions_get_accounts_title,
                                    R.string.permissions_get_accounts_message,
                                    getAccountsCompleteListener);
                        }
                    };
                }
                break;
            case PHONE:
                if (configuration.isReceiveSMSEnabled() && !canReadSmsWithoutPermission()) {
                    final OnCompleteListener receiveSMSCompleteListener = completeListener;
                    completeListener = new OnCompleteListener() {
                        @Override
                        public void onComplete() {
                            requestPermissions(
                                    Manifest.permission.RECEIVE_SMS,
                                    R.string.permissions_receive_sms_title,
                                    R.string.permissions_receive_sms_message,
                                    receiveSMSCompleteListener);
                        }
                    };
                }
                if (configuration.isReadPhoneStateEnabled() && !isGooglePlayServicesAvailable()) {
                    final OnCompleteListener readPhoneStateCompleteListener = completeListener;
                    completeListener = new OnCompleteListener() {
                        @Override
                        public void onComplete() {
                            requestPermissions(
                                    Manifest.permission.READ_PHONE_STATE,
                                    R.string.permissions_read_phone_state_title,
                                    R.string.permissions_read_phone_state_message,
                                    readPhoneStateCompleteListener);
                        }
                    };
                }
                break;
        }
        completeListener.onComplete();
    }

    private boolean isGooglePlayServicesAvailable() {
        final GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int googlePlayServicesAvailable = apiAvailability.isGooglePlayServicesAvailable(this);
        return googlePlayServicesAvailable == ConnectionResult.SUCCESS;
    }

    private boolean canReadSmsWithoutPermission() {
        final GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int googlePlayServicesAvailable = apiAvailability.isGooglePlayServicesAvailable(this);
        if (googlePlayServicesAvailable == ConnectionResult.SUCCESS) {
            return true;
        }
        //TODO we should also check for Android O here t18761104

        return false;
    }

    private void showHelloActivity(final String finalState) {
        final Intent intent = new Intent(this, TokenActivity.class);
        intent.putExtra(
                TokenActivity.HELLO_TOKEN_ACTIVITY_INITIAL_STATE_EXTRA,
                initialStateParam);
        intent.putExtra(TokenActivity.HELLO_TOKEN_ACTIVITY_FINAL_STATE_EXTRA, finalState);
        startActivity(intent);
    }

    private void showHelloActivity(final String code, final String finalState) {
        final Intent intent = new Intent(this, CodeActivity.class);
        intent.putExtra(CodeActivity.HELLO_CODE_ACTIVITY_CODE_EXTRA, code);
        intent.putExtra(
                CodeActivity.HELLO_CODE_ACTIVITY_INITIAL_STATE_EXTRA,
                initialStateParam);
        intent.putExtra(CodeActivity.HELLO_CODE_ACTIVITY_FINAL_STATE_EXTRA, finalState);

        startActivity(intent);
    }

    private void showErrorActivity(final AccountKitError error) {
        final Intent intent = new Intent(this, ErrorActivity.class);
        intent.putExtra(ErrorActivity.HELLO_TOKEN_ACTIVITY_ERROR_EXTRA, error);

        startActivity(intent);
    }

    private void requestPermissions(
            final String permission,
            final int rationaleTitleResourceId,
            final int rationaleMessageResourceId,
            final OnCompleteListener listener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (listener != null) {
                listener.onComplete();
            }
            return;
        }

        checkRequestPermissions(
                permission,
                rationaleTitleResourceId,
                rationaleMessageResourceId,
                listener);
    }

    @TargetApi(23)
    private void checkRequestPermissions(
            final String permission,
            final int rationaleTitleResourceId,
            final int rationaleMessageResourceId,
            final OnCompleteListener listener) {
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            if (listener != null) {
                listener.onComplete();
            }
            return;
        }

        final int requestCode = nextPermissionsRequestCode++;
        permissionsListeners.put(requestCode, listener);

        if (shouldShowRequestPermissionRationale(permission)) {
            new AlertDialog.Builder(this)
                    .setTitle(rationaleTitleResourceId)
                    .setMessage(rationaleMessageResourceId)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            requestPermissions(new String[]{permission}, requestCode);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            // ignore and clean up the listener
                            permissionsListeners.remove(requestCode);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            requestPermissions(new String[]{permission}, requestCode);
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(
            final int requestCode,
            final @NonNull String[] permissions,
            final @NonNull int[] grantResults) {
        final OnCompleteListener permissionsListener = permissionsListeners.get(requestCode);
        permissionsListeners.remove(requestCode);
        if (permissionsListener != null
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            permissionsListener.onComplete();
        }
    }
}
