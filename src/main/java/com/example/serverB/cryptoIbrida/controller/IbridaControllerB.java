package com.example.serverB.cryptoIbrida.controller;

import com.example.serverB.cryptoIbrida.dto.IbridResponse;
import com.example.serverB.cryptoIbrida.service.IbridaServiceB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@RestController
@RequestMapping(path = "api/v1/crypto/ibrida")
public class IbridaControllerB {


    @Autowired
    private IbridaServiceB ibridaServiceB;

    @RequestMapping(path ="/comunicate",method = RequestMethod.POST,consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> comunicate(@RequestBody String message) throws NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        String result=ibridaServiceB.comunicate(message);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @RequestMapping(path="/ricevimento", method = RequestMethod.POST)
    public ResponseEntity<String> ricevimento(@RequestBody IbridResponse ibridResponse) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String result=ibridaServiceB.ricevimento(ibridResponse);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @RequestMapping(path = "/hand-shake", method = RequestMethod.POST,consumes = MediaType.TEXT_PLAIN_VALUE, produces= MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> scambio(@RequestBody String publicKey) throws IOException {
        String result=ibridaServiceB.scambio(publicKey);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }




}
