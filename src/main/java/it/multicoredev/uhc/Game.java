package it.multicoredev.uhc;

import it.multicoredev.mbcore.spigot.chat.Chat;
import it.multicoredev.uhc.events.*;
import it.multicoredev.uhc.storage.Config;
import it.multicoredev.uhc.storage.GameFile;
import it.multicoredev.uhc.utils.PlayerInventoryUpdateTask;
import it.multicoredev.uhc.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
public class Game {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(6);
    private final BukkitScheduler bukkitScheduler;

    private ScheduledFuture<?> containerTask;
    private ScheduledFuture<?> countdownTask;
    private ScheduledFuture<?> gameTask;
    private ScheduledFuture<?> deathmatchTask;

    private final Plugin plugin;
    private final Config config;
    private GameFile game;

    private BossBar timebar;
    private boolean running = false;
    private boolean waiting = false;

    private boolean forceRestart = false; //TODO ???
    public Map<Player, Location> deathLocation = new HashMap<>();
    public List<PlayerInventoryUpdateTask> inventoryUpdateTasks = new ArrayList<>();

    public Game(Plugin plugin, Config config) {
        this.plugin = plugin;
        this.config = config;

        bukkitScheduler = Bukkit.getScheduler();

        File gameFile = new File(plugin.getDataFolder(), "game.json");
        if (gameFile.exists() && gameFile.isFile()) {
            try {
                game = UHC.GSON.load(gameFile, GameFile.class);
            } catch (Exception e) {
                Chat.severe("&cFailed to load game.json. &r" + e.getMessage());
                plugin.onDisable();
                return;
            }

            restart();
        }
    }

    public GameFile currentGame() {
        return game;
    }

    public void start() {
        running = true;
        containerTask = scheduler.schedule(this::countdown, 0, TimeUnit.MILLISECONDS);
    }

