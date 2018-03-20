package com.globaldelight.boom.app.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.business.ErrorCode;
import com.globaldelight.boom.business.LicenseManager;

/**
 * Created by adarsh on 08/12/17.
 */

public class UserVerificationActivity extends AppCompatActivity {

    private View mPromocodeView;
    private View mVerifyScreen;
    private Button mSubmitButton;
    private EditText mPromoCodeField;
    private TextView mTitleTextView;
    private View mProgressView;
    private View mCongratsView;

    private View.OnClickListener mSubmitClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String promoCode = mPromoCodeField.getText().toString();
            verifyPromoCode(promoCode);
        }
    };

    private View.OnClickListener mContinueClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
        }
    };

    private TextWatcher mTextChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            boolean hasText = charSequence.length() > 0;
            mSubmitButton.setEnabled(hasText);
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_verification);

        mVerifyScreen = findViewById(R.id.verify_screen);
        mTitleTextView = findViewById(R.id.promocode_page_title);
        mPromocodeView = findViewById(R.id.promocode_view);

        mPromoCodeField = findViewById(R.id.promocode_text_field);
        mPromoCodeField.addTextChangedListener(mTextChangeListener);
        mPromoCodeField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    String promoCode = mPromoCodeField.getText().toString();
                    if ( promoCode.length() > 0 ) {
                        verifyPromoCode(promoCode);
                    }
                }
                return false;
            }
        });

        mSubmitButton = findViewById(R.id.promocode_verify_button);
        mSubmitButton.setOnClickListener(mSubmitClickListener);
        mSubmitButton.setEnabled(false);


        mProgressView = findViewById(R.id.promocode_progress_view);
        mProgressView.setVisibility(View.GONE);
        Button continueButton = findViewById(R.id.continue_button);
        continueButton.setOnClickListener(mContinueClickListener);

        mCongratsView = findViewById(R.id.congrats_screen);
        mCongratsView.setVisibility(View.GONE);

    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAnalytics.getInstance(this).startSession(this);
        FlurryAnalytics.getInstance(this).pageView();
    }

    @Override
    protected void onStop() {
        FlurryAnalytics.getInstance(this).endSession(this);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void verifyPromoCode(String promoCode) {
        FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.EVENT_PROMOCODE_VERIFY_CLICKED);
        mTitleTextView.setTextColor(getResources().getColor(R.color.white));
        mTitleTextView.setText(R.string.promocode_progress_text);
        mPromocodeView.setVisibility(View.GONE);
        mPromoCodeField.setEnabled(false);
        mProgressView.setVisibility(View.VISIBLE);

        LicenseManager.getInstance(this).verifyCode(promoCode, new LicenseManager.Callback() {
            @Override
            public void onSuccess() {
                UserVerificationActivity.this.onSuccess();
            }

            @Override
            public void onError(@ErrorCode int errorCode) {
                UserVerificationActivity.this.onError(errorCode);
            }
        });
    }

    private void onError(@ErrorCode int errorCode) {
        FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.EVENT_PROMOCODE_VERIFY_FAILED);

        mProgressView.setVisibility(View.GONE);
        mPromocodeView.setVisibility(View.VISIBLE);
        mSubmitButton.setEnabled(mPromoCodeField.getText().length() > 0);
        mPromoCodeField.setEnabled(true);
        mTitleTextView.setTextColor(getResources().getColor(R.color.promo_code_error));

        switch (errorCode) {
            case ErrorCode.NETWORK_ERROR:
                mTitleTextView.setText(R.string.promocode_network_error);
                break;

            case ErrorCode.FAILED:
            case ErrorCode.INVALID_CODE:
                mTitleTextView.setText(R.string.promocode_invalid_error);
                break;

            case ErrorCode.LIMIT_EXCEEDED:
                mTitleTextView.setText(R.string.promocode_limit_exceeded);
                break;
        }
    }

    private void onSuccess() {
        FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.EVENT_PROMOCODE_VERIFY_SUCCESS);
        mVerifyScreen.setVisibility(View.GONE);
        mCongratsView.setVisibility(View.VISIBLE);
    }
}
