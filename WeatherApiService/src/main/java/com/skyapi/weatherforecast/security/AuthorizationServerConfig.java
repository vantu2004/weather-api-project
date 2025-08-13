package com.skyapi.weatherforecast.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtClaimsSet.Builder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.skyapi.weatherforecast.clientapp.ClientAppRepository;
import com.skyapi.weatherforecast.common.ClientApp;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@Profile("production")
public class AuthorizationServerConfig {
	private final RsaKeyProperties rsaKeyProperties;

	@Value("${app.security.jwt.issuer}")
	private String issuerName;

	@Value("${app.security.jwt.access-token.expiration}")
	private int accessTokenExpirationTime;

	/*
	 * Khi Client gửi request đến API kèm Access Token (Bearer ...),
	 * NimbusJwtDecoder sử dụng khóa công khai từ rsaKeyProperties.getPublicKey() để
	 * xác minh chữ ký trong Access Token (JWT)
	 */
	@Bean
	public JwtDecoder jwtDecoder() {
		return NimbusJwtDecoder.withPublicKey(rsaKeyProperties.getPublicKey()).build();
	}

	/*
	 * JwtEncoder chịu trách nhiệm tạo JWT và ký nó bằng khóa bí mật RSA.
	 * 
	 * Khi client gửi request tới /oauth2/token, OAuth2 Server sử dụng
	 * RegisteredClientRepository để kiểm tra client_id và client_secret trong cơ sở
	 * dữ liệu (thông qua ClientAppRepository). Tiếp tục tạo header/payload của JWT.
	 * Cuối cùng dùng privateKey để ký cho JWT (phần này gọi là JWS)
	 */
	@Bean
	public JwtEncoder jwtEncoder() {
		JWK jwk = new RSAKey.Builder(rsaKeyProperties.getPublicKey()).privateKey(rsaKeyProperties.getPrivateKey())
				.build();

		JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));

		return new NimbusJwtEncoder(jwkSource);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Cấu hình SecurityFilterChain cho Authorization Server.
	 * 
	 * @param http đối tượng HttpSecurity dùng để cấu hình các rule bảo mật.
	 * @return SecurityFilterChain đã cấu hình cho Authorization Server.
	 */
	@Bean
	public SecurityFilterChain securityFilterChainOAuth2AuthorizationServer(HttpSecurity http) throws Exception {
		// Tạo cấu hình Authorization Server
		OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer
				.authorizationServer();

		http
				/*
				 * Chỉ áp dụng cấu hình bảo mật cho các request khớp với endpoints của
				 * Authorization Server
				 */
				.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
				// Gắn cấu hình Authorization Server vào HttpSecurity
				.with(authorizationServerConfigurer, (authorizationServer) ->
				// Bật hỗ trợ OpenID Connect (OIDC) với cấu hình mặc định
				authorizationServer.oidc(Customizer.withDefaults()))
				.authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated());

		return http.build();
	}

	/*
	 * RegisteredClientRepository để kiểm tra client_id và client_secret trong cơ sở
	 * dữ liệu (thông qua ClientAppRepository)
	 */
	@Bean
	public RegisteredClientRepository registeredClientRepository(ClientAppRepository clientAppRepository) {
		return new RegisteredClientRepository() {

			@Override
			public void save(RegisteredClient registeredClient) {

			}

			@Override
			public RegisteredClient findById(String id) {
				return null;
			}

			@Override
			public RegisteredClient findByClientId(String clientId) {
				Optional<ClientApp> clientAppOptional = clientAppRepository.findByClientId(clientId);
				if (!clientAppOptional.isPresent()) {
					return null;
				}

				ClientApp clientApp = clientAppOptional.get();
				return RegisteredClient.withId(clientApp.getId().toString()).clientId(clientApp.getClientId())
						.clientSecret(clientApp.getClientSecret()).clientName(clientApp.getName())
						.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
						.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
						.scope(clientApp.getRole().toString()).build();

			}
		};

	}

	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
		return new OAuth2TokenCustomizer<JwtEncodingContext>() {

			@Override
			public void customize(JwtEncodingContext context) {
				RegisteredClient registeredClient = context.getRegisteredClient();

				Builder builder = context.getClaims();
				builder.issuer(issuerName);
				builder.expiresAt(Instant.now().plus(accessTokenExpirationTime, ChronoUnit.MINUTES));
				builder.claims(new Consumer<Map<String, Object>>() {

					@Override
					public void accept(Map<String, Object> t) {
						t.put("scope", registeredClient.getScopes());
						t.put("name", registeredClient.getClientName());
						t.remove("aud");
					}
				});

			}
		};

	}
}
