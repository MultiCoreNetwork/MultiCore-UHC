package it.multicoredev.uhc.listeners;

import it.multicoredev.mbcore.spigot.chat.Chat;
import it.multicoredev.uhc.Game;
import it.multicoredev.uhc.events.UHCRespawnEvent;
import it.multicoredev.uhc.storage.Config;
import it.multicoredev.uhc.storage.GameFile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Copyright © 2019 - 2022 by Lorenzo Magni
 * This file is part of MultiCoreUHC.
 * MultiCoreUHC is under "The 3-Clause BSD License", you can find a copy <a href="https://opensource.org/licenses/BSD-3-Clause">here</a>.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
public class PlayerRespawnListener implements Listener {
    private final Plugin plugin;
    private final Config config;
    private final Game game;

    public PlayerRespawnListener(Plugin plugin, Config config, Game game) {
        this.plugin = plugin;
        this.config = config;
        this.game = game;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!game.isRunning()) return;

        Player player = event.getPlayer();

        UHCRespawnEvent respawnEvent = new UHCRespawnEvent(game, player);
        Bukkit.getPluginManager().callEvent(respawnEvent);

        if (respawnEvent.isCancelled()) return;

        if (canRespawn(player, game, config)) {
            game.currentGame().respawnPlayer(player.getUniqueId(), false);
            Bukkit.getScheduler().runTaskLater(plugin, () -> player.setHealth(config.game.respawnHealth), 10);

            if (config.game.respawnToDeath) {
                event.setRespawnLocation(game.deathLocation.get(player));
            } else {
                if (config.game.respawnToTeam) {
                    List<Player> members = getTeamMembers(player)
                            .stream()
                            .filter(member -> game.currentGame().getAlivePlayers().contains(member.getUniqueId()))
                            .collect(Collectors.toList()); //TODO To check

                    if (!members.isEmpty()) event.setRespawnLocation(members.get(0).getLocation());
                    else event.setRespawnLocation(game.deathLocation.get(player));
                }
            }

            Chat.broadcast(config.messages.playerRespawn
                    .replace("{player}", player.getDisplayName())
                    .replace("{health}", String.valueOf(config.game.respawnHealth)));
            Chat.info("&e" + player.getName() + " died and respawned");
        }
    }

    public static boolean canRespawn(Player player, Game game, Config config) {
        return config.game.canRespawn &&
                game.getTimer() < (config.game.respawnTimeLimit * 60) &&
                game.currentGame().getPlayers().containsKey(player.getUniqueId().toString()) &&
                game.currentGame().getPlayers().get(player.getUniqueId().toString()) < config.game.respawnLimit;
    }

    public static List<Player> getTeamMembers(Player player) {
        List<Player> members = new ArrayList<>();
        ScoreboardManager sm = Bukkit.getScoreboardManager();
        if (sm == null) return members;

        Set<Team> teams = sm.getMainScoreboard().getTeams();
        for (Team team : teams) {
            if (team.hasEntry(player.getName())) {
                for (String entry : team.getEntries()) {
                    Player p = Bukkit.getPlayer(entry);
                    if (p != null) members.add(p);
                }
                return members;
            }
        }

        return members;
    }
}
