package dev.tanguy.game.jeopardy.gameplay.core.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.TeamId;
import dev.tanguy.game.jeopardy.gameplay.domain.model.Player;
import dev.tanguy.game.jeopardy.gameplay.domain.model.Team;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.AddPlayerCommand;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.AddPlayerResult;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.AddPlayerUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AddPlayerUseCaseImpl implements AddPlayerUseCase {

    private final GameSessionUpdater updater;

    @Override
    public AddPlayerResult addPlayer(AddPlayerCommand command) {
        return updater.updateOwned(command.sessionId(), command.requesterId(), session -> {
            Optional<Player> existing = session.findPlayer(command.playerId());
            if (existing.isPresent()) {
                return new AddPlayerResult(command.playerId(), existing.get().teamId(), true);
            }

            TeamId teamId = null;
            if (command.externalTeamId() != null) {
                Team team = session.findTeamByExternalId(command.externalTeamId())
                        .orElseGet(() -> session.createTeam(
                                TeamId.generate(), command.teamName(), command.externalTeamId(), command.teamColour()));
                teamId = team.getId();
            }

            session.addPlayer(command.playerId(), command.playerName(), teamId);

            TeamId assignedTeamId = session.findPlayer(command.playerId())
                    .map(Player::teamId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Player " + command.playerId() + " was added but not found in session " + session.getId()));

            return new AddPlayerResult(command.playerId(), assignedTeamId, false);
        });
    }
}
