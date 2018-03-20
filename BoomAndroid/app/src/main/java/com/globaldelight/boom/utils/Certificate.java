package com.globaldelight.boom.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyProperties;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

import javax.crypto.Cipher;
import javax.security.auth.x500.X500Principal;

/**
 * Created by adarsh on 22/02/18.
 */

public class Certificate {

    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final String SIGN_ALGORITHM = "SHA256withRSA";

    private String mAlias;
    private KeyStore mKeyStore;
    private Context mContext;

    public Certificate(Context context, String alias) {
        mAlias = alias;
        mContext = context.getApplicationContext();
        init();
    }

    public byte[] sign(byte[] data) throws RuntimeException {
        try {
            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry)mKeyStore.getEntry(mAlias, null);
            Signature s = Signature.getInstance(SIGN_ALGORITHM);
            s.initSign(entry.getPrivateKey());
            s.update(data);
            return  s.sign();
        }
        catch (Exception e) {
            throw new RuntimeException("Sign failed", e);
        }
    }

    public boolean verify(byte[] signature, byte[] data) throws RuntimeException {
        try {
            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry)mKeyStore.getEntry(mAlias, null);
            Signature s = Signature.getInstance(SIGN_ALGORITHM);
            s.initVerify(entry.getCertificate());
            s.update(data);

            return s.verify(signature);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public byte[] encrypt(byte[] data) throws RuntimeException {
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry =
                    (KeyStore.PrivateKeyEntry) mKeyStore.getEntry(mAlias, null);
            PublicKey publicKey = privateKeyEntry.getCertificate().getPublicKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(data);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to encrypt!", e);
        }

    }

    public byte[] decrypt(byte[] data) throws RuntimeException {
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry =
                    (KeyStore.PrivateKeyEntry) mKeyStore.getEntry(mAlias, null);
            PrivateKey privateKey = privateKeyEntry.getPrivateKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(data);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to decrypt!", e);
        }
    }

    private void init() {
        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
            mKeyStore.load(null);
            if ( !mKeyStore.containsAlias(mAlias) ) {
                Calendar start = new GregorianCalendar();
                Calendar end = new GregorianCalendar();
                end.add(Calendar.YEAR, 10);

                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(mContext)
                        .setAlias(mAlias)
                        .setSubject(new X500Principal("CN=" + mAlias))
                        .setSerialNumber(new BigInteger(64, new Random(System.currentTimeMillis())))
                        .setStartDate(start.getTime()).setEndDate(end.getTime())
                        .build();

                KeyPairGenerator kpg = KeyPairGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_RSA, mKeyStore.getProvider());
                kpg.initialize(spec);
                KeyPair kp = kpg.generateKeyPair();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
