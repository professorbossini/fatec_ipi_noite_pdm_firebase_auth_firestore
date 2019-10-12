package br.com.bossini.fatec_ipi_noite_pdm_firebase_auth_firestore;

import java.util.Date;

class Mensagem implements Comparable <Mensagem> {

    @Override
    public int compareTo(Mensagem mensagem) {
        return this.data.compareTo(mensagem.data);
    }

    private String texto;
    private Date data;
    private String email;

    public Mensagem() {
    }

    public Mensagem(String texto, Date data, String email) {
        this.texto = texto;
        this.data = data;
        this.email = email;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
