package com.example.serverB;

import com.example.serverB.cryptoAsimmetrica.service.AsimmetricServiceB;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.NoSuchAlgorithmException;

@SpringBootApplication
public class ServerBApplication {

	public static void main(String[] args) throws NoSuchAlgorithmException {
		SpringApplication.run(ServerBApplication.class, args);
//		AsimmetricServiceB.genPairKey();
	}

}
