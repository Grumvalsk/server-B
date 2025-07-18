package com.example.serverB.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Value("${url.cartella.chiavi}")
    public  String cartellaChiavi;

    public String getCartellaChiavi() {
        return cartellaChiavi;
    }
}
