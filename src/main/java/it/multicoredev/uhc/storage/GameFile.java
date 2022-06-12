package it.multicoredev.uhc.storage;

import com.google.gson.annotations.SerializedName;
import it.multicoredev.mbcore.spigot.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

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
public class GameFile {
    @SerializedName("alive_players")
    private Map<String, Integer> alivePlayers = new HashMap<>();
    @SerializedName("dead_players")
    private List<String> deadPlayers = new ArrayList<>();
    private long timer;

    public GameFile() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("uhc.player")) {
                alivePlayers.put(player.getUniqueId().toString(), 0);
                Chat.info("&2Adding player &a" + player.getName());
            }
        }

        timer = 0;
    }

    public Map<String, Integer> getPlayers() {
        return alivePlayers;
    }

    public List<UUID> getAlivePlayers() {
        List<UUID> uuids = new ArrayList<>();

        for (String uuid : alivePlayers.keySet()) {
            uuids.add(UUID.fromString(uuid));
        }

        return uuids;
    }

    public List<UUID> getDeadPlayers() {
        List<UUID> uuids = new ArrayList<>();

        for (String uuid : deadPlayers) {
            uuids.add(UUID.fromString(uuid));
        }

        return uuids;
    }

    public List<UUID> getRespawnedPlayers() {
        List<UUID> uuids = new ArrayList<>();

        for (String uuid : alivePlayers.keySet()) {
            if (alivePlayers.get(uuid) > 0) uuids.add(UUID.fromString(uuid));
        }

        return uuids;
    }

    public long getTimer() {
        return timer;
    }

    public void incrementTimer() {
        timer++;
    }

    public boolean respawnPlayer(UUID uuid, boolean force) {
        String id = uuid.toString();
        if (deadPlayers.contains(id) && !force) return false;
        if (alivePlayers.containsKey(id)) {
            alivePlayers.put(id, alivePlayers.get(id) + 1);
            return true;
        }

        if (force) {
            alivePlayers.put(id, 1);
            return true;
        }

        return false;
    }

    public void killPlayer(UUID uuid) {
        String id = uuid.toString();
        if (!alivePlayers.containsKey(id)) return;
        alivePlayers.remove(id);
        if (!deadPlayers.contains(id)) deadPlayers.add(id);
    }
}
