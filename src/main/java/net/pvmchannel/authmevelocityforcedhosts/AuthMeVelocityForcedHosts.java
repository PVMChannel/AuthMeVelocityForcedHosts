package net.pvmchannel.authmevelocityforcedhosts;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.github._4drian3d.authmevelocity.api.velocity.event.ProxyLoginEvent;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(
        id = "authmevelocityforcedhosts",
        name = "AuthMeVelocityForcedHosts",
        authors = {"PVMChannel"},
        version = BuildConstants.VERSION
)
public class AuthMeVelocityForcedHosts {

    private Path dataDirectory;
    private ProxyServer proxy;
    private Logger logger;

    @Inject
    public AuthMeVelocityForcedHosts(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory){
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    private CommentedConfigurationNode config;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) throws IOException {
        if (Files.notExists(dataDirectory)) {
            Files.createDirectory(dataDirectory);
        }

        final Path configFile = dataDirectory.resolve("config.yml");
        if (Files.notExists(configFile)) {
            try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream("config.yml")) {
                Files.copy(stream, configFile);
            }
        }

        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(configFile).build();
        config = loader.load();
    }

    @Subscribe
    public void onLoginInProxy(ProxyLoginEvent event){
        Player player = event.player();
        String host = player.getVirtualHost().get().getHostString();
        
        if(!config.node("forced_hosts").node(host).empty()){
            String serverName = config.node("forced_hosts").node(host).getString();
            RegisteredServer server = proxy.getServer(serverName).orElseThrow(() -> new RuntimeException("Server not found: " + serverName));

            player.createConnectionRequest(server).connect();
        }
    }
}
