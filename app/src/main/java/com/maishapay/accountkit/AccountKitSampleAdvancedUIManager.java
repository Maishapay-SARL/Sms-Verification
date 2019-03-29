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

import android.app.Fragment;
import android.os.Parcel;
import android.support.annotation.Nullable;

import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.ui.BaseUIManager;
import com.facebook.accountkit.ui.ButtonType;
import com.facebook.accountkit.ui.LoginFlowState;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.accountkit.ui.TextPosition;

public class AccountKitSampleAdvancedUIManager extends BaseUIManager {
    private static final int BODY_HEIGHT = 80;
    private static final int FOOTER_HEIGHT = 120;
    private static final int HEADER_HEIGHT = 80;

    private final ButtonType confirmButton;
    private final ButtonType entryButton;
    private AccountKitError error;
    private LoginType loginType;
    private final TextPosition textPosition;

    @Deprecated
    public AccountKitSampleAdvancedUIManager(
            final ButtonType confirmButton,
            final ButtonType entryButton,
            final TextPosition textPosition,
            final LoginType loginType) {
        super(-1);
        this.confirmButton = confirmButton;
        this.entryButton = entryButton;
        this.textPosition = textPosition;
        this.loginType = loginType;
    }

    private AccountKitSampleAdvancedUIManager(final Parcel source) {
        super(source);
        this.loginType = LoginType.values()[source.readInt()];
        String s = source.readString();
        final ButtonType confirmButton = s == null ? null : ButtonType.valueOf(s);
        s = source.readString();
        final ButtonType entryButton = s == null ? null : ButtonType.valueOf(s);
        s = source.readString();
        final TextPosition textPosition = s == null ? null : TextPosition.valueOf(s);
        this.confirmButton = confirmButton;
        this.entryButton = entryButton;
        this.textPosition = textPosition;
    }

    @Override
    @Nullable
    public Fragment getBodyFragment(final LoginFlowState state) {
        return getPlaceholderFragment(state, BODY_HEIGHT, "Body");
    }

    @Override
    @Nullable
    public ButtonType getButtonType(final LoginFlowState state) {
        switch (state) {
            case PHONE_NUMBER_INPUT:
            case EMAIL_INPUT:
                return entryButton;
            case CODE_INPUT:
            case CONFIRM_ACCOUNT_VERIFIED:
                return confirmButton;
            default:
                return null;
        }
    }

    @Override
    @Nullable
    public Fragment getFooterFragment(final LoginFlowState state) {
        return getPlaceholderFragment(state, FOOTER_HEIGHT, "Footer");
    }

    @Override
    @Nullable
    public Fragment getHeaderFragment(final LoginFlowState state) {
        if (state != LoginFlowState.ERROR) {
            return getPlaceholderFragment(state, HEADER_HEIGHT, "Header");
        }
        final String errorMessage = getErrorMessage();
        if (errorMessage == null) {
            return PlaceholderFragment.create(HEADER_HEIGHT, R.string.error_message);
        } else {
            return PlaceholderFragment.create(HEADER_HEIGHT, errorMessage);
        }
    }

    @Override
    @Nullable
    public TextPosition getTextPosition(final LoginFlowState state) {
        return textPosition;
    }

    @Override
    public void onError(final AccountKitError error) {
        this.error = error;
    }

    private String getErrorMessage() {
        if (error == null) {
            return null;
        }

        final String message = error.getUserFacingMessage();
        if (message == null) {
            return null;
        }

        return message;
    }

    @Nullable
    private PlaceholderFragment getPlaceholderFragment(
            final LoginFlowState state,
            final int height,
            final String suffix) {
        final String prefix;
        switch (state) {
            case PHONE_NUMBER_INPUT:
                prefix = "Custom Phone Number ";
                break;
            case EMAIL_INPUT:
                prefix = "Custom Email ";
                break;
            case ACCOUNT_VERIFIED:
                prefix = "Custom Account Verified ";
                break;
            case CONFIRM_ACCOUNT_VERIFIED:
                prefix = "Custom Confirm Account Verified ";
                break;
            case CONFIRM_INSTANT_VERIFICATION_LOGIN:
                prefix = "Custom Confirm Instant Verification Login ";
                break;
            case EMAIL_VERIFY:
                prefix = "Custom Email Verify ";
                break;
            case SENDING_CODE:
                switch (loginType) {
                    case EMAIL:
                        prefix = "Custom Sending Email ";
                        break;
                    case PHONE:
                        prefix = "Custom Sending Code ";
                        break;
                    default:
                        return null;
                }
                break;
            case SENT_CODE:
                switch (loginType) {
                    case EMAIL:
                        prefix = "Custom Sent Email ";
                        break;
                    case PHONE:
                        prefix = "Custom Sent Code ";
                        break;
                    default:
                        return null;
                }
                break;
            case CODE_INPUT:
                prefix = "Custom Code Input ";
                break;
            case VERIFYING_CODE:
                prefix = "Custom Verifying ";
                break;
            case VERIFIED:
                prefix = "Custom Verified ";
                break;
            case RESEND:
                prefix = "Custom Resend ";
                break;
            case ERROR:
                prefix = "Custom Error ";
                break;
            default:
                return null;
        }
        return PlaceholderFragment.create(height, prefix.concat(suffix));
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(loginType.ordinal());
        dest.writeString(confirmButton != null ? confirmButton.name() : null);
        dest.writeString(entryButton != null ? entryButton.name() : null);
        dest.writeString(textPosition != null ? textPosition.name() : null);
    }

    public static final Creator<AccountKitSampleAdvancedUIManager> CREATOR
            = new Creator<AccountKitSampleAdvancedUIManager>() {
        @Override
        public AccountKitSampleAdvancedUIManager createFromParcel(final Parcel source) {
            return new AccountKitSampleAdvancedUIManager(source);
        }

        @Override
        public AccountKitSampleAdvancedUIManager[] newArray(final int size) {
            return new AccountKitSampleAdvancedUIManager[size];
        }
    };
}
