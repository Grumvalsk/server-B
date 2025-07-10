package com.example.serverB.cryptoSimmetrica.controller;

import com.example.serverB.cryptoSimmetrica.dto.BaseResponse;
import com.example.serverB.cryptoSimmetrica.service.CrittografiaServiceB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@RestController
@RequestMapping("api/v1/crittografia/simmetrica/")
public class CrittografiaControllerB {


    @Autowired
    private CrittografiaServiceB service;

    @RequestMapping(path="ricezione", method = RequestMethod.POST)
    public ResponseEntity<String>ricezione(@RequestBody BaseResponse response) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        service.ricezioneMessaggio(response);
        return ResponseEntity.status(HttpStatus.OK).body("comunicazione ricevuta");
    }
}
