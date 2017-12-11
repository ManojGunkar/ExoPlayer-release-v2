package com.globaldelight.boom.business;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyProperties;

import com.globaldelight.boom.BuildConfig;
import com.globaldelight.boom.business.inapp.Base64;

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

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;

/**
 * Created by adarsh on 11/12/17.
 */

public class SecureStorage {

    private final static String ALIAS = String.format("%s-%s", BuildConfig.APPLICATION_ID, "B2B");

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

            String path = mContext.getFilesDir().getPath() + "/" + encryptedName();

            File file = new File(path);
            if ( !file.exists() ) {
                file.createNewFile();
            }

            FileOutputStream fileStream = new FileOutputStream(path);
            CipherOutputStream encryptedStream = new CipherOutputStream(fileStream, input);

            byte[] encodedBytes = Base64.encode(data).getBytes("UTF-8");
            encryptedStream.write(encodedBytes);
            encryptedStream.close();

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

            String path = mContext.getFilesDir().getPath() + "/" + encryptedName();

            FileInputStream fileStream = new FileInputStream(path);
            CipherInputStream decryptStream = new CipherInputStream(fileStream, input);

            byte[] encodedBytes = new byte[512];
            int length = decryptStream.read(encodedBytes);

            byte[] data = Base64.decode(encodedBytes, 0, length);
            decryptStream.close();

            if ( verifySign(data) ) {
                return data;
            }
            else {
                return null;
            }
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
                        // You'll use the alias later to retrieve the key. It's a key
                        // for the key!
                        .setAlias(ALIAS)
                        // The subject used for the self-signed certificate of the
                        // generated pair
                        .setSubject(new X500Principal("CN=" + ALIAS))
                        // The serial number used for the self-signed certificate of the
                        // generated pair.
                        .setSerialNumber(BigInteger.valueOf(2017))
                        // Date range of validity for the generated pair.
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


    private String encryptedName() {
        try {
            byte[] bytes = mName.getBytes("UTF-8");
            for ( int i = 0; i < bytes.length; i++ ) {
                bytes[i] = (byte)(bytes[i] ^ 0xA1);
            }
            return Base64.encode(bytes);
        }
        catch (Exception e) {
            return null;
        }
    }

    private static Cipher getCipher() {
        try {
            return Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
        } catch (Exception exception) {
            throw new RuntimeException("getCipher: Failed to get an instance of Cipher", exception);
        }
    }

    private void sign(byte[] data) {
        try {
            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry)mKeyStore.getEntry(ALIAS, null);
            Signature s = Signature.getInstance("SHA256withRSA");
            s.initSign(entry.getPrivateKey());
            s.update(data);
            byte[] signature = s.sign();

            SharedPreferences pref = mContext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
            pref.edit().putString(String.format("id-%s", mName), Base64.encode(signature)).apply();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean verifySign(byte[] data) {
        try {
            SharedPreferences pref = mContext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
            byte[] signature = Base64.decode(pref.getString(String.format("id-%s", mName), ""));

            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry)mKeyStore.getEntry(ALIAS, null);
            Signature s = Signature.getInstance("SHA256withRSA");
            s.initVerify(entry.getCertificate());
            s.update(data);

            return s.verify(signature);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
