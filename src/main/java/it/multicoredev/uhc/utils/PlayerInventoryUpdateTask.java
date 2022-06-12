package it.multicoredev.uhc.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

/**
 * Copyright © 2020 by Lorenzo Magni
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
public class PlayerInventoryUpdateTask {
    private final Plugin plugin;
    private final Player player;
    private final PlayerInventory inventory;
    private BukkitTask task = null;

    public PlayerInventoryUpdateTask(Plugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = player.getInventory();
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Player target = getNearestPlayer();
            if (target == null) return;

            player.getInventory().clear();
            player.getInventory().setContents(target.getInventory().getContents());
        }, 0, 20);
    }

    public void stop() {
        if (task != null) task.cancel();
        player.getInventory().clear();
        player.getInventory().setContents(inventory.getContents());
    }

    private Player getNearestPlayer() {
        List<Entity> entities = player.getNearbyEntities(5, 5, 5);

        for (Entity entity : entities) {
            if (entity instanceof Player) return (Player) entity;
        }

        return null;
    }

    public Player getPlayer() {
        return player;
    }
}
