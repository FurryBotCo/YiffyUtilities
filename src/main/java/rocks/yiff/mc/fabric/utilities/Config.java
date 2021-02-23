package rocks.yiff.mc.fabric.utilities;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.external.JDAWebhookClient;
import club.minnced.discord.webhook.send.AllowedMentions;
import com.google.common.collect.Lists;
import com.google.gson.*;
import rocks.yiff.mc.fabric.utilities.discord.Client;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Config {
    JsonParser parser = new JsonParser();
    Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
    final Path path = Paths.get("config", "utils.json").toAbsolutePath();
    public Configuration cnf;
    public Config() { }

    void load() {
        Main.getLogger().info(String.format("Loading configuration file from \"%s\".", path.toString()));
        try (FileReader reader = new FileReader(this.getFile())) {
            JsonObject json = this.fix(parser.parse(reader).getAsJsonObject());
            if(!json.has("token") || json.get("token").isJsonNull() || json.get("token").getAsString().isEmpty()) {
                throw new Exception("Token is null or invalid.");
            }
            cnf = new Configuration(json);
            Client.init(cnf);
        } catch(FileNotFoundException e) {
            this.setup();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private JsonObject fix() { return fix(new JsonObject()); }

    private JsonObject fix(JsonObject obj) {
        // we consider null & a non-zero string to be "valid" until the check happens further up
        // in this file
        if(!obj.has("token") || (!obj.get("token").isJsonNull() && obj.get("token").getAsString().isEmpty())) obj.add("token", JsonNull.INSTANCE);
        if(!obj.has("channels") || obj.get("channels").isJsonNull() || !obj.get("channels").isJsonArray()) obj.add("channels", new JsonArray());
        try(FileWriter writer = new FileWriter(getFile())) {
            writer.write(gson.toJson(obj));
            writer.flush();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    void setup() {
        Main.getLogger().info("Creating default configuration file.");
        this.fix();
        this.load();
    }

    String getFile() { return path.toString(); }

    public static class Configuration {
        List<ChannelConfig> channels = Lists.newArrayList();
        String token;
        public Configuration(JsonObject obj) {
            JsonArray arr = obj.get("channels").getAsJsonArray();
            for(int i = 0; i <= (arr.size()-1); i++) {
                JsonObject ck = arr.get(i).getAsJsonObject();
                this.channels.add(new ChannelConfig(ck.get("id").getAsString(), ck.get("webhook").getAsString()));
            }
            this.token = obj.get("token").getAsString();
        }

        public String getToken() { return token; }
        public List<ChannelConfig> getChannels() { return channels; }

        public boolean hasChannel(String id) {
            return Arrays.stream(this.channels.toArray()).filter(ch -> ((ChannelConfig) ch).id.equals(id)).toArray().length != 0;
        }

        public @Nullable String channelToWebhookURL(String id) {
            Optional<Object> v = Arrays.stream(this.channels.toArray()).filter(ch -> ((ChannelConfig) ch).id.equals(id)).findFirst();
            return v.map(ch -> ((ChannelConfig) ch).webhook).orElse(null);
        }

        public @Nullable String[] getOtherChannels(String id) {
            Object[] o = Arrays.stream(this.channels.toArray()).filter(ch -> !((ChannelConfig) ch).id.equals(id)).map(ch -> ((ChannelConfig) ch).id).toArray();
            return Arrays.copyOf(o, o.length, String[].class);
        }

        public @Nullable JDAWebhookClient channelToWebook(String id) {
            Optional<Object> v = Arrays.stream(this.channels.toArray()).filter(ch -> ((ChannelConfig) ch).id.equals(id)).findFirst();
            return v.map(ch -> ((ChannelConfig) ch).hook).orElse(null);
        }

        public @Nullable String webhookToChannel(String url) {
            Optional<Object> v = Arrays.stream(this.channels.toArray()).filter(ch -> ((ChannelConfig) ch).webhook.equals(url)).findFirst();
            return v.map(ch -> ((ChannelConfig) ch).id).orElse(null);
        }

        public @Nullable String webhookToChannel(URL url) {
            return webhookToChannel(url.toString());
        }

        public JDAWebhookClient[] getWebhooks() {
            Object[] o = Arrays.stream(this.channels.toArray()).map(ch -> ((ChannelConfig) ch).hook).toArray();
            return Arrays.copyOf(o, o.length, JDAWebhookClient[].class);
        }
    }

    public static class ChannelConfig {
        String id;
        String webhook;
        JDAWebhookClient hook;
        public ChannelConfig(String id, String webhook) {
            this.id = id;
            this.webhook = webhook;
            hook = new WebhookClientBuilder(webhook)
                    .setAllowedMentions(AllowedMentions.none().withParseUsers(true))
                    .setWait(false)
                    .buildJDA();
            Client.w.addWebhooks(hook);
        }

        public String getId() { return id; }
        public String getWebhook() { return webhook; }
        public JDAWebhookClient getHook() { return hook; }
    }
}