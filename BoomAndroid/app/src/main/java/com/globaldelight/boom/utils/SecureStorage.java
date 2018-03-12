package com.globaldelight.boom.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import com.globaldelight.boom.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by adarsh on 11/12/17.
 */

public class SecureStorage {

    private final static String ALIAS = String.format("ss-%s-%s-0", BuildConfig.APPLICATION_ID, BuildConfig.FLAVOR);
    private final static String ALIAS_ENCRYPTION_KEY = String.format("ss-%s-%s-1", BuildConfig.APPLICATION_ID, BuildConfig.FLAVOR);
    private final static String TRANSFORMATION = KeyProperties.KEY_ALGORITHM_AES;

    private Context mContext;
    private String  mName;
    private Certificate mCertificate;

    public SecureStorage(String name, Context context) {
        mContext = context;
        mName = name;
        mCertificate = new Certificate(mContext, ALIAS);
        initKey();
    }

    public static boolean exists(Context context, String name) {
        File file = new File(getFilePath(context, name));
        return file.exists();
    }


    public void store(byte[] data) {
        try {
            String path = getFilePath(mContext, mName);
            File file = new File(path);
            if ( !file.exists() ) {
                file.createNewFile();
            }

            FileOutputStream fileStream = new FileOutputStream(path);
            CipherOutputStream encryptStream = new CipherOutputStream(fileStream, getCipher(Cipher.ENCRYPT_MODE));

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
            FileInputStream fileStream = new FileInputStream(getFilePath(mContext, mName));
            CipherInputStream decryptStream = new CipherInputStream(fileStream, getCipher(Cipher.DECRYPT_MODE));

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            byte[] encodedBytes = new byte[4096];
            while ( true ) {
                int length = decryptStream.read(encodedBytes);
                if ( length == -1) break;

                byteStream.write(encodedBytes, 0, length);
            }
            decryptStream.close();

            byte[] data = Base64.decode(byteStream.toByteArray(), Base64.DEFAULT);
            return verifySign(data) ? data : null;
        }
        catch (Exception e) {
            return null;
        }
    }


    private static String encryptedName(String name) {
        try {
            byte[] bytes = name.getBytes("UTF-8");
            for ( int i = 0; i < bytes.length; i++ ) {
                bytes[i] = (byte)(bytes[i] ^ 0xA1);
            }
            return toBase64(bytes);
        }
        catch (Exception e) {
            return null;
        }
    }


    private void sign(byte[] data) {
        try {
            byte[] signature = mCertificate.sign(data);
            SharedPreferences pref = mContext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
            pref.edit().putString(signatureKey(), toBase64(signature)).apply();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean verifySign(byte[] data) {
        try {
            SharedPreferences pref = mContext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
            byte[] signature =  dataFromBase64(pref.getString(signatureKey(), ""));
            return mCertificate.verify(signature, data);
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
        return String.format("ss-id-%s", mName);
    }


    private void initKey() {
        SharedPreferences pref = mContext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        String key = pref.getString(ALIAS_ENCRYPTION_KEY, null);
        if ( key == null ) {
            try {
                KeyGenerator kg = KeyGenerator.getInstance(TRANSFORMATION);
                kg.init(128);
                SecretKey newKey = kg.generateKey();
                byte[] encryptedKey = mCertificate.encrypt(newKey.getEncoded());
                pref.edit().putString(ALIAS_ENCRYPTION_KEY, toBase64(encryptedKey)).apply();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private SecretKeySpec getEncryptionKey() {
        try {
            SharedPreferences pref = mContext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
            String encodedKey = pref.getString(ALIAS_ENCRYPTION_KEY, null);
            if ( encodedKey != null ) {
                byte[] key = mCertificate.decrypt(dataFromBase64(encodedKey));
                SecretKeySpec sp = new SecretKeySpec(key, TRANSFORMATION);
                return sp;
            }
        }
        catch (Exception e) {

        }

        return null;
    }


    private Cipher getCipher(int opmode) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(opmode, getEncryptionKey());
            return cipher;
        } catch (Exception exception) {
            throw new RuntimeException("Failed to get cipher", exception);
        }
    }

    private static byte[] toBase64Data(String string) {
        return toBase64(string).getBytes();
    }

    private static String toBase64(String string) {
        return toBase64(string.getBytes());
    }

    private static String toBase64(byte[] data) {
        try {
            return new String(Base64.encode(data, Base64.DEFAULT), "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private static byte[] dataFromBase64(String string) {
        return fromBase64(string).getBytes();
    }

    private static String fromBase64(String string) {
        return fromBase64(string.getBytes());
    }

    private static String fromBase64(byte[] data) {
        try {
            return new String(Base64.decode(data, Base64.DEFAULT), "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            return null;
        }
    }

}
