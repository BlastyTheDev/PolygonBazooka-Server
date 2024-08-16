package me.blasty.polygonbazookaserver.api.security.jwt;

import com.auth0.jwt.JWT;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JWTService {

    private static final String KEY = Dotenv.load().get("JWT_SIGNING_KEY");

    private Key getSigningKey() {
        byte[] bytes = Decoders.BASE64.decode(KEY);
        return Keys.hmacShaKeyFor(bytes);
    }

    public String createToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        // expire in 100 years (doesnt expire)
        Date expiration = new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 36525);
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public String createToken(UserDetails userDetails) {
        return createToken(Map.of(), userDetails);
    }

    public String getSubject(String jwt) {
        return JWT.decode(jwt).getSubject();
    }

    public boolean isTokenExpired(String jwt) {
        return JWT.decode(jwt).getExpiresAt().before(new Date(System.currentTimeMillis()));
    }

    // valid if not expired, subject matches user, and token is in database (known)
    public boolean isTokenValid(String jwt, UserDetails userDetails) {
        return !isTokenExpired(jwt) && getSubject(jwt).equals(userDetails.getUsername());
    }
    
}
