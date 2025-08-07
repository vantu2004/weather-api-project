package com.skyapi.weatherforecast.security;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
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
public class AuthorizationServerConfig {
	private final RsaKeyProperties rsaKeyProperties;

	// Bean để giải mã (decode) JWT sử dụng public key
	@Bean
	public JwtDecoder jwtDecoder() {
		/*
		 * Tạo một JwtDecoder sử dụng public key từ class rsaKeyProperties. Dùng để xác
		 * minh token được gửi từ client
		 */
		return NimbusJwtDecoder.withPublicKey(rsaKeyProperties.getPublicKey()).build();
	}

	// Bean để mã hóa (encode) JWT sử dụng private key
	@Bean
	public JwtEncoder jwtEncoder() {
		/*
		 * Tạo một đối tượng JWK (JSON Web Key) từ cặp khóa RSA JWK là định dạng chuẩn
		 * để biểu diễn khóa dưới dạng JSON
		 */
		JWK jwk = new RSAKey.Builder(rsaKeyProperties.getPublicKey()) // Thiết lập public key
				.privateKey(rsaKeyProperties.getPrivateKey()) // Thiết lập private key (dùng để ký token)
				.build();

		/*
		 * Tạo một nguồn JWK bất biến (immutable) chứa JWK vừa tạo Đây là nơi
		 * NimbusJwtEncoder sẽ lấy khóa để ký token
		 */
		JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));

		// Tạo một encoder để tạo JWT, sử dụng nguồn JWK đã khai báo ở trên
		return new NimbusJwtEncoder(jwkSource);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
		OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer
				.authorizationServer();

		http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
				.with(authorizationServerConfigurer,
						(authorizationServer) -> authorizationServer.oidc(Customizer.withDefaults()))
				.authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated());

		return http.build();
	}

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
				return RegisteredClient.withId(clientApp.getId().toString()).clientId(clientApp.getId().toString())
						.clientSecret(clientApp.getClientSecret())
						.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
						.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
						.scope(clientApp.getRole().toString()).build();

			}
		};

	}
}
