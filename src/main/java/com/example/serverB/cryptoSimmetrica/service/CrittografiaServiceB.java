package com.example.serverB.cryptoSimmetrica.service;

import com.example.serverB.cryptoSimmetrica.dto.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

@Service
public class CrittografiaServiceB {

    private final String pw="miaPasswordSuperSegreta123!";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private byte[] salt;
    private SecretKeySpec key;

    @Autowired
    private RestTemplate restTemplate;


    public void ricezioneMessaggio(BaseResponse response) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        salt=response.getSalt();
        generazioneChiave();
        String messaggio= decryptMessage(response.getMssage(), response.getIV());
        System.out.println(messaggio);


    }

    private  String decryptMessage(String message,byte[]iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        generazioneChiave();
        byte[] decode= Base64.getDecoder().decode(message.getBytes());
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // 2. Inizializza Cipher in modalità ENCRYPT
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec); // key è la tua SecretKeySpec

        // 3. Cifra il messaggio
        byte[] decripted = cipher.doFinal(decode);



        // 5. Restituisci risposta con testo cifrato e IV
        return new String(decripted);
    }

    private void generazioneChiave() throws NoSuchAlgorithmException, InvalidKeySpecException {
        //imposta le condizioni di derivazione della chiave in quello che sarà una specifica usata dall'oggetto factory che genera la chiave
        PBEKeySpec spec=new PBEKeySpec(pw.toCharArray(),salt,ITERATIONS,KEY_LENGTH);
        //Crea l'oggetto che userà per generare la chiave indicando come parametro in formato stringa quello che l'algoritmo('HmacSHA256') da usare preceduto
        // dal tipo di specifica che si vuole usare ('PBKDF')
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = skf.generateSecret(spec).getEncoded();
        //ritorna una chiave simmetrica AES a partire dai i byte derivati
        key= new SecretKeySpec(keyBytes, "AES");
    }

}
