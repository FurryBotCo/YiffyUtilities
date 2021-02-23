package rocks.yiff.mc.fabric.utilities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rocks.yiff.mc.fabric.utilities.discord.Client;

import javax.annotation.Nullable;


public class Main implements ModInitializer {
    static JDA jda;
    private final static Logger log = LogManager.getLogger("YiffyUtilities");
    private final Config cnf = new Config();
    @Override
    public void onInitialize() {
        getLogger().info("Initialized.");
        cnf.load();
    }

    public static Logger getLogger() {
        return log;
    }

    public static @Nullable MinecraftServer getServer() { return Client.server; }
}