    public void restart() {
        bukkitScheduler.runTaskAsynchronously(plugin, () -> {
            while (!forceRestart || !allAliveAreOnline()) waiting = true;

            boolean hasDelay = config.start.messagesDelay > 0;
            long delay = config.start.messagesDelay * 1000;

            for (String msg : config.start.restartMessages) {
                Chat.broadcast(msg);
                if (hasDelay) sleep(delay);
            }

            for (String cmd : config.start.restartCommands) {
                bukkitScheduler.callSyncMethod(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
            }

            run();
        });
    }

    public void stop() {
        if (containerTask != null) {
            if (!containerTask.isCancelled() && !containerTask.isDone()) containerTask.cancel(true);
        }

        running = false;
        deleteGameFile();
        unregisterTimebarPlayers();
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public long getTimer() {
        return game.getTimer();
    }

    public void registerTimebar(Player player) {
        timebar.addPlayer(player);
    }

    public void unregisterTimebarPlayers() {
        for (Player player : timebar.getPlayers()) {
            timebar.removePlayer(player);
        }
    }

    public void forceRestart() {
        forceRestart = true;
    }

    private void countdown() {
        UHCCountdownStartEvent countdownStartEvent = new UHCCountdownStartEvent(
                Game.this,
                config.start.countdown,
                config.start.countdownMessages,
                config.start.countdownConsoleCommands,
                config.start.countdownConsoleCommands
        );
        Bukkit.getPluginManager().callEvent(countdownStartEvent);

        if (countdownStartEvent.isCancelled()) return;

        if (countdownStartEvent.getTime() <= 0) {
            startGame();
            return;
        }

        boolean hasDelay = config.start.messagesDelay > 0;
        long delay = config.start.messagesDelay * 1000;

        for (String msg : countdownStartEvent.getMessages()) {
            Chat.broadcast(msg);
            if (hasDelay) sleep(delay);
        }

        for (String cmd : countdownStartEvent.getConsoleCommands()) {
            bukkitScheduler.callSyncMethod(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
        }

        for (String cmd : countdownStartEvent.getPlayerCommands()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("uhc.player")) continue;
                bukkitScheduler.callSyncMethod(plugin, () -> Bukkit.dispatchCommand(player, cmd.replace("{player}", player.getName())));
            }
        }

        final int[] countdownTime = {countdownStartEvent.getTime()};
        countdownTask = scheduler.scheduleAtFixedRate(() -> {
            broadcastTitle("#2196f3" + countdownTime[0], "", 0, 20, 0); //TODO Move this to config

            if (countdownTime[0] == 0) {
                countdownTask.cancel(false); //TODO Check this
                startGame();
            }

            countdownTime[0]--;
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void startGame() {
        UHCPreStartEvent preStartEvent = new UHCPreStartEvent(
                Game.this,
                config.start.startTitle,
                config.start.startSubtitle,
                config.start.startMessages,
                config.start.startConsoleCommands,
                config.start.startPlayersCommands
        );
        Bukkit.getPluginManager().callEvent(preStartEvent);

        if (preStartEvent.isCancelled()) return;

        if (preStartEvent.getTitle() != null && !preStartEvent.getTitle().isEmpty()) {
            broadcastTitle(preStartEvent.getTitle(), preStartEvent.getSubtitle(), 10, 50, 10);
        }

        boolean hasDelay = config.start.messagesDelay > 0;
        long delay = config.start.messagesDelay * 1000;

        for (String msg : preStartEvent.getMessages()) {
            Chat.broadcast(msg);
            if (hasDelay) sleep(delay);
        }

        game = new GameFile();
        saveGame();

        for (String cmd : preStartEvent.getConsoleCommands()) {
            bukkitScheduler.callSyncMethod(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
        }

        for (String cmd : preStartEvent.getPlayerCommands()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("uhc.player")) continue;
                bukkitScheduler.callSyncMethod(plugin, () -> Bukkit.dispatchCommand(player, cmd.replace("{player}", player.getName())));
            }
        }

        UHCPostStartEvent postStartEvent = new UHCPostStartEvent(Game.this);
        Bukkit.getPluginManager().callEvent(postStartEvent);
        if (postStartEvent.isCancelled()) return;

        run();
    }

    private void run() {
        boolean splitEpisodes = config.game.epDuration > 0;
        long epDuration = config.game.epDuration * 60;
        long uhcDuration = config.game.uhcDuration * 60;

        if (config.game.useBossbar) {
            timebar = Bukkit.createBossBar(
                    Chat.toLegacyText(Chat.getTranslated(config.messages.bossbar.replace("{timer}",
                            TimeUtils.getClock(config.game.useTimer ? game.getTimer() : uhcDuration - game.getTimer())))),
                    getTimebarColor(),
                    BarStyle.SOLID);
            timebar.setProgress(1);

            for (Player player : Bukkit.getOnlinePlayers()) {
                registerTimebar(player);
            }
        }

        gameTask = scheduler.scheduleAtFixedRate(() -> {
            long timer = incrementTimer();

            if (timer >= uhcDuration) {
                gameTask.cancel(false); //TODO Check this
                startDeathmatch();
                return;
            }

            if (splitEpisodes) {
                int ep = TimeUtils.endEpisode(timer, epDuration);
                if (ep != -1) {
                    Chat.broadcast(config.messages.episodeEnd.replace("{n}", String.valueOf(ep)));

                    UHCEpisodeEndEvent episodeEndEvent = new UHCEpisodeEndEvent(Game.this, ep);
                    Bukkit.getPluginManager().callEvent(episodeEndEvent);
                }
            }

            if (config.game.canRespawn && timer == (config.game.respawnTimeLimit * 60)) {
                Chat.broadcast(config.messages.respawnEnd);

                UHCRespawnEndEvent endEvent = new UHCRespawnEndEvent(Game.this);
                Bukkit.getPluginManager().callEvent(endEvent);
            }

            if (timebar != null) updateTimebar((double) timer / uhcDuration, getTimebarColor());
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void startDeathmatch() {
        UHCDeathmatchStartEvent deathmatchStartEvent = new UHCDeathmatchStartEvent(
                Game.this,
                config.game.deathmatchMessages,
                config.game.deathmatchConsoleCommands,
                config.game.deathmatchPlayersCommands
        );
        Bukkit.getPluginManager().callEvent(deathmatchStartEvent);

        if (deathmatchStartEvent.isCancelled()) return;

        if (config.game.useBossbar) updateTimebar(1, BarColor.RED);

        boolean hasDelay = config.game.messagesDelay > 0;
        long delay = config.game.messagesDelay * 1000;

        for (String msg : deathmatchStartEvent.getMessages()) {
            Chat.broadcast(msg);
            if (hasDelay) sleep(delay);
        }

        for (String cmd : deathmatchStartEvent.getConsoleCommands()) {
            bukkitScheduler.callSyncMethod(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
        }

        for (String cmd : deathmatchStartEvent.getPlayerCommands()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("uhc.player")) continue;
                bukkitScheduler.callSyncMethod(plugin, () -> Bukkit.dispatchCommand(player, cmd.replace("{player}", player.getName())));
            }
        }

        deathmatchTask = scheduler.scheduleAtFixedRate(this::incrementTimer, 1, 1, TimeUnit.SECONDS);
    }

    public void endGame() {
        scheduler.schedule(() -> {
            UHCPreEndEvent preEndEvent = new UHCPreEndEvent(
                    Game.this,
                    config.end.endTitle,
                    config.end.endSubtitle,
                    config.end.endMessages,
                    config.end.endConsoleCommands,
                    config.end.endPlayersCommands
            );
            Bukkit.getPluginManager().callEvent(preEndEvent);

            if (preEndEvent.isCancelled()) return;

            if (gameTask != null) {
                if (!gameTask.isCancelled() && !gameTask.isDone()) gameTask.cancel(true);
                gameTask = null;
            }

            if (deathmatchTask != null) {
                if (!deathmatchTask.isCancelled() && !deathmatchTask.isDone()) deathmatchTask.cancel(true);
                deathmatchTask = null;
            }

            if (countdownTask != null) {
                if (!countdownTask.isCancelled() && !countdownTask.isDone()) countdownTask.cancel(true);
                countdownTask = null;
            }

            for (PlayerInventoryUpdateTask task : inventoryUpdateTasks) task.stop();
            inventoryUpdateTasks.clear();

            timebar.removeAll();

            if (preEndEvent.getTitle() != null && !preEndEvent.getTitle().isEmpty()) {
                broadcastTitle(preEndEvent.getTitle(), preEndEvent.getSubtitle(), 10, 50, 10);
            }

            boolean hasDelay = config.end.messagesDelay > 0;
            long delay = config.end.messagesDelay * 1000;

            for (String msg : preEndEvent.getMessages()) {
                Chat.broadcast(msg);
                if (hasDelay) sleep(delay);
            }

            for (String cmd : preEndEvent.getConsoleCommands()) {
                bukkitScheduler.callSyncMethod(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
            }

            for (String cmd : preEndEvent.getPlayerCommands()) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.hasPermission("uhc.player")) continue;
                    bukkitScheduler.callSyncMethod(plugin, () -> Bukkit.dispatchCommand(player, cmd.replace("{player}", player.getName())));
                }
            }

            game = null;
            deleteGameFile();

            UHCPostEndEvent postEndEvent = new UHCPostEndEvent(Game.this);
            Bukkit.getPluginManager().callEvent(postEndEvent);

            running = false;
        }, 0, TimeUnit.MILLISECONDS);
    }

    public void addInventoryUpdateTask(PlayerInventoryUpdateTask inventoryUpdateTask) {
        inventoryUpdateTasks.add(inventoryUpdateTask);
        inventoryUpdateTask.start();
    }

    public void removeInventoryUpdateTask(Player player) {
        PlayerInventoryUpdateTask task = getInventoryUpdateTask(player);
        if (task == null) return;

        inventoryUpdateTasks.remove(task);
        task.stop();
    }

    private PlayerInventoryUpdateTask getInventoryUpdateTask(Player player) {
        for (PlayerInventoryUpdateTask task : inventoryUpdateTasks) {
            if (task.getPlayer().getUniqueId().equals(player.getUniqueId())) return task;
        }

        return null;
    }

    private void saveGame() {
        UHC.GSON.saveAsync(game, new File(plugin.getDataFolder(), "game.json"));
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

    private void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(Chat.toLegacyText(Chat.getTranslated(title)), Chat.toLegacyText(Chat.getTranslated(subtitle)), fadeIn, stay, fadeOut);
        }
    }

    private long incrementTimer() {
        game.incrementTimer();
        saveGame();
        return game.getTimer();
    }

    private BarColor getTimebarColor() {
        if (!config.game.bossbarDayNightCycle) return BarColor.GREEN;

        World world = Bukkit.getWorld(config.game.world);
        if (world == null) return BarColor.YELLOW;

        if (world.getTime() < 12542) return BarColor.YELLOW;
        return BarColor.BLUE;
    }

    private void updateTimebar(double progress, BarColor color) {
        timebar.setProgress(progress);
        timebar.setTitle(Chat.toLegacyText(Chat.getTranslated(config.messages.bossbar.replace(
                "{timer}",
                TimeUtils.getClock(config.game.useTimer ?
                        game.getTimer() :
                        config.game.uhcDuration - game.getTimer())))));
        timebar.setColor(color);
    }

    private boolean allAliveAreOnline() {
        for (UUID uuid : game.getAlivePlayers()) {
            if (Bukkit.getPlayer(uuid) == null) return false;
        }

        return true;
    }

    void deleteGameFile() {
        File gameFile = new File(plugin.getDataFolder(), "game.json");
        if (gameFile.exists()) gameFile.delete();
    }
}
