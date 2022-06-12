package it.multicoredev.uhc.storage;

import com.google.gson.annotations.SerializedName;
import it.multicoredev.mclib.json.JsonConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
public class Config extends JsonConfig {
    public Start start;
    public Game game;
    public End end;
    public Messages messages;

    public void init() {
        if (start == null) {
            start = new Start();
            start.init();
        }
        if (game == null) {
            game = new Game();
            game.init();
        }
        if (end == null) {
            end = new End();
            end.init();
        }
        if (messages == null) {
            messages = new Messages();
            messages.init();
        }
    }

    public static class Start extends JsonConfig {
        @SerializedName("countdown_messages")
        public List<String> countdownMessages;
        @SerializedName("countdown_console_commands")
        public List<String> countdownConsoleCommands;
        @SerializedName("countdown_players_commands")
        public List<String> countdownPlayersCommands;
        public Integer countdown;
        @SerializedName("start_title")
        public String startTitle;
        @SerializedName("start_subtitle")
        public String startSubtitle;
        @SerializedName("start_messages")
        public List<String> startMessages;
        @SerializedName("start_console_commands")
        public List<String> startConsoleCommands;
        @SerializedName("start_players_commands")
        public List<String> startPlayersCommands;
        @SerializedName("restart_message")
        public List<String> restartMessages;
        @SerializedName("restart_commands")
        public List<String> restartCommands;
        @SerializedName("messages_delay")
        public Integer messagesDelay;
        @SerializedName("automatic_restart")
        public Boolean autoRestart;

        public void init() {
            if (countdownMessages == null) countdownMessages = new ArrayList<>(Collections.singletonList("&9The UHC will start soon... Get ready!"));
            if (countdownConsoleCommands == null) countdownConsoleCommands = new ArrayList<>();
            if (countdownPlayersCommands == null) countdownPlayersCommands = new ArrayList<>();
            if (countdown == null) countdown = 10;
            if (startTitle == null) startTitle = "#2196f3Multi#2962ffCore &fUHC";
            if (startSubtitle == null) startSubtitle = "#2196f3Starts now!";
            if (startMessages == null) startMessages = new ArrayList<>(Collections.singletonList("&cPay attention, if you die in the game you will die in real life!"));
            if (startConsoleCommands == null) startConsoleCommands = new ArrayList<>();
            if (startPlayersCommands == null) startPlayersCommands = new ArrayList<>();
            if (restartMessages == null) restartMessages = new ArrayList<>(Collections.singletonList("&eThe game is restarting..."));
            if (restartCommands == null) restartCommands = new ArrayList<>();
            if (messagesDelay == null) messagesDelay = 0;
            if (autoRestart == null) autoRestart = true;
        }
    }

    public static class Game {
        @SerializedName("uhc_duration")
        public Integer uhcDuration;
        @SerializedName("episode_duration")
        public Integer epDuration;
        @SerializedName("allow_respawn")
        public Boolean canRespawn;
        @SerializedName("respawn_time_limit")
        public Integer respawnTimeLimit;
        @SerializedName("respawn_to_team")
        public Boolean respawnToTeam;
        @SerializedName("respawn_to_death")
        public Boolean respawnToDeath;
        @SerializedName("respawn_limit")
        public Integer respawnLimit;
        @SerializedName("respawn_health")
        public Double respawnHealth;
        @SerializedName("respawn_command")
        public String respawnCommand;
        @SerializedName("deathmatch_messages")
        public List<String> deathmatchMessages;
        @SerializedName("deathmatch_console_command")
        public List<String> deathmatchConsoleCommands;
        @SerializedName("deathmatch_players_command")
        public List<String> deathmatchPlayersCommands;
        @SerializedName("use_bossbar")
        public Boolean useBossbar;
        @SerializedName("bossbar_day_night_cycle")
        public Boolean bossbarDayNightCycle;
        @SerializedName("use_timer")
        public Boolean useTimer;
        @SerializedName("messages_delay")
        public Integer messagesDelay;
        @SerializedName("death_sound")
        public String deathSound;
        @SerializedName("game_world")
        public String world;

