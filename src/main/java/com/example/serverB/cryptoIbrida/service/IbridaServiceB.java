package com.example.serverB.cryptoIbrida.service;

import com.example.serverB.cryptoIbrida.dto.IbridResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Service
public class IbridaServiceB {


    public String ricevimento(IbridResponse ibridResponse) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String keyPrivata=leggiFileDaResources("chiave_privataB.txt");
        byte[]chiavePrivataB= Base64.getDecoder().decode(keyPrivata);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(chiavePrivataB);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey pKey = keyFactory.generatePrivate(keySpec);
        Cipher cipher=Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE,pKey);
        byte[]chiaveSimmetrica=Base64.getDecoder().decode(ibridResponse.getKey());
        byte[]resultAsimm=cipher.doFinal(chiaveSimmetrica);
        SecretKey aesKey = new SecretKeySpec(resultAsimm, "AES");
        Cipher cipherAes= Cipher.getInstance("AES");
        cipherAes.init(Cipher.DECRYPT_MODE,aesKey);
        byte[] testo = Base64.getDecoder().decode(ibridResponse.getMessage());
        byte[]result=cipherAes.doFinal(testo);
        System.out.println("MESSAGGIO DECRIPTATO: "+new String(result));
        return "messaggio ricevuto";
    }

    public String scambio(String chiave) throws IOException {
        Path directory = Paths.get("C:\\Users\\pi03873\\OneDrive - Alliance\\Desktop\\CYBERSECURITY\\serverB\\serverB\\chiavi_pubbliche-b");
        if (!Files.exists(directory)) {
            Files.createDirectories(directory); // crea la cartella se non esiste
        }

        Path pathFile = directory.resolve("chiave_pubblicaA.txt");
        Files.write(pathFile, chiave.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("File scritto in: " + pathFile.toAbsolutePath());
        return leggiFileDaResources("chiave_pubblicaA.txt");


    }
    public String leggiFileDaCartellaEsterna( String nomeFile) {
        Path pathFile = Paths.get("C:\\Users\\pi03873\\OneDrive - Alliance\\Desktop\\CYBERSECURITY\\serverA\\serverA\\chiavi_pubbliche-a", nomeFile);

        if (!Files.exists(pathFile)) {
            System.out.println("File non trovato: " + pathFile.toAbsolutePath());
            return null;
        }

        try {
            byte[] bytes = Files.readAllBytes(pathFile);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Errore nella lettura del file: " + pathFile.toAbsolutePath());
            e.printStackTrace();
            return null;
        }
    }

    private String leggiFileDaResources(String nomeFile) {
        try {
            ClassPathResource resource = new ClassPathResource(nomeFile);
            if (!resource.exists()) {
                return null;
            }
            byte[] bytes = resource.getInputStream().readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Log dell'errore se vuoi
            return null;
        }
    }
}
