package com.cairnfg.waypoint.authorization.configuration.cryptography;

import com.nimbusds.jose.jwk.RSAKey;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RsaConfiguration {

  private final String privateKey;
  private final String publicKey;

  public RsaConfiguration(@Value("${waypoint.authorization.oauth2.private-key}") String privateKey,
      @Value("${waypoint.authorization.oauth2.public-key}") String publicKey) {
    this.privateKey = privateKey;
    this.publicKey = publicKey;
  }

  private PublicKey getPublicKey()
      throws InvalidKeySpecException, NoSuchAlgorithmException {
    byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey.getBytes(StandardCharsets.UTF_8));
    X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return keyFactory.generatePublic(spec);
  }

  private PrivateKey getPrivateKey()
      throws InvalidKeySpecException, NoSuchAlgorithmException {
    byte[] privateKeyBytes = Base64.getDecoder()
        .decode(privateKey.getBytes(StandardCharsets.UTF_8));
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
    KeyFactory fact = KeyFactory.getInstance("RSA");
    return fact.generatePrivate(keySpec);
  }

  @Bean
  public RSAKey rsaKey()
      throws InvalidKeySpecException, NoSuchAlgorithmException {
    RSAPublicKey publicKey = (RSAPublicKey) getPublicKey();
    RSAPrivateKey privateKey = (RSAPrivateKey) getPrivateKey();
    return new RSAKey.Builder(publicKey)
        .privateKey(privateKey)
        .keyID(UUID.randomUUID().toString())
        .build();
  }
}
