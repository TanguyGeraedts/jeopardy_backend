package dev.tanguy.game.jeopardy.gameplay.adapter.out.lobby;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(LobbyProperties.class)
public class LobbyClientConfig {

    @Bean
    public RestClient lobbyRestClient(LobbyProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());

        RestClient.Builder clientBuilder = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory);

        if (StringUtils.hasText(properties.apiKey())) {
            clientBuilder = clientBuilder.defaultHeader("X-Api-Key", properties.apiKey());
        }
        return clientBuilder.build();
    }
}