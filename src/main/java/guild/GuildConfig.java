package guild;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GuildConfig {
    private long guildId;
    private long roleId;
    private long promotionChannelId;
    private long outputChannel;
    private Map<Long, Date> lastActive;

    public GuildConfig() {
    }

    public GuildConfig(long guildId) {
        this.guildId = guildId;
        this.lastActive = new HashMap<>();
    }

    public void setOutputChannel(long outputChannel) {
        this.outputChannel = outputChannel;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public void setPromotionChannelId(long promotionChannelId) {
        this.promotionChannelId = promotionChannelId;
    }

    public long getGuildId() {
        return this.guildId;
    }

    public void updateDate(long idLong) {
        this.lastActive.put(idLong, new Date());
    }

    public void updateDate(long idLong, Date date) {
        this.lastActive.put(idLong, date);
    }

    public Map<Long, Date> getLastActive() {
        return this.lastActive;
    }

    public long getRoleId() {
        return this.roleId;
    }

    public long getPromotionChannelId() {
        return this.promotionChannelId;
    }

    public long getOutputChannel() {
        return this.outputChannel;
    }
}
