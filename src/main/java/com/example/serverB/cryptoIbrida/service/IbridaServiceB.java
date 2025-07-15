package com.example.serverB.cryptoIbrida.service;

import com.example.serverB.configuration.RestTemplateConfig;
import com.example.serverB.cryptoIbrida.dto.IbridResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
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
public class IbridaServiceB {

    @Value("${url.ibrid.handshake}")
    private  String urlHandShake;

    @Value("${url.ibrid.ricevimento}")
    private   String urlRivevimento;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RestTemplateConfig config;



    public String comunicate(String message) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String chiavePubA= leggiFileDaCartellaEsterna("chiave_pubblicaA.txt").replaceAll("\\r?\\n", "").trim();
        if(chiavePubA==null){
            String chiavePubB=leggiFileDaResources("chiave_pubblicaB.txt");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            HttpEntity<String> request= new HttpEntity<>(chiavePubB);
            HttpEntity<String>response=restTemplate.postForEntity(urlHandShake,request,String.class);
            chiavePubA=response.getBody().replaceAll("\\r?\\n", "").trim();
            Path directory = Paths.get(config.getCartellaChiavi());
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            Path pathFile = directory.resolve("chiave_pubblicaA.txt");
            Files.write(pathFile, chiavePubA.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("File scritto in: " + pathFile.toAbsolutePath());
        }
        //gestione chiave pubblica del  server ricevente
        byte[] byteChiavePubA = Base64.getDecoder().decode(chiavePubA);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(byteChiavePubA);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pKey = keyFactory.generatePublic(keySpec);
        SecretKey aesKey=generazioneChiaveSimmetrica();
        Cipher cipher= Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE,aesKey);
        byte[]testo=message.getBytes(StandardCharsets.UTF_8);
        byte[]result=cipher.doFinal(testo);
        String messaggioCriptato = Base64.getEncoder().encodeToString(result);
        IbridResponse ibridResponse= new IbridResponse();
        ibridResponse.setMessage(messaggioCriptato);
        Cipher cipher1= Cipher.getInstance("RSA");
        cipher1.init(Cipher.ENCRYPT_MODE,pKey);
        byte[] aesKeyBytes = aesKey.getEncoded();
        byte[]resultKey=cipher1.doFinal(aesKeyBytes);
        ibridResponse.setKey(Base64.getEncoder().encodeToString(resultKey));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<IbridResponse> request=new HttpEntity<>(ibridResponse);
        HttpEntity<String>response=restTemplate.postForEntity(urlRivevimento,request,String.class);
        return response.getBody();
    }

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
        Path directory = Paths.get(config.getCartellaChiavi());
        if (!Files.exists(directory)) {
            Files.createDirectories(directory); // crea la cartella se non esiste
        }

        Path pathFile = directory.resolve("chiave_pubblicaA.txt");
        Files.write(pathFile, chiave.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("File scritto in: " + pathFile.toAbsolutePath());
        return leggiFileDaResources("chiave_pubblicaB.txt");


    }

    private SecretKey generazioneChiaveSimmetrica() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        return keyGen.generateKey();

    }
    public String leggiFileDaCartellaEsterna( String nomeFile) {
        Path pathFile = Paths.get(config.getCartellaChiavi(), nomeFile);

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
