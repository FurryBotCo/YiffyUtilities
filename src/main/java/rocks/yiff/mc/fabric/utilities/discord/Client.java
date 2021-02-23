package rocks.yiff.mc.fabric.utilities.discord;

import club.minnced.discord.webhook.WebhookCluster;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.minecraft.server.MinecraftServer;
import rocks.yiff.mc.fabric.utilities.Config;

import javax.security.auth.login.LoginException;
import java.util.Collections;

public class Client {
    private static JDA jda;
    private static Config.Configuration cnf;
    public static MinecraftServer server;
    public static WebhookCluster w = new WebhookCluster();
    public static void init(Config.Configuration cnf) {
        Client.cnf = cnf;
        try {
            jda = JDABuilder
                    .createLight(cnf.getToken(), Collections.singleton(GatewayIntent.GUILD_MESSAGES))
                    .setChunkingFilter(ChunkingFilter.NONE)
                    .setMemberCachePolicy(MemberCachePolicy.NONE)
                    .addEventListeners(new EventHandler())
                    .build();
        } catch(LoginException e) {
            e.printStackTrace();
        }
    }

    public static Config.Configuration getConfig() { return cnf; }

    public static void onServerStopped() {
        if(jda != null)  jda.shutdownNow();
    }

    public static void onServerTick(MinecraftServer server) {
        if(Client.server == null) Client.server = server;
    }
}
