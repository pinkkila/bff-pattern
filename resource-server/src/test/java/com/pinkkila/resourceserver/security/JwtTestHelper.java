package com.pinkkila.resourceserver.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.Date;
import java.util.List;

public class JwtTestHelper {
    
    public static final String DEFAULT_ISSUER = "http://mock-auth-server";
    
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
    
    public static String generateToken(String subject, List<String> scopes) throws Exception {
        return generateToken(subject, scopes, DEFAULT_ISSUER);
    }
    
    public static String generateToken(String subject, List<String> scopes, String issuer) throws Exception {
        RSASSASigner signer = new RSASSASigner(RSA_KEY);
        
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer(issuer)
                .claim("scope", String.join(" ", scopes))
                .expirationTime(new Date(new Date().getTime() + 60 * 1000))
                .build();
        
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(RSA_KEY.getKeyID()).build(),
                claimsSet);
        
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }
}