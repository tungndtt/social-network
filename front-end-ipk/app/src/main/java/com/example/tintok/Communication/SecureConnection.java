package com.example.tintok.Communication;

import android.os.Build;
import android.security.keystore.KeyProperties;
import android.util.Log;

import androidx.annotation.RequiresApi;



import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

import java.security.spec.X509EncodedKeySpec;

import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class SecureConnection {
    public static SecureConnection instance;
    private KeyPair mRSAkey;
    public PublicKey serverpubKey;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static SecureConnection getInstance(){
        if(instance == null)
            instance = new SecureConnection();
        return instance;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setServerpubKey(String pubkey){
        pubkey = pubkey.replaceAll("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "").trim();
        byte[] binCpk = Base64.getDecoder().decode(pubkey);
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(binCpk);
            this.serverpubKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private SecureConnection(){
        try {
           KeyPairGenerator mGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA);
           mGenerator.initialize(256);
           this.serverpubKey=null;
           mRSAkey = mGenerator.generateKeyPair();
           Log.e("prikey", Base64.getEncoder().encodeToString(mRSAkey.getPrivate().getEncoded()));
            Log.e("pubkey", Base64.getEncoder().encodeToString(mRSAkey.getPublic().getEncoded()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String EncodeDataToSend(String data){
        try {
            Cipher cipher =Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, serverpubKey);
            byte[] mData = data.getBytes(StandardCharsets.UTF_8);
            byte[] encStr = cipher.doFinal(mData);

            return Base64.getEncoder().encodeToString(encStr);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String DecodeReceivedData(String data){
        if(serverpubKey == null)
            return data;
        try {
            Cipher cipher =Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, mRSAkey.getPrivate());
            byte[] encStr = cipher.doFinal(Base64.getDecoder().decode(data));
            return  new String(encStr, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return "";
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getPubKeyClient(){
        return new String(Base64.getEncoder().encode(mRSAkey.getPublic().getEncoded()));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getPubKeyServer(){
       return new String(Base64.getEncoder().encode(serverpubKey.getEncoded()));

    }
}
