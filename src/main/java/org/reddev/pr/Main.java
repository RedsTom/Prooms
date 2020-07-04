package org.reddev.pr;

import com.google.common.collect.Lists;
import fr.il_totore.ucp.registration.CommandRegistry;
import fr.il_totore.ucp.registration.PrefixedCommandRegistry;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;
import org.json.simple.JSONArray;
import org.reddev.pr.register.CommandRegister;
import org.reddev.pr.register.EventRegistry;
import org.reddev.pr.register.LangReader;
import org.reddev.pr.register.LangRegisterer;
import org.reddev.pr.utils.i18n.I18n;
import org.reddev.pr.utils.sql.DatabaseManager;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class Main {

    public static int userPermission = 66061568;
    public static JSONArray langs;
    private static DiscordApi api;
    private static CommandRegistry<MessageCreateEvent> registry = new PrefixedCommandRegistry<>(Lists.newArrayList(), "%");
    private static DatabaseManager databaseManager;
    private static File langFile;
    public static String token;

    public static void main(String[] args) throws SQLException, IOException {

        databaseManager = new DatabaseManager();
        databaseManager.openConnection();
        langFile = new File(System.getProperty("user.dir"), "config.json");
        if (!(langFile.exists())) {
            langFile.createNewFile();
            new LangRegisterer(langFile);
        }

        try {
            new LangReader();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //API DEFINITION
        {
            api = new DiscordApiBuilder().setToken(token).login().join();
        }

        Main.getDatabaseManager().createTableIfNotExists("servers", "id INT(255) UNIQUE, createChannelId INT(255) UNIQUE, categoryId INT(255) UNIQUE, lang VARCHAR(255)");

        new CommandRegister(registry);
        new EventRegistry(api, registry);
    }

    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public static EmbedBuilder getErrorEmbed(String s, Server server) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(I18n.format(server.getId(), "message.error") + " : ")
                .setDescription(s)
                .setColor(Color.RED);
        return embed;
    }

    public static EmbedBuilder getSuccessEmbed(String title, String message) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title)
                .setDescription(message)
                .setColor(Color.GREEN);
        return embed;
    }

    public static File getLangFile() {
        return langFile;
    }

    public static CommandRegistry<MessageCreateEvent> getRegistry() {
        return registry;
    }
}
