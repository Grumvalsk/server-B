package com.example.serverB.cryptoAsimmetrica.service;

import com.example.serverB.cryptoAsimmetrica.dto.AsimmetricResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Arrays;
import java.util.Base64;

@Service
public class AsimmetricServiceB {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url.asimmetric.handshake}")
    private  String ulrHandShake;

    @Value("${url.asimmetric.handshake}")
    private   String urlComunicate;

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
            chiavePubblicaA = response.getBody().replaceAll("\\r?\\n", "").trim();
            Path directory = Paths.get("C:\\Users\\pi03873\\OneDrive - Alliance\\Desktop\\CYBERSECURITY\\serverB\\serverB\\chiavi_pubbliche-b");
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            Path pathFile = directory.resolve("chiave_pubblicaA.txt");
            Files.write(pathFile, chiavePubblicaA.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("File scritto in: " + pathFile.toAbsolutePath());
        }

        // Scrittura su disco


        // Cifratura testo
        byte[] chiavePubA = Base64.getDecoder().decode(chiavePubblicaA);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(chiavePubA);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pKey = keyFactory.generatePublic(keySpec);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pKey);

        byte[] testo = message.getBytes(StandardCharsets.UTF_8);
        byte[] result = cipher.doFinal(testo);
        String messaggioCriptato = Base64.getEncoder().encodeToString(result);

        //Cifratura firma
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(message.getBytes(StandardCharsets.UTF_8));
        String base64Privata = Files.readString(Paths.get("chiave_privata.txt"));
        byte[] chiavePrivataBytes = Base64.getDecoder().decode(base64Privata);

        // Crea il KeySpec
        PKCS8EncodedKeySpec keySpecPrivate = new PKCS8EncodedKeySpec(chiavePrivataBytes);

        // Genera la chiave privata
        KeyFactory keyFactoryPrivate = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactoryPrivate.generatePrivate(keySpecPrivate);
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] resultFirma = cipher.doFinal(digest);
        String firmaCriptata = Base64.getEncoder().encodeToString(resultFirma);
        AsimmetricResponse asimmetricResponse= new AsimmetricResponse();
        asimmetricResponse.setFirma(firmaCriptata);
        asimmetricResponse.setMessaggio(messaggioCriptato);
        // Invio messaggio criptato
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<AsimmetricResponse> entity = new HttpEntity<>(asimmetricResponse, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(urlComunicate, entity, String.class);
        return response.getBody();
    }

    public String ricevimento(AsimmetricResponse response) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String keyPrivata=leggiFileDaResources("chiave_privataB.txt");
        byte[]chiavePrivataB=Base64.getDecoder().decode(keyPrivata);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(chiavePrivataB);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey pKey = keyFactory.generatePrivate(keySpec);
        Cipher chiper=Cipher.getInstance("RSA");
        chiper.init(Cipher.DECRYPT_MODE,pKey);
        byte[] messaggioDecodificato = Base64.getDecoder().decode(response.getMessaggio());
        byte[] result = chiper.doFinal(messaggioDecodificato);
        String messaggioRicevuto=new String(result);
        System.out.println(messaggioRicevuto);
       // verifica firma
        String keyPubblicaA=leggiFileDaCartellaEsterna("chiave_pubblicaA.txt").replaceAll("\\r?\\n", "").trim();
        byte[]chiavePubB=Base64.getDecoder().decode(keyPubblicaA);
        X509EncodedKeySpec keySpecFirm = new X509EncodedKeySpec(chiavePubB);
        KeyFactory keyFactoryFirm = KeyFactory.getInstance("RSA");
        PublicKey publicKeyB = keyFactoryFirm.generatePublic(keySpecFirm);
        Cipher cipher = Cipher.getInstance("RSA");
        byte[] firma = Base64.getDecoder().decode(response.getFirma());
        cipher.init(Cipher.DECRYPT_MODE, publicKeyB);
        byte[] digestDaFirma = cipher.doFinal(firma); // questo è un array di byte!
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digestCalcolato = md.digest(messaggioRicevuto.getBytes(StandardCharsets.UTF_8));

        if (!Arrays.equals(digestDaFirma, digestCalcolato)) {

            return "firma non autentica";
        }



        return "Messaggio ricevuto";
    }



    public String scambio(String chiave) throws IOException {
        Path directory = Paths.get("C:\\Users\\pi03873\\OneDrive - Alliance\\Desktop\\CYBERSECURITY\\serverB\\serverB\\chiavi_pubbliche-b");
        if (!Files.exists(directory)) {
            Files.createDirectories(directory); // crea la cartella se non esiste
        }

        Path pathFile = directory.resolve("chiave_pubblicaA.txt");
        Files.write(pathFile, chiave.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        System.out.println("File scritto in: " + pathFile.toAbsolutePath());
        return leggiFileDaResources("chiave_pubblicaB.txt");

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
