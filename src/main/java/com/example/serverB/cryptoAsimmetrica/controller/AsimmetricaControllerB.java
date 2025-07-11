package com.example.serverB.cryptoAsimmetrica.controller;

import com.example.serverB.cryptoAsimmetrica.service.AsimmetricServiceB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@RestController
@RequestMapping(path="api/v1/crypto/asymmetric")
public class AsimmetricaControllerB {

    @Autowired
    private AsimmetricServiceB serviceB;

    @RequestMapping(path="/comunicate",method = RequestMethod.POST)
    public ResponseEntity<String> comunicazione(@RequestBody String message) throws NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        String reuslt=serviceB.comunicate(message);
        return ResponseEntity.status(HttpStatus.OK).body(reuslt);
    }

    @RequestMapping(path="/ricevimento", method = RequestMethod.GET)
    public ResponseEntity<String> ricezione(@RequestParam("message")String message){
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @RequestMapping(value = "/hand-shake", method = RequestMethod.POST,consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> scambio(@RequestBody String chiave) throws IOException {
        String result=serviceB.scambio(chiave);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
