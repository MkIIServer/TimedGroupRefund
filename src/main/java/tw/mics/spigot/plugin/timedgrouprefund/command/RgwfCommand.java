package tw.mics.spigot.plugin.timedgrouprefund.command;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import tw.mics.spigot.plugin.timedgrouprefund.TimedGroupRefund;

public class RgwfCommand implements CommandExecutor {
    TimedGroupRefund plugin;
    List<Material> blockBlockList;

    public RgwfCommand(TimedGroupRefund i) {
        this.plugin = i;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return true;
    }
}
