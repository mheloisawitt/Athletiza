package br.com.athletiza.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hash de senhas com PBKDF2 (CH-12). A senha nunca é gravada em texto puro.
 *
 * Formato gravado no banco: pbkdf2$iteracoes$salt$hash (salt e hash em Base64).
 */
public final class Senha {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACOES = 120_000;
    private static final int TAMANHO_SALT = 16;
    private static final int TAMANHO_HASH = 256;

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Senha() {
    }

    public static String gerarHash(char[] senha) {
        byte[] salt = new byte[TAMANHO_SALT];
        ALEATORIO.nextBytes(salt);
        byte[] hash = calcular(senha, salt, ITERACOES);
        Base64.Encoder base64 = Base64.getEncoder();
        return "pbkdf2$" + ITERACOES + "$" + base64.encodeToString(salt) + "$" + base64.encodeToString(hash);
    }

    public static boolean conferir(char[] senha, String hashGravado) {
        if (senha == null || hashGravado == null) {
            return false;
        }
        String[] partes = hashGravado.split("\\$");
        if (partes.length != 4 || !"pbkdf2".equals(partes[0])) {
            return false;
        }
        try {
            int iteracoes = Integer.parseInt(partes[1]);
            byte[] salt = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            return MessageDigest.isEqual(esperado, calcular(senha, salt, iteracoes));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] calcular(char[] senha, byte[] salt, int iteracoes) {
        PBEKeySpec especificacao = new PBEKeySpec(senha, salt, iteracoes, TAMANHO_HASH);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(especificacao).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Algoritmo de senha indisponível: " + ALGORITMO, e);
        } finally {
            especificacao.clearPassword();
        }
    }

    /** Gera o hash de uma senha pela linha de comando (útil para scripts SQL). */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java br.com.athletiza.util.Senha <senha>");
            return;
        }
        char[] senha = args[0].toCharArray();
        System.out.println(gerarHash(senha));
        Arrays.fill(senha, ' ');
    }
}
