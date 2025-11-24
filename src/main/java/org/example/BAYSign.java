package org.example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

public class BAYSign {
    private static final String CIPHER_PROVIDER = "SunJCE";
    private static final String TRANSFORMATION_PKCS1Paddiing = "RSA/ECB/PKCS1Padding";

    public String createSignature(JSONObject json, String publicKeyString){
        System.out.println("DEBUG: publicKeyString = " + publicKeyString);
        Map mapq = json.toMap();
        PublicKey publicKey = getPublicKey(publicKeyString);
        String sign = getSign(mapq, publicKey);

        return sign;
    }

    public static String rankParameters(Map<String, String> map) {
        ArrayList<String> list = new ArrayList<>();
        try {
            for (Map.Entry<String, String> entry : map.entrySet()) {

                if (StringUtils.isNotBlank(entry.getValue())) {
                    list.add(entry.getKey() + "=" + entry.getValue() + "&");
                }
            }
            int size = list.size();
            String[] arrayToSort = list.toArray(new String[size]);
            // sorted
            Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < size; i++) {
                sb.append(arrayToSort[i]);
            }
            String result = sb.toString();
            int length = result.length();
            result = result.substring(0, length - 1);
            System.out.println(result);
            // Use SHA256 encryption
            result = DigestUtils.sha256Hex(result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String decryptSign(String sign, Key key) {
        try {
            String result = decrypt(key, sign);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getSign(Map<String, String> map, Key key) {
        ArrayList<String> list = new ArrayList<>();
        String result = "";
        try {
            result = rankParameters(map);
            result = encrypt(key, result.getBytes());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static PublicKey getPublicKey(String publicKeyString) {
        PublicKey publicKey = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyString));
            publicKey = keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return publicKey;
    }

    public static String cleanKeyString(String key) {
        return key.replaceAll("\\s+", "");
    }

    public static PrivateKey getPrivateKey(String privateKeyString) {
        PrivateKey privateKey = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString));
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return privateKey;
    }

    public static String encrypt(Key key, byte[] plainTextData) {
        Cipher cipher = null;
        String result = "";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            cipher = Cipher.getInstance(TRANSFORMATION_PKCS1Paddiing, CIPHER_PROVIDER);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            int inputLen = plainTextData.length;

            int offSet = 0;
            byte[] encry = null;
            for (int i = 0; inputLen - offSet > 0; offSet = i * 117) {
                byte[] cache;
                if (inputLen - offSet > 117) {
                    cache = cipher.doFinal(plainTextData, offSet, 117);
                } else {
                    cache = cipher.doFinal(plainTextData, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                ++i;
            }
            // return result;
            byte[] encryptedData = out.toByteArray();
            out.close();
            result = new String(Base64.getEncoder().encode(encryptedData));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String decrypt(Key key, String cipherData) {
        Cipher cipher = null;
        String result = "";
        try {
            cipher = Cipher.getInstance(TRANSFORMATION_PKCS1Paddiing, CIPHER_PROVIDER);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] encryptedData = Base64.getDecoder().decode(cipherData.getBytes());
            int inputLen = encryptedData.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;

            for (int i = 0; inputLen - offSet > 0; offSet = i * 128) {
                byte[] cache;
                if (inputLen - offSet > 128) {
                    cache = cipher.doFinal(encryptedData, offSet, 128);
                } else {
                    cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                ++i;
            }
            byte[] decryptedData = out.toByteArray();
            out.close();
            result = new String(decryptedData);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
