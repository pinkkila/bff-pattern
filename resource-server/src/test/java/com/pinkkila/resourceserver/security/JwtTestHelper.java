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
    
    // These values must match the public key in MockWebServerPropertySource
    // In a real project, you'd ideally generate these dynamically or
    // load them from a PEM file in src/test/resources.
//    private static final String PRIVATE_KEY_JSON = "{ \"keys\": [ { \"kty\": \"RSA\", \"e\": \"AQAB\", \"n\": \"jvBtqsGCOmnYzwe_-HvgOqlKk6HPiLEzS6uCCcnVkFXrhnkPMZ-uQXTR0u-7ZklF0XC7-AMW8FQDOJS1T7IyJpCyeU4lS8RIf_Z8RX51gPGnQWkRvNw61RfiSuSA45LR5NrFTAAGoXUca_lZnbqnl0td-6hBDVeHYkkpAsSck1NPhlcsn-Pvc2Vleui_Iy1U2mzZCM1Vx6Dy7x9IeP_rTNtDhULDMFbB_JYs-Dg6Zd5Ounb3mP57tBGhLYN7zJkN1AAaBYkElsc4GUsGsUWKqgteQSXZorpf6HdSJsQMZBDd7xG8zDDJ28hGjJSgWBndRGSzQEYU09Xbtzk-8khPuw\" } ] }"; // Your private key JSON
    private static final String PRIVATE_KEY_JSON = """
            {
              "kty": "RSA",
              "use": "sig",
              "kid": "test-key-id",
              "alg": "RS256",
              "n": "jvBtqsGCOmnYzwe_-HvgOqlKk6HPiLEzS6uCCcnVkFXrhnkPMZ-uQXTR0u-7ZklF0XC7-AMW8FQDOJS1T7IyJpCyeU4lS8RIf_Z8RX51gPGnQWkRvNw61RfiSuSA45LR5NrFTAAGoXUca_lZnbqnl0td-6hBDVeHYkkpAsSck1NPhlcsn-Pvc2Vleui_Iy1U2mzZCM1Vx6Dy7x9IeP_rTNtDhULDMFbB_JYs-Dg6Zd5Ounb3mP57tBGhLYN7zJkN1AAaBYkElsc4GUsGsUWKqgteQSXZorpf6HdSJsQMZBDd7xG8zDDJ28hGjJSgWBndRGSzQEYU09Xbtzk-8khPuw",
              "e": "AQAB"
            }
            """;
    
    public static String generateToken(String subject, List<String> scopes) throws Exception {
        RSAKey rsaJWK = RSAKey.parse(PRIVATE_KEY_JSON);
        RSASSASigner signer = new RSASSASigner(rsaJWK);
        
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer("http://mock-auth-server") // Matches the issuer in your config
                .claim("scope", String.join(" ", scopes))
                .expirationTime(new Date(new Date().getTime() + 60 * 1000))
                .build();
        
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaJWK.getKeyID()).build(),
                claimsSet);
        
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }
}