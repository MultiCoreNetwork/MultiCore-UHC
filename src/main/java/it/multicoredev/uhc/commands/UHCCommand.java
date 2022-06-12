package it.multicoredev.uhc.commands;

import it.multicoredev.mbcore.spigot.chat.Chat;
import it.multicoredev.mbcore.spigot.util.TabCompleterUtil;
import it.multicoredev.uhc.Game;
import it.multicoredev.uhc.listeners.PlayerRespawnListener;
import it.multicoredev.uhc.storage.Config;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Copyright © 2020 by Lorenzo Magni
 * This file is part of MultiCore-UHC.
 * MultiCore-UHC is under "The 3-Clause BSD License", you can find a copy <a href="https://opensource.org/licenses/BSD-3-Clause">here</a>.
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
public class UHCCommand implements CommandExecutor, TabCompleter {
    private final Config config;
    private final Game game;

    public UHCCommand(Config config, Game game) {
        this.config = config;
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("uhc.manage")) {
            Chat.send(config.messages.insufficientPerms, sender);
            return true;
        }

        if (args.length == 0) {
            Chat.send("&e/uhc <start|stop|restart|end|list|respawn>", sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {
            if (game.isRunning()) {
                Chat.send(config.messages.gameRunning, sender);
                return true;
            }

            game.start();
        } else if (args[0].equalsIgnoreCase("stop")) {
            if (!game.isRunning()) {
                Chat.send(config.messages.gameNotRunning, sender);
                return true;
            }

            game.stop();
        } else if (args[0].equalsIgnoreCase("restart")) {
            if (!game.isWaiting()) {
                Chat.send(config.messages.noRestart, sender);
                return true;
            }

            game.forceRestart();
        } else if (args[0].equalsIgnoreCase("end")) {
            if (game.isRunning()) {
                Chat.send(config.messages.gameRunning, sender);
                return true;
            }

            game.endGame();
        } else if (args[0].equalsIgnoreCase("list")) {
            Chat.send("&2Alive players: &b" + getPlayers(game.currentGame().getAlivePlayers()), sender); //TODO Move to config
            Chat.send("&eRespawned players: &b" + getPlayers(game.currentGame().getRespawnedPlayers()), sender); //TODO Move to config
            Chat.send("&cDead players: &b" + getPlayers(game.currentGame().getDeadPlayers()), sender); //TODO Move to config
        } else if (args[0].equalsIgnoreCase("respawn")) {
            Player player = Bukkit.getPlayer(args[1]);

            if (player == null) {
                Chat.send(config.messages.notFound, sender);
                return true;
            }

            if (game.currentGame().respawnPlayer(player.getUniqueId(), true)) {
                if (config.game.respawnToDeath) {
                    player.teleport(game.deathLocation.get(player));
                } else {
                    if (config.game.respawnToTeam) {
                        List<Player> members = PlayerRespawnListener.getTeamMembers(player); //TODO To check
                        if (!members.isEmpty()) player.teleport(members.get(0).getLocation());
                    }
                }

                player.setGameMode(GameMode.SURVIVAL);
                game.removeInventoryUpdateTask(player);
                player.setHealth(config.game.respawnHealth);

                Chat.broadcast(config.messages.playerRespawn
                        .replace("{player}", player.getDisplayName())
                        .replace("{health}", String.valueOf(config.game.respawnHealth)));

            } else {
                Chat.send(config.messages.playerNotRespawned, sender);
            }
        } else {
            Chat.send("&e/uhc <start|stop|restart|end|list|respawn>", sender);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("uhc.manage")) return new ArrayList<>();

        return TabCompleterUtil.getCompletions(args[0], "start", "stop", "restart", "end", "respawn", "list");
    }

    public String getPlayers(List<UUID> uuids) {
        String[] names = new String[uuids.size()];
        for (int i = 0; i < uuids.size(); i++) {
            Player player = Bukkit.getPlayer(uuids.get(i));
            if (player == null) names[i] = uuids.get(i).toString();
            else names[i] = player.getName();
        }

        return Arrays.toString(names);
    }
}
