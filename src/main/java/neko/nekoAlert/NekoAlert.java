package neko.nekoAlert;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;

@Plugin(id = "nekoalert", name = "NekoAlert", version = "1.0-SNAPSHOT", url = "https://cnmsb.xin/", authors = {"不穿胖次の小奶猫"})
public class NekoAlert {

    @Inject
    private Logger logger;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
    }
}
