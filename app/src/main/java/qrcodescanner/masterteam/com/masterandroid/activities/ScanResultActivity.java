package qrcodescanner.masterteam.com.masterandroid.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;

import java.io.File;
import java.util.Locale;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;
import qrcodescanner.masterteam.com.masterandroid.databinding.ActivityScanResultBinding;
import qrcodescanner.masterteam.com.masterandroid.utils.constant.IntentKey;
import qrcodescanner.masterteam.com.masterandroid.utils.constant.PreferenceKey;
import qrcodescanner.masterteam.com.masterandroid.models.Code;
import qrcodescanner.masterteam.com.masterandroid.utils.util.SharedPrefUtil;
import qrcodescanner.masterteam.com.masterandroid.utils.util.TimeUtil;
import qrcodescanner.masterteam.com.masterandroid.utils.util.database.DatabaseUtil;
import qrcodescanner.masterteam.com.masterandroid.R;

public class ScanResultActivity extends AppCompatActivity implements View.OnClickListener {

    private CompositeDisposable mCompositeDisposable;
    private ActivityScanResultBinding mBinding;
    private Menu mToolbarMenu;
    private Code mCurrentCode;
    private boolean mIsHistory, mIsPickedFromGallery;

    public CompositeDisposable getCompositeDisposable() {
        return mCompositeDisposable;
    }

    public void setCompositeDisposable(CompositeDisposable compositeDisposable) {
        mCompositeDisposable = compositeDisposable;
    }

    public Code getCurrentCode() {
        return mCurrentCode;
    }

    public void setCurrentCode(Code currentCode) {
        mCurrentCode = currentCode;
    }

    public Menu getToolbarMenu() {
        return mToolbarMenu;
    }

    public void setToolbarMenu(Menu toolbarMenu) {
        mToolbarMenu = toolbarMenu;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_scan_result);
        setCompositeDisposable(new CompositeDisposable());
        getWindow().setBackgroundDrawable(null);
        initializeToolbar();
        loadQRCode();
        setListeners();
    }

    private void setListeners() {
        mBinding.textViewOpenInBrowser.setOnClickListener(this);
    }

    private void loadQRCode() {
        Intent intent = getIntent();

        if (intent != null) {
            Bundle bundle = intent.getExtras();

            if (bundle != null && bundle.containsKey(IntentKey.MODEL)) {
                setCurrentCode(bundle.getParcelable(IntentKey.MODEL));
            }

            if (bundle != null && bundle.containsKey(IntentKey.IS_HISTORY)) {
                mIsHistory = bundle.getBoolean(IntentKey.IS_HISTORY);
            }

            if (bundle != null && bundle.containsKey(IntentKey.IS_PICKED_FROM_GALLERY)) {
                mIsPickedFromGallery = bundle.getBoolean(IntentKey.IS_PICKED_FROM_GALLERY);
            }
        }

        if (getCurrentCode() != null) {
            mBinding.textViewContent.setText(String.format(Locale.ENGLISH,
                    getString(R.string.content), getCurrentCode().getContent()));

            mBinding.textViewType.setText(String.format(Locale.ENGLISH, getString(R.string.code_type),
                    getResources().getStringArray(R.array.code_types)[getCurrentCode().getType()]));

            mBinding.textViewTime.setText(String.format(Locale.ENGLISH, getString(R.string.created_time),
                    TimeUtil.getFormattedDateString(getCurrentCode().getTimeStamp())));

            mBinding.textViewOpenInBrowser.setEnabled(URLUtil.isValidUrl(getCurrentCode().getContent()));

            if (!TextUtils.isEmpty(getCurrentCode().getCodeImagePath())) {
                Glide.with(this)
                        .asBitmap()
                        .load(getCurrentCode().getCodeImagePath())
                        .into(mBinding.imageViewScannedCode);
            }

            if (SharedPrefUtil.readBooleanDefaultTrue(PreferenceKey.COPY_TO_CLIPBOARD)
                    && !mIsHistory) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText(
                            getString(R.string.scanned_qr_code_content),
                            getCurrentCode().getContent());
                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(this, getString(R.string.copied_to_clipboard),
                            Toast.LENGTH_SHORT).show();
                }
            }

            if (SharedPrefUtil.readBooleanDefaultTrue(PreferenceKey.SAVE_HISTORY) && !mIsHistory) {
                getCompositeDisposable().add(DatabaseUtil.on().insertCode(getCurrentCode())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribeWith(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        }));
            }
        }
    }

    private void initializeToolbar() {
        setSupportActionBar(mBinding.toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_view_open_in_browser:
                if (getCurrentCode() != null
                        && URLUtil.isValidUrl(getCurrentCode().getContent())) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    browserIntent.setData(Uri.parse(getCurrentCode().getContent()));
                    startActivity(browserIntent);
                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getCompositeDisposable().dispose();

        if (getCurrentCode() != null
                && !SharedPrefUtil.readBooleanDefaultTrue(PreferenceKey.SAVE_HISTORY)
                && !mIsHistory && !mIsPickedFromGallery) {
            new File(getCurrentCode().getCodeImagePath()).delete();
        }
    }
}
