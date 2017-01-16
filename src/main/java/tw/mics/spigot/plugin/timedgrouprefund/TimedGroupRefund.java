package tw.mics.spigot.plugin.timedgrouprefund;

import java.util.List;
import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;

public class TimedGroupRefund extends JavaPlugin {
    static TimedGroupRefund instance;
    List<String> enable_world;
    List<String> disable_world;
    List<String> full_disable_world;
    Map<String, Map<String, Boolean>> player_pvp_stats; //player world
    
    public void onEnable() {
        
    }

    public static TimedGroupRefund getInstance() {
        return instance;
    }
}
