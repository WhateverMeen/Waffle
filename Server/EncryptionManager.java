import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;


public class EncryptionManager{


    //Class used to handle encryption related functions such as generating keys, encrypting and decrypting messages
    public static KeyPair get_keys() throws Exception{
        //Function to generate a RSA key pair used for encryption
        SecureRandom r = new SecureRandom();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048, r);
        return keyPairGenerator.generateKeyPair();
    }

    public static PublicKey public_key_from_string(String key) throws Exception{
        //Converts a string received from a socket into a public key
        byte[] bytes = Base64.getDecoder().decode(key.getBytes());
        X509EncodedKeySpec publicKey = new X509EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(publicKey);
    }

    public static String encrypt_message(String message, PublicKey key) throws Exception{
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(message.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt_message(String message, PrivateKey key) throws Exception{
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decoded = Base64.getDecoder().decode(message);
        byte[] result = cipher.doFinal(decoded);
        return new String(result, "UTF-8");
    }
}