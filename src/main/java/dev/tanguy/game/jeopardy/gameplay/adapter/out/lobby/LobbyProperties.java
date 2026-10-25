package dev.tanguy.game.jeopardy.gameplay.adapter.out.lobby;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.core.io.Resource;

import java.time.Duration;

@ConfigurationProperties("app.lobby")
public record LobbyProperties(
        /** e.g. https://lobby.geraedts.dev */
        String baseUrl,
        /** Where the lobby sends players when the game starts. {gameSessionId} is replaced. */
        String launchUrl,
        /** Optional. Sent as X-Api-Key once you protect POST /lobbies on the lobby side. */
        String apiKey,
        /** The lobby's lobby-public.pem, used to verify its handoff tokens. Required. */
        Resource publicKeyLocation,
        @DefaultValue("lobby-service") String issuer,
        @DefaultValue("game-server") String handoffAudience,
        /** Audience of the per-player game tokens the lobby hands each player (used to buzz). */
        @DefaultValue("game-player") String playerAudience,
        // ---- How the lobby page looks (sent with every new lobby; set in application.yaml) ----
        @DefaultValue("Jeopardy") String gameName,
        /** Optional image URL shown instead of gameName at the top of the lobby. Blank = the name as text. */
        String gameBanner,
        @DefaultValue("#0F172A") String backgroundColor,
        /** Optional image URL behind the lobby (cover, darkened). Blank = backgroundColor only. */
        String backgroundImage,
        /** Players needed in the lobby before its host (the first player to join) can start the game. */
        @DefaultValue("1") int minPlayers,
        @DefaultValue("true") boolean balancedTeams,
        /** The lobby deletes itself this long after creation, regardless of activity. */
        @DefaultValue("4h") Duration ttl,
        @DefaultValue("2s") Duration connectTimeout,
        @DefaultValue("5s") Duration readTimeout
) {}