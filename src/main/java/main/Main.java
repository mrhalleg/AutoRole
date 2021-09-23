package main;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        // https://discordapp.com/api/oauth2/authorize?client_id=565293748309983252&permissions=36777024&scope=bot

        if (args.length < 1) {
            System.out.println("Requeired arguments: Discord-Bot-Secret");
            System.exit(1);
        }

        Collection<GatewayIntent> intents = new ArrayList<>();
        intents.add(GatewayIntent.GUILD_MEMBERS);
        intents.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
        intents.add(GatewayIntent.GUILD_MESSAGES);
        intents.add(GatewayIntent.GUILD_VOICE_STATES);
        JDA jda = JDABuilder.create(intents).setToken(args[0])
                            .setChunkingFilter(ChunkingFilter.ALL)
                            .setMemberCachePolicy(MemberCachePolicy.ALL)
                            .setMaxReconnectDelay(60)
                            .build()
                            .awaitReady();

        AutoBot bot = new AutoBot(jda);

        jda.addEventListener(bot);


        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                bot.checkDemotions();
            }
        }, new Date(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
    }
}
