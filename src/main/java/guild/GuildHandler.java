package guild;

import main.AutoBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class GuildHandler {
    private AutoBot bot;
    private GuildConfig config;
    private Guild guild;

    public GuildHandler(AutoBot bot, Guild guild) {
        this.bot = bot;
        this.guild = guild;

        this.config = new GuildConfig(guild.getIdLong());

        setOutputChannelId(guild.getTextChannels().get(0).getIdLong());
        setRoleId(guild.getRoles().get(guild.getRoles().size() - 2).getIdLong());
        setPromotionChannelId(guild.getVoiceChannels().get(0).getIdLong());
        addUnmanaged();
        removeUnnecessary();
    }

    public GuildHandler(AutoBot bot, GuildConfig config, Guild guild) {
        this.bot = bot;
        this.config = config;
        this.guild = guild;
        printSettings();
        addUnmanaged();
        removeUnnecessary();
    }

    public void addUnmanaged() {
        log("Adding Unmanaged...");
        Role role = getRole();
        for (Member member : this.guild.getMembersWithRoles(role)) {
            if (getLastActiveDate(member.getIdLong()) == null) {
                log("Adding " + member.getEffectiveName());
                this.config.updateDate(member.getIdLong());
            }
        }
        this.bot.saveConfig(this.config);
    }

    public void removeUnnecessary() {
        log("Removing Unnecessary...");
        List<Long> toRemove = new LinkedList<>();
        for (Long id : this.config.getLastActive().keySet()) {
            if (!getMember(id).getRoles().contains(getRole())) {
                log("Removing " + getMember(id).getEffectiveName());
                toRemove.add(id);
            }
        }

        for (Long id : toRemove) {
            this.config.getLastActive().remove(id);
        }
        this.bot.saveConfig(this.config);
    }

    public void isActive(Member member) {
        if (!member.getRoles().contains(getRole())) {
            return;
        }
        log(member.getEffectiveName() + " was active");
        this.config.updateDate(member.getIdLong());
        this.bot.saveConfig(this.config);
    }

    public void checkDemotions() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MONTH, -1);
        Date cutoff = cal.getTime();
        String message = "\n";
        message += "Daily Report " + new Date() + "\n";
        message += padRight("Member", 30);
        message += padRight("Days left", 15);
        message += "Last Active";
        message += "\n";

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Daily Report " + new Date());
        eb.addField(padRight("Member", 40) + ".", "", true);
        eb.addField("Days left", "", true);
        eb.addField("Last Active", "", true);

        String s1 = null;
        String s2 = null;
        String s3 = null;

        for (long id : this.config.getLastActive().keySet()) {
            Date lastActive = getLastActiveDate(id);
            long kickedIn = ChronoUnit.DAYS.between(
                    cutoff.toInstant()
                          .atZone(ZoneId.systemDefault())
                          .toLocalDate(),
                    lastActive.toInstant()
                              .atZone(ZoneId.systemDefault())
                              .toLocalDate());

            message += padRight(getMember(id).getEffectiveName(), 30);
            message += padRight(kickedIn + "", 15);
            message += lastActive;
            message += "\n";

            if (s1 == null) {
                s1 = getMember(id).getEffectiveName();
                s2 = kickedIn + "";
                s3 = lastActive + "";
            } else {
                eb.addField(getMember(id).getEffectiveName(),
                        s1, true);
                eb.addField(kickedIn + "", s2, true);
                eb.addField(lastActive + "", s3, true);

                s1 = null;
                s2 = null;
                s2 = null;
            }

            if (eb.getFields().size() >= 22) {
                getOutputChannel().sendMessageEmbeds(eb.build()).queue();
                eb = new EmbedBuilder();
            }

            if (getLastActiveDate(id).before(cutoff)) {
                demote(getMember(id));
            }
        }


        log(message);
        if (eb.getFields().size() > 0) {
            getOutputChannel().sendMessageEmbeds(eb.build()).queue();
        }
    }

    public static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    private Date getLastActiveDate(long id) {
        return this.config.getLastActive().get(id);
    }

    private Role getRole() {
        return this.guild.getRoleById(this.config.getRoleId());
    }

    private Member getMember(long id) {
        return this.guild.getMemberById(id);
    }

    private VoiceChannel getPromotionChannel() {
        return this.guild.getVoiceChannelById(this.config.getPromotionChannelId());
    }

    private TextChannel getOutputChannel() {
        return this.guild.getTextChannelById(this.config.getOutputChannel());
    }

    public void joinedVoice(Member member, VoiceChannel channel) {
        if (channel.getIdLong() == this.config.getPromotionChannelId()) {
            promote(member);
        } else {
            isActive(member);
        }
    }

    private void demote(Member member) {
        this.guild.removeRoleFromMember(member, getRole()).queue();
        log("Demoted " + member.getEffectiveName());
    }

    private void promote(Member member) {
        this.guild.addRoleToMember(member, getRole()).queue();
        log("Promoted " + member.getEffectiveName());
    }

    public void log(String message) {
        System.out.println(new Date() + " [" + this.guild.getName() + "] " + message);
    }

    public void sendMessage(String message) {
        String sub = "";
        for (String s : message.split("\\n")) {
            if (sub.length() + s.length() > 2000) {
                this.guild.getTextChannelById(this.config.getOutputChannel()).sendMessage(sub).queue();
                sub = s + "\n";
            } else {
                sub += s + "\n";
            }
        }
        this.guild.getTextChannelById(this.config.getOutputChannel()).sendMessage(sub).queue();
    }

    private void printSettings() {
        log("promotion-channel: " + getPromotionChannel().getName());
        sendMessage("promotion-channel: " + getPromotionChannel().getAsMention());

        log("role: " + getRole().getName());
        sendMessage("role: " + getRole().getAsMention());

        log("output: " + getOutputChannel().getName());
        sendMessage("output: " + getOutputChannel().getAsMention());
    }

    private void setPromotionChannelId(long id) {
        this.config.setPromotionChannelId(id);
        log("Set PromotionChannel to " + this.guild.getVoiceChannelById(id).getName());
        sendMessage("Set PromotionChannel to " + this.guild.getVoiceChannelById(id).getAsMention());
        this.bot.saveConfig(this.config);
    }

    private void setRoleId(long id) {
        this.config.setRoleId(id);
        log("Set role to " + this.guild.getRoleById(id).getName());
        sendMessage("Set role to " + this.guild.getRoleById(id).getAsMention());
        this.bot.saveConfig(this.config);
    }

    private void setOutputChannelId(long id) {
        this.config.setOutputChannel(id);
        log("Set role to " + this.guild.getTextChannelById(id).getName());
        sendMessage("Set role to " + this.guild.getTextChannelById(id).getAsMention());
        this.bot.saveConfig(this.config);
    }
}
