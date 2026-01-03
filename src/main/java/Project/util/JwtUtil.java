package Project.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

	private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
	@Value("${jwt.secret:mySecretKey12345mySecretKey12345mySecretKey12345}")
	private String secretKeyString;

	@Value("${jwt.expiration:86400000}")
	private long EXPIRATION_TIME; // 24 hours in milliseconds

	private Key SECRET_KEY;

	/**
	 * Initialize the secret key after properties are injected This ensures the key
	 * remains consistent across app restarts
	 */
	@PostConstruct
	public void init() {
		try {
			if (secretKeyString.length() < 32) {
				logger.warn("JWT secret key is too short. Padding to 32 characters.");
				secretKeyString = String.format("%-32s", secretKeyString).replace(' ', '0');
			}
			byte[] keyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
			SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);

			logger.info("-> JWT Secret Key initialized successfully");
		} catch (Exception e) {
			logger.error("!! Failed to initialize JWT secret key", e);
			SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
			logger.warn("! Using generated secret key. Tokens will be invalidated on restart!");
		}
	}

	public String generateToken(String username) {
		Map<String, Object> claims = new HashMap<>();
		return createToken(claims, username);
	}

	public String generateTokenWithClaims(String username, Map<String, Object> additionalClaims) {
		return createToken(additionalClaims, username);
	}

	private String createToken(Map<String, Object> claims, String subject) {
		Date now = new Date(System.currentTimeMillis());
		Date expirationDate = new Date(System.currentTimeMillis() + EXPIRATION_TIME);

		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(now).setExpiration(expirationDate)
				.signWith(SECRET_KEY).compact();
	}

	public Boolean validateToken(String token, String username) {
		try {
			final String extractedUsername = extractUsername(token);
			return (extractedUsername.equals(username) && !isTokenExpired(token));
		} catch (Exception e) {
			logger.error("Token validation failed", e);
			return false;
		}
	}

	public boolean isTokenValid(String token, org.springframework.security.core.userdetails.UserDetails userDetails) {
		try {
			final String username = extractUsername(token);
			boolean valid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);

			if (valid) {
				logger.debug("Token is valid for user: {}", username);
			} else {
				logger.warn("Token validation failed for user: {}", username);
			}

			return valid;
		} catch (ExpiredJwtException e) {
			logger.warn("Token has expired");
			return false;
		} catch (MalformedJwtException e) {
			logger.warn("Malformed JWT token");
			return false;
		} catch (SignatureException e) {
			logger.warn("Invalid JWT signature");
			return false;
		} catch (UnsupportedJwtException e) {
			logger.warn("Unsupported JWT token");
			return false;
		} catch (IllegalArgumentException e) {
			logger.warn("JWT claims string is empty");
			return false;
		}
	}

	public String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
	}

	public Date extractExpiration(String token) {
		return extractAllClaims(token).getExpiration();
	}

	public boolean isTokenExpired(String token) {
		try {
			Date expiration = extractExpiration(token);
			return expiration.before(new Date());
		} catch (ExpiredJwtException e) {
			return true;
		}
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody();
	}

	public long getExpirationTime() {
		return EXPIRATION_TIME;
	}

	/**
	 * Refresh an existing token by generating a new one with the same subject
	 */
	public String refreshToken(String token) {
		try {
			String username = extractUsername(token);
			return generateToken(username);
		} catch (Exception e) {
			logger.error("Failed to refresh token", e);
			throw new RuntimeException("Invalid token for refresh");
		}
	}
}