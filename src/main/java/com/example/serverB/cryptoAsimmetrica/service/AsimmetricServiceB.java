package com.example.serverB.cryptoAsimmetrica.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class AsimmetricServiceB {

    @Autowired
    private RestTemplate restTemplate;

    private final String ulrHandShake="http://localhost:8080/api/v1/crypto/asimmetrica/hand-shake";
    private final  String urlComunicate="http://localhost:8080/api/v1/crypto/asimmetrica/ricevimento";

    public String comunicate(String message) throws IOException, NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {

        String chiavePubblicaA = leggiFileDaCartellaEsterna("chiave_pubblicaA.txt");

        if (chiavePubblicaA == null) {
            String chiavePubblicaServer = leggiFileDaResources("chiave_pubblicaB.txt");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            HttpEntity<String> httpEntity = new HttpEntity<>(chiavePubblicaServer, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(ulrHandShake, httpEntity, String.class);
            chiavePubblicaA = response.getBody();
            Path directory = Paths.get("C:\\Users\\pi03873\\OneDrive - Alliance\\Desktop\\CYBERSECURITY\\serverB\\serverB\\chiavi_pubbliche-b");
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            Path pathFile = directory.resolve("chiave_pubblicaA.txt");
            Files.write(pathFile, chiavePubblicaA.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("File scritto in: " + pathFile.toAbsolutePath());
        }

        // Scrittura su disco


        // Cifratura
        byte[] chiavePubA = Base64.getDecoder().decode(chiavePubblicaA);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(chiavePubA);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pKey = keyFactory.generatePublic(keySpec);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pKey);

        byte[] testo = message.getBytes(StandardCharsets.UTF_8);
        byte[] result = cipher.doFinal(testo);
        String messaggioCriptato = Base64.getEncoder().encodeToString(result);

        // Invio messaggio criptato
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> entity = new HttpEntity<>(messaggioCriptato, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(urlComunicate, entity, String.class);
        return response.getBody();
    }



    public String decript(String message) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String chiavePrivata= leggiFileDaResources("chiave_privata.txt");
        PrivateKey privateKey= getPrivateKey();
        return null;
    }

    public String scambio(String chiave) throws IOException {
        Path directory = Paths.get("target/resources");
        if (!Files.exists(directory)) {
            Files.createDirectories(directory); // crea la cartella se non esiste
        }

        Path pathFile = directory.resolve("chiave_pubblicaA.txt");
        Files.write(pathFile, chiave.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        System.out.println("File scritto in: " + pathFile.toAbsolutePath());
        return Base64.getEncoder().encodeToString(leggiFileDaResources("chiave_pubblicaB.txt").getBytes());

    }
    private PublicKey getPublicKeyServerA() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String privateKey=leggiFileDaResources("chiave_pubblicaA.txt");
        byte[]keyByte= Base64.getDecoder().decode(privateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyByte);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    private PrivateKey getPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String privateKey=leggiFileDaResources("chiave_privataB.txt");
        byte[]keyByte= Base64.getDecoder().decode(privateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyByte);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey pKey = keyFactory.generatePrivate(keySpec);
        return pKey;
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

    public String leggiFileDaCartellaEsterna( String nomeFile) {
        Path pathFile = Paths.get("C:\\Users\\pi03873\\OneDrive - Alliance\\Desktop\\CYBERSECURITY\\serverB\\serverB\\chiavi_pubbliche-b", nomeFile);

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

    public static void genPairKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator=KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        KeyPair paioChiavi=keyPairGenerator.generateKeyPair();
        String privateKeyBase64 = Base64.getEncoder().encodeToString(paioChiavi.getPrivate().getEncoded());
        System.out.println("CHIAVE PRIVATA: "+privateKeyBase64);
        String publicKey = Base64.getEncoder().encodeToString(paioChiavi.getPublic().getEncoded());
        System.out.println("CHIAVE PUBBLICA: "+publicKey);
    }
}
