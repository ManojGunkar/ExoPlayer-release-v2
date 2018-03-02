package com.globaldelight.boom.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import com.globaldelight.boom.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;

/**
 * Created by adarsh on 11/12/17.
 */

public class SecureStorage {

    private final static String ALIAS = String.format("%s-%s", BuildConfig.APPLICATION_ID, "B2B");
    private final static String TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private final static String SIGN_ALGORITHM = "SHA256withRSA";

    private Context mContext;
    private String  mName;
    private KeyStore mKeyStore;

    public SecureStorage(String name, Context context) {
        mContext = context;
        mName = name;
        initKeys();
    }


    public void store(byte[] data) {
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry =
                    (KeyStore.PrivateKeyEntry) mKeyStore.getEntry(ALIAS, null);
            PublicKey publicKey = privateKeyEntry.getCertificate().getPublicKey();

            Cipher input = getCipher();
            input.init(Cipher.ENCRYPT_MODE, publicKey);

            String path = getFilePath(mContext, mName);
            File file = new File(path);
            if ( !file.exists() ) {
                file.createNewFile();
            }

            FileOutputStream fileStream = new FileOutputStream(path);
            CipherOutputStream encryptStream = new CipherOutputStream(fileStream, input);

            byte[] encodedBytes = Base64.encode(data, Base64.DEFAULT);
            encryptStream.write(encodedBytes);
            encryptStream.close();

            sign(data);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] load() {
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry =
                    (KeyStore.PrivateKeyEntry) mKeyStore.getEntry(ALIAS, null);
            PrivateKey privateKey = privateKeyEntry.getPrivateKey();

            Cipher input = getCipher();
            input.init(Cipher.DECRYPT_MODE, privateKey);


            FileInputStream fileStream = new FileInputStream(getFilePath(mContext, mName));
            CipherInputStream decryptStream = new CipherInputStream(fileStream, input);

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            byte[] encodedBytes = new byte[4096];
            while ( true ) {
                int length = decryptStream.read(encodedBytes);
                if ( length == -1) break;

                byteStream.write(encodedBytes, 0, length);
            }

            byte[] data = Base64.decode(byteStream.toByteArray(), Base64.DEFAULT);
            decryptStream.close();

            return verifySign(data) ? data : null;
        }
        catch (Exception e) {
            return null;
        }
    }

    private void initKeys() {
        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
            mKeyStore.load(null);
            if ( !mKeyStore.containsAlias(ALIAS) ) {
                Calendar start = new GregorianCalendar();
                Calendar end = new GregorianCalendar();
                end.add(Calendar.YEAR, 10);

                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(mContext)
                        .setAlias(ALIAS)
                        .setSubject(new X500Principal("CN=" + ALIAS))
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

    private static String encryptedName(String name) {
        try {
            byte[] bytes = name.getBytes("UTF-8");
            for ( int i = 0; i < bytes.length; i++ ) {
                bytes[i] = (byte)(bytes[i] ^ 0xA1);
            }
            return new String(Base64.encode(bytes, Base64.DEFAULT), "UTF-8");
        }
        catch (Exception e) {
            return null;
        }
    }

    private static Cipher getCipher() {
        try {
            return Cipher.getInstance(TRANSFORMATION);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to get cipher", exception);
        }
    }

    private void sign(byte[] data) {
        try {
            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry)mKeyStore.getEntry(ALIAS, null);
            Signature s = Signature.getInstance(SIGN_ALGORITHM);
            s.initSign(entry.getPrivateKey());
            s.update(data);
            byte[] signature = s.sign();

            SharedPreferences pref = mContext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
            String signatureStr = new String(Base64.encode(signature, Base64.DEFAULT), "UTF-8");
            pref.edit().putString(signatureKey(), signatureStr).apply();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean verifySign(byte[] data) {
        try {
            SharedPreferences pref = mContext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
            byte[] signature = Base64.decode(pref.getString(signatureKey(), ""), Base64.DEFAULT);

            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry)mKeyStore.getEntry(ALIAS, null);
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

    private static String getFilePath(Context context, String name) {
        return context.getFilesDir().getPath() + "/" + encryptedName(name);
    }

    private String signatureKey() {
        return String.format("id-%s", mName);
    }

    public static boolean exists(Context context, String name) {
        File file = new File(getFilePath(context, name));
        return file.exists();
    }
}
