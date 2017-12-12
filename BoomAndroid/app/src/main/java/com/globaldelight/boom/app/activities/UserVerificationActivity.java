package com.globaldelight.boom.app.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.BuildConfig;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.sharedPreferences.UserPreferenceHandler;
import com.globaldelight.boom.business.LicenseManager;
import com.globaldelight.boom.utils.Utils;

/**
 * Created by adarsh on 08/12/17.
 */

public class UserVerificationActivity extends AppCompatActivity {

    private Button mSubmitButton;
    private EditText mPromoCodeField;
    private TextView mErrorTextView;
    private View mProgressView;

    private View.OnClickListener mSubmitClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String promoCode = mPromoCodeField.getText().toString();
            verifyPromoCode(promoCode);
        }
    };

    private TextWatcher mTextChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            boolean hasText = charSequence.length() > 0;
            if ( hasText ) {
                mErrorTextView.setVisibility(View.GONE);
            }

            mSubmitButton.setEnabled(hasText);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_verification);

        mSubmitButton = findViewById(R.id.promocode_submit_button);
        mSubmitButton.setOnClickListener(mSubmitClickListener);
        mSubmitButton.setEnabled(false);

        mPromoCodeField = findViewById(R.id.promocode_text_field);
        mPromoCodeField.addTextChangedListener(mTextChangeListener);
        mPromoCodeField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    String promoCode = mPromoCodeField.getText().toString();
                    verifyPromoCode(promoCode);
                }
                return false;
            }
        });

        mErrorTextView = findViewById(R.id.promocode_error_view);
        mErrorTextView.setVisibility(View.GONE);

        mProgressView = findViewById(R.id.promocode_progress_view);
        mProgressView.setVisibility(View.GONE);

    }

    @Override
    public void onBackPressed() {
//        Utils.createDialogBuilder(this)
//                .content(R.string.promocode_exit_dialog_message)
//                .positiveText(R.string.dialog_exit_button)
//                .negativeText(R.string.dialog_txt_cancel)
//                .onPositive(new MaterialDialog.SingleButtonCallback() {
//                    @Override
//                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        moveTaskToBack(true);
//                    }
//                })
//                .show();
        moveTaskToBack(true);
    }

    private void verifyPromoCode(String promoCode) {

        mSubmitButton.setEnabled(false);
        mPromoCodeField.setEnabled(false);
        mProgressView.setVisibility(View.VISIBLE);

        LicenseManager.getInstance(this).verifyCode(promoCode, new LicenseManager.Callback() {
            @Override
            public void onSuccess() {
                UserVerificationActivity.this.onSuccess();
            }

            @Override
            public void onError(int errorCode) {
                UserVerificationActivity.this.onError();
            }
        });
    }

    private void onError() {
        mProgressView.setVisibility(View.GONE);
        mErrorTextView.setVisibility(View.VISIBLE);
        mPromoCodeField.setText("");
        mSubmitButton.setEnabled(false);
        mPromoCodeField.setEnabled(true);
    }

    private void onSuccess() {
        finish();
    }
}
