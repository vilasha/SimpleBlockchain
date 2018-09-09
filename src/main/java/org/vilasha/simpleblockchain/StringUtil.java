package org.vilasha.simpleblockchain;

import java.security.*;
import java.util.Base64;
import com.google.gson.GsonBuilder;
import java.util.Vector;

public class StringUtil {
    public static String applySha256(String input){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(input.getBytes("UTF-8"));

            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);

                if (hex.length() == 1) {
                    hexString.append('0');
                }

                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
        Signature dsa;

        byte[] output = new byte[0];

        try {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);

            byte[] strByte = input.getBytes();

            dsa.update(strByte);

            byte[] realSig = dsa.sign();

            output = realSig;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return output;
    }

    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");

            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());

            return ecdsaVerify.verify(signature);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public final static String getJson(Object o) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(o, o.getClass());
    }

    public static String getDificultyString(int difficulty) {
        return new String(new char[difficulty]).replace('\0', '0');
    }

    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static String getMerkleRoot(Vector<Transaction> transactions) {
        int count = transactions.size();

        Vector<String> previousTreeLayer = new Vector<String>();

        for(Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.getTransactionId());
        }

        Vector<String> treeLayer = previousTreeLayer;

        while (count > 1) {
            treeLayer = new Vector<String>();

            for (int i=1; i < previousTreeLayer.size(); i+=2) {
                treeLayer.add(applySha256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
            }

            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }

        String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";

        return merkleRoot;
    }
}