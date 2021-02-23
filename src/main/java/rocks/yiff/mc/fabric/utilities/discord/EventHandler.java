package rocks.yiff.mc.fabric.utilities.discord;

import club.minnced.discord.webhook.external.JDAWebhookClient;
import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import rocks.yiff.mc.fabric.utilities.Config;
import rocks.yiff.mc.fabric.utilities.Main;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

public class EventHandler extends ListenerAdapter {
    @Override
    public void onReady(@Nonnull ReadyEvent ev) {
        Main.getLogger().info(String.format("JDA is ready as %s with %s servers.", ev.getJDA().getSelfUser().getAsTag(), ev.getGuildTotalCount()));
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent ev) {
        Message msg = ev.getMessage();
        MessageChannel ch = msg.getChannel();
        User author = msg.getAuthor();
        if(!Client.getConfig().hasChannel(ev.getChannel().getId()) || author.isBot() || msg.isWebhookMessage()) return;
        String[] o = Client.getConfig().getOtherChannels(ch.getId());
        assert o != null;
        for(String c : o) {
            WebhookMessageBuilder m = new WebhookMessageBuilder()
                    .setUsername(author.getName())
                    .setAvatarUrl(author.getAvatarUrl())
                    .setContent(msg.getContentRaw())
                    .setAllowedMentions(AllowedMentions.none().withParseUsers(true));
            if(msg.getAttachments().size() > 0) {
                StringBuilder b = new StringBuilder();
                for(Message.Attachment a : msg.getAttachments()) b.append(String.format("%s\n", a.getUrl()));
                m.setContent(String.format("%s\n\n%s", msg.getContentRaw(), b.toString()));
            }
            Objects.requireNonNull(Client.getConfig().channelToWebook(c)).send(m.build());
        }
    }
}
