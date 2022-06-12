package it.multicoredev.uhc.listeners;

import it.multicoredev.mbcore.spigot.chat.Chat;
import it.multicoredev.uhc.Game;
import it.multicoredev.uhc.events.UHCDeathEvent;
import it.multicoredev.uhc.events.UHCPermanentDeathEvent;
import it.multicoredev.uhc.storage.Config;
import it.multicoredev.uhc.storage.GameFile;
import it.multicoredev.uhc.utils.PlayerInventoryUpdateTask;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.Set;

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
public class PlayerDeathListener implements Listener {
    private final Plugin plugin;
    private final Config config;
    private final Game game;

    public PlayerDeathListener(Plugin plugin, Config config, Game game) {
        this.plugin = plugin;
        this.config = config;
        this.game = game;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!game.isRunning()) return;

        Player player = event.getEntity();

        UHCDeathEvent deathEvent = new UHCDeathEvent(game, player);
        Bukkit.getPluginManager().callEvent(deathEvent);

        if (deathEvent.isCancelled()) return;

        game.deathLocation.put(player, player.getLocation());

        if (config.game.deathSound != null && !config.game.deathSound.isEmpty()) {
            try {
                Sound sound = Sound.valueOf(config.game.deathSound);

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p.getLocation(), sound, 1, 1);
                }
            } catch (Exception ignored) {
                Chat.warning("&6Sound '" + config.game.deathSound + "' not found!");
            }
        }

        if (config.end.manualEnd) return;
        if (PlayerRespawnListener.canRespawn(player, game, config)) return;

        killPlayer(player);

        if (config.end.singleplayer) {
            if (game.currentGame().getAlivePlayers().size() == 1) game.endGame();
        } else {
            ScoreboardManager sm = Bukkit.getScoreboardManager();
            if (sm == null) return;

            Set<Team> teams = sm.getMainScoreboard().getTeams();
            int activeTeams = 0;

            for (Team team : teams) {
                if (teamContainsAlivePlayers(team)) activeTeams++;
            }

            if (activeTeams == 1) game.endGame();
        }
    }

    private boolean teamContainsAlivePlayers(Team team) {
        Set<String> players = team.getEntries();

        for (String name : players) {
            Player player = Bukkit.getPlayer(name);
            if (player == null) continue;
            if (game.currentGame().getAlivePlayers().contains(player.getUniqueId())) return true;
        }

        return false;
    }

    private void killPlayer(Player player) {
        UHCPermanentDeathEvent deathEvent = new UHCPermanentDeathEvent(game, player);
        Bukkit.getPluginManager().callEvent(deathEvent);

        if (deathEvent.isCancelled()) return;

        game.currentGame().killPlayer(player.getUniqueId());
        Bukkit.getScheduler().callSyncMethod(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamemode spectator " + player.getName())); //Using command as a workaround for the spigot bug of fake gm3

        PlayerInventoryUpdateTask updateTask = new PlayerInventoryUpdateTask(plugin, player);
        game.addInventoryUpdateTask(updateTask);

        Chat.send(config.messages.permaDeath, player);
        Chat.info("&c" + player.getName() + " died permanently");
    }
}
