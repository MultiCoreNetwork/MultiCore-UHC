package it.multicoredev.uhc.events;

import it.multicoredev.uhc.Game;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

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
public class UHCPreEndEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancel;
    private final Game game;
    private String title;
    private String subtitle;
    private List<String> messages;
    private List<String> consoleCommands;
    private List<String> playerCommands;

    /**
     * Called at the end of the game
     */
    public UHCPreEndEvent(Game game, String title, String subtitle, List<String> messages, List<String> consoleCommands, List<String> playerCommands) {
        super(true);
        this.game = game;
        this.title = title;
        this.subtitle = subtitle;
        this.messages = messages;
        this.consoleCommands = consoleCommands;
        this.playerCommands = playerCommands;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public Game getGame() {
        return game;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public List<String> getConsoleCommands() {
        return consoleCommands;
    }

    public void setConsoleCommands(List<String> consoleCommands) {
        this.consoleCommands = consoleCommands;
    }

    public List<String> getPlayerCommands() {
        return playerCommands;
    }

    public void setPlayerCommands(List<String> playerCommands) {
        this.playerCommands = playerCommands;
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    public void addConsoleCommand(String command) {
        consoleCommands.add(command);
    }

    public void addPlayerCommand(String command) {
        playerCommands.add(command);
    }

    public void clearConsoleCommands() {
        consoleCommands.clear();
    }

    public void clearPlayerCommands() {
        playerCommands.clear();
    }
}