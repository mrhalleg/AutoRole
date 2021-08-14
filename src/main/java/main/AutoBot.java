package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import guild.GuildConfig;
import guild.GuildHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AutoBot extends ListenerAdapter {
    private JDA jda;
    private Map<Long, GuildHandler> guilds;
    private ObjectMapper mapper;

    public AutoBot(JDA jda) {
        this.jda = jda;
        this.guilds = new HashMap<>();

        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);

        for (Guild g : this.jda.getGuilds()) {
            File file = new File(getFilename(g.getIdLong()));
            GuildHandler handler = null;

            System.out.println("initializing guild " + g.getName() + " (" + g.getIdLong() + ")");
            try {
                handler = new GuildHandler(this, loadConfig(file), g);
            } catch (Exception e) {
                System.out.println("failed to load, using default");
                handler = new GuildHandler(this, g);
            }
            this.guilds.put(g.getIdLong(), handler);
        }
    }

    public void checkDemotions() {
        System.out.println("Checking Demotions...");
        for (GuildHandler handler : this.guilds.values()) {
            handler.checkDemotions();
        }
    }

    private void isActive(Member member) {
        if (member.getUser().isBot()) {
            return;
        }
        System.out.println(member);
        this.guilds.get(member.getGuild().getIdLong()).isActive(member);
    }

    public GuildConfig loadConfig(File file) throws IOException {
        System.out.println("loading file " + file.getAbsolutePath());
        return this.mapper.readValue(file, GuildConfig.class);

    }

    public String getFilename(long l) {
        return l + ".config";
    }

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        this.guilds.get(event.getGuild().getIdLong()).addUnmanaged();
    }

    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        this.guilds.get(event.getGuild().getIdLong()).removeUnnecessary();
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        isActive(event.getMember());
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        isActive(event.getMember());
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        isActive(event.getMember());
    }

    @Override
    public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
        isActive(event.getMember());
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        GuildHandler handler = this.guilds.get(event.getGuild().getIdLong());
        if (handler != null) {
            handler.joinedVoice(event.getMember(), event.getVoiceState().getChannel());
        }
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        GuildHandler handler = this.guilds.get(event.getGuild().getIdLong());
        if (handler != null) {
            handler.joinedVoice(event.getMember(), event.getVoiceState().getChannel());
        }
    }

    public void saveConfig(GuildConfig config) {
        try {
            File file = new File(getFilename(config.getGuildId()));
            this.mapper.writeValue(file, config);
            System.out.println("saved config file " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