        public void init() {
            if (uhcDuration == null) uhcDuration = 90;
            if (epDuration == null) epDuration = 0;
            if (canRespawn == null) canRespawn = true;
            if (respawnTimeLimit == null) respawnTimeLimit = 30;
            if (respawnToTeam == null) respawnToTeam = true;
            if (respawnToDeath == null) respawnToDeath = false;
            if (respawnLimit == null) respawnLimit = 1;
            if (respawnHealth == null) respawnHealth = 20.0;
            if (respawnCommand == null) respawnCommand = "";
            if (deathmatchMessages == null) deathmatchMessages = new ArrayList<>(Collections.singleton("&cThe deathmatch is starting now!"));
            if (deathmatchConsoleCommands == null) deathmatchConsoleCommands = new ArrayList<>();
            if (deathmatchPlayersCommands == null) deathmatchPlayersCommands = new ArrayList<>();
            if (useBossbar == null) useBossbar = true;
            if (bossbarDayNightCycle == null) bossbarDayNightCycle = true;
            if (useTimer == null) useTimer = true;
            if (messagesDelay == null) messagesDelay = 0;
            if (deathSound == null) deathSound = "ENTITY_LIGHTNING_BOLT_THUNDER";
            if (world == null) world = "world";
        }
    }

    public static class End extends JsonConfig {
        @SerializedName("manual_end")
        public Boolean manualEnd;
        public Boolean singleplayer;
        @SerializedName("end_messages")
        public List<String> endMessages;
        @SerializedName("end_console_command")
        public List<String> endConsoleCommands;
        @SerializedName("end_players_command")
        public List<String> endPlayersCommands;
        @SerializedName("end_title")
        public String endTitle;
        @SerializedName("end_subtitle")
        public String endSubtitle;
        @SerializedName("messages_delay")
        public Integer messagesDelay;

        public void init() {
            if (manualEnd == null) manualEnd = false;
            if (singleplayer == null) singleplayer = false;
            if (endMessages == null) endMessages = new ArrayList<>();
            if (endConsoleCommands == null) endConsoleCommands = new ArrayList<>();
            if (endPlayersCommands == null) endPlayersCommands = new ArrayList<>();
            if (endTitle == null) endTitle = "#2196f3Multi#2962ffCore &fUHC";
            if (endSubtitle == null) endSubtitle = "&cIs over!";
            if (messagesDelay == null) messagesDelay = 0;
        }
    }

    public static class Messages extends JsonConfig {
        @SerializedName("episode_end")
        public String episodeEnd;
        public String bossbar;
        public String stop;
        @SerializedName("insufficient_permissions")
        public String insufficientPerms;
        @SerializedName("game_running")
        public String gameRunning;
        @SerializedName("game_not_running")
        public String gameNotRunning;
        @SerializedName("nothing_to_restart")
        public String noRestart;
        @SerializedName("permanent_death")
        public String permaDeath;
        @SerializedName("player_respawn")
        public String playerRespawn;
        @SerializedName("player_not_respawned")
        public String playerNotRespawned;
        @SerializedName("respawn_period_end")
        public String respawnEnd;
        @SerializedName("player_not_found")
        public String notFound;

        public void init() {
            if (episodeEnd == null) episodeEnd = "&eEnd of the #FF6F00{n} &eepisode!";
            if (bossbar == null) bossbar = "&e{timer}";
            if (stop == null) stop = "&cThe game has been stopped";
            if (insufficientPerms == null) insufficientPerms = "&4Insufficient permissions!";
            if (gameRunning == null) gameRunning = "&cGame already running!";
            if (gameNotRunning == null) gameNotRunning = "&cGame not running!";
            if (noRestart == null) noRestart = "&cNothing to restart! Use /uhc start to start the game.";
            if (permaDeath == null) permaDeath = "#ff0000You are permanently dead! Thanks for playing.";
            if (playerRespawn == null) playerRespawn = "&e{player} respawned with {health} hearts!";
            if (playerNotRespawned == null) playerNotRespawned = "&cPlayer not respawned!";
            if (respawnEnd == null) respawnEnd = "&cFrom now on you can't respawn anymore!";
            if (notFound == null) notFound = "&cPlayer not found!";
        }
    }
}
