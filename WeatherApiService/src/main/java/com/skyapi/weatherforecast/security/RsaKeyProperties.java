package com.skyapi.weatherforecast.security;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
/*
 * Tìm các cấu hình bắt đầu bằng rsa trong application.properties và ánh xạ vào
 * các trường tương ứng, tuy nhiên Spring tạo một Bean từ class này, nhưng sẽ
 * không tự động binding dữ liệu từ file application.properties vào các field
 * trừ khi thêm @EnableConfigurationProperties(RsaKeyProperties.class) vào Main
 * class
 */
@ConfigurationProperties(prefix = "rsa")
@Getter
@Setter
public class RsaKeyProperties {
	private RSAPublicKey publicKey;
	private RSAPrivateKey privateKey;

}
