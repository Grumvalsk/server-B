package com.example.serverB.cryptoAsimmetrica.controller;

import com.example.serverB.cryptoAsimmetrica.dto.AsimmetricResponse;
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

    @RequestMapping(path="/ricevimento", method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String>ricevimento(@RequestBody AsimmetricResponse response) throws NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        String result= serviceB.ricevimento(response);
        return ResponseEntity.status(HttpStatus.OK).body(result);


    }

    @RequestMapping(value = "/hand-shake", method = RequestMethod.POST,consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> scambio(@RequestBody String chiave) throws IOException {
        String result=serviceB.scambio(chiave);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
