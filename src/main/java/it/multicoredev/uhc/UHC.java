package it.multicoredev.uhc;

import it.multicoredev.mbcore.spigot.chat.Chat;
import it.multicoredev.mclib.json.GsonHelper;
import it.multicoredev.uhc.commands.UHCCommand;
import it.multicoredev.uhc.listeners.PlayerDeathListener;
import it.multicoredev.uhc.listeners.PlayerJoinListener;
import it.multicoredev.uhc.listeners.PlayerRespawnListener;
import it.multicoredev.uhc.storage.Config;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

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
public class UHC extends JavaPlugin {
    public static final GsonHelper GSON = new GsonHelper();
    private Config config;
    private Game game;
    private static UHC instance;

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists() || !getDataFolder().isDirectory()) {
            if (!getDataFolder().mkdir()) {
                onDisable();
                return;
            }
        }

        try {
            config = new Config();
            config.init();

            if (!getDataFolder().exists() || !getDataFolder().isDirectory()) {
                if (!getDataFolder().mkdir()) {
                    Chat.severe("&cFailed to create data folder!");
                    onDisable();
                    return;
                }
            }

            config = GSON.autoload(new File(getDataFolder(), "config.json"), config, Config.class);
        } catch (IOException e) {
            e.printStackTrace();
            onDisable();
            return;
        }

        game = new Game(this, config);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(game), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this, config, game), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnListener(this, config, game), this);

        UHCCommand cmd = new UHCCommand(config, game);
        getCommand("uhc").setExecutor(cmd);
        getCommand("uhc").setTabCompleter(cmd);
    }

    @Override
    public void onDisable() {
        if (game != null) game.deleteGameFile();
    }

    public static UHC getInstance() {
        return instance;
    }

    public Game getGame() {
        return game;
    }
}
