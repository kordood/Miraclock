package com.example.fingerprintalarm;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


public class FingerprintActivity extends Activity implements FingerprintHelper.Callback {
    public static final String DEFAULT_KEY_NAME = "default_key";

    FingerprintManagerCompat managerCompat;
    FingerprintManager mFingerprintManager;

    private FingerprintManager.CryptoObject mCryptoObject;
    private FingerprintHelper mFingerprintHelper;

    KeyStore mKeyStore = null;
    KeyGenerator mKeyGenerator = null;
    KeyguardManager mKeyguardManager;

    private Context mContext;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = FingerprintActivity.this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            mKeyguardManager.requestDismissKeyguard(this, null);
        } else {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        setContentView(R.layout.activity_fingerprint);

        managerCompat = FingerprintManagerCompat.from(this);

        if (managerCompat.isHardwareDetected() && managerCompat.hasEnrolledFingerprints()) {
            fingerprintStart();
        } else {
            Toast.makeText(getApplicationContext(), "Fingerprint not supported", Toast.LENGTH_SHORT).show();
        }
    }

    protected void fingerprintStart() {
        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        try {
            mKeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }

        Cipher defaultCipher;
        try {
            defaultCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get an instance of Cipher", e);
        }

        mKeyguardManager = mContext.getSystemService(KeyguardManager.class);
        mFingerprintManager = mContext.getSystemService(FingerprintManager.class);

        mFingerprintHelper = new FingerprintHelper(mFingerprintManager, mContext, this);

        if (!mKeyguardManager.isKeyguardSecure()) {
            Toast.makeText(mContext,
                    "Lock screen not set up.\n"
                            + "Go to 'Settings -> Security -> Fingerprint' to set up a fingerprint",
                    Toast.LENGTH_LONG).show();
            return;
        }

        createKey(DEFAULT_KEY_NAME);

        if (initCipher(defaultCipher, DEFAULT_KEY_NAME)) {
            mCryptoObject = new FingerprintManager.CryptoObject(defaultCipher);
        }
    }

    private boolean initCipher(Cipher cipher, String keyName) {
        try {
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(keyName, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);


            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            Toast.makeText(mContext, "Keys are invalidated after created. Retry the purchase\n"
                            + e.getMessage(),
                    Toast.LENGTH_LONG).show();

            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            Toast.makeText(mContext, "Failed to init cipher", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mCryptoObject != null) {
            mFingerprintHelper.startAuthentication(mFingerprintManager, mCryptoObject);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mFingerprintHelper.stopListening();
    }

    public void setContext(Context context) {
        mContext = context;
    }


    public void createKey(String keyName) {
        try {
            mKeyStore.load(null);


            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyName,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

            mKeyGenerator.init(builder.build());
            mKeyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onAuthenticated(boolean b) {
        if (b) {
            Toast.makeText(mContext.getApplicationContext(), "Auth success", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(mContext, MainActivity.class);
            mContext.startActivity(intent);
            ((Activity)mContext).finish();
        } else
            Toast.makeText(mContext.getApplicationContext(), "Auth failed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(String s) {
        Toast.makeText(mContext.getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onHelp(String s) {
        Toast.makeText(mContext.getApplicationContext(), "Auth help message:" + s, Toast.LENGTH_LONG).show();
    }

}
