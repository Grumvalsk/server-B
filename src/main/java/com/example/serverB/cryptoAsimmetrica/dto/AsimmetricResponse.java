package com.example.serverB.cryptoAsimmetrica.dto;


public class AsimmetricResponse {

    private String messaggio;
    private String firma;

    public String getMessaggio() {
        return messaggio;
    }

    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }

    public String getFirma() {
        return firma;
    }

    public void setFirma(String firma) {
        this.firma = firma;
    }
}
