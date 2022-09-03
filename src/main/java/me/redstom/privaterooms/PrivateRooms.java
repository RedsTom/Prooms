/*
 * PrivateRooms is a discord bot to manage vocal chats.
 * Copyright (C) 2022 GravenDev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.redstom.privaterooms;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.redstom.privaterooms.util.command.CommandExecutor;
import me.redstom.privaterooms.util.command.CommandExecutorRepr;
import me.redstom.privaterooms.util.command.ICommand;
import me.redstom.privaterooms.util.command.RegisterCommand;
import me.redstom.privaterooms.util.events.RegisterListener;
import net.dv8tion.jda.api.JDA;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PrivateRooms {

    private final ApplicationContext ctx;
    private final JDA client;
    private final List<ICommand> commands;
    private final Map<String, CommandExecutorRepr> commandExecutors;

    @SneakyThrows
    public void run() {
        this.client.addEventListener(ctx.getBeansWithAnnotation(RegisterListener.class).values().toArray(Object[]::new));

        List<ICommand> iCommands = ctx.getBeansWithAnnotation(RegisterCommand.class).values().stream()
          .filter(a -> a instanceof ICommand)
          .map(a -> (ICommand) a)
          .toList();

        Map<String, CommandExecutorRepr> executorMap = iCommands.stream()
          .flatMap(a -> Arrays.stream(a.getClass().getMethods())
            .map(m -> new CommandExecutorRepr(a, m)))
          .filter(c -> c.method().getAnnotation(CommandExecutor.class) != null)
          .collect(Collectors.toMap(c -> c.method().getAnnotation(CommandExecutor.class).value(), c -> c));

        commands.addAll(iCommands);
        commandExecutors.putAll(executorMap);
        client.updateCommands()
          .addCommands(iCommands.stream()
            .map(ICommand::command)
            .toList())
          .queue();
    }
}
