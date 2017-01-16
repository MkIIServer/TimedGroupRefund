package tw.mics.spigot.plugin.timedgrouprefund;

import java.util.List;
import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;

import tw.mics.spigot.plugin.timedgrouprefund.command.RgwfCommand;
import tw.mics.spigot.plugin.timedgrouprefund.config.Config;

public class TimedGroupRefund extends JavaPlugin {
    static TimedGroupRefund instance;
    List<String> enable_world;
    List<String> disable_world;
    List<String> full_disable_world;
    Map<String, Map<String, Boolean>> player_pvp_stats; //player world
    
    public void onEnable() {
        instance = this;
        Config.load();
        this.getCommand("rgwf").setExecutor(new RgwfCommand(this));
    }

    public static TimedGroupRefund getInstance() {
        return instance;
    }
}
