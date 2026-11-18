package com.pinkkila.resourceserver.mockauthserver;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.pinkkila.resourceserver.userid.UserId;

import java.util.Date;
import java.util.List;

/*
* check: https://docs.spring.io/spring-authorization-server/reference/getting-started.html#defining-required-components
*/
public class JwtTestHelper {
    
    public static final String DEFAULT_ISSUER = "http://mock-auth-server";

    // The audience the resource server accepts, set in application.yaml
    public static final String DEFAULT_AUDIENCE = "http://localhost:8090";

    // Generate a pair once for the entire test session
    public static final RSAKey RSA_KEY = generate();
    
    private static RSAKey generate() {
        try {
            return new com.nimbusds.jose.jwk.gen.RSAKeyGenerator(2048)
                    .keyID("test-key-id")
                    .generate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate test RSA key", e);
        }
    }

    public static String jwkSet() {
        return new JWKSet(RSA_KEY.toPublicJWK()).toString();
    }

    public static String generateToken(String subject, List<String> scopes, UserId userIdValue) throws Exception {
        return generateToken(subject, scopes, DEFAULT_ISSUER, userIdValue);
    }
    
    public static String generateToken(String subject, List<String> scopes, String issuer, UserId userIdValue) throws Exception {
        return generateToken(subject, scopes, issuer, List.of(DEFAULT_AUDIENCE), userIdValue);
    }

    public static String generateToken(String subject, List<String> scopes, String issuer, List<String> audience,
                                       UserId userIdValue) throws Exception {
        RSASSASigner signer = new RSASSASigner(RSA_KEY);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer(issuer)
                .audience(audience)
                .claim("scope", String.join(" ", scopes))
                .claim("user_id", userIdValue.toString())
                .expirationTime(new Date(new Date().getTime() + 60 * 1000))
                .build();
        
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(RSA_KEY.getKeyID()).build(),
                claimsSet);
        
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }
}