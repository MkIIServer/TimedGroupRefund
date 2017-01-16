package tw.mics.spigot.plugin.timedgrouprefund.command;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.github.cheesesoftware.PowerfulPermsAPI.CachedGroup;
import com.github.cheesesoftware.PowerfulPermsAPI.Group;
import com.github.cheesesoftware.PowerfulPermsAPI.PermissionManager;
import com.github.cheesesoftware.PowerfulPermsAPI.PowerfulPermsPlugin;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import net.milkbowl.vault.economy.Economy;
import tw.mics.spigot.plugin.timedgrouprefund.TimedGroupRefund;
import tw.mics.spigot.plugin.timedgrouprefund.config.Config;

public class RgwfCommand implements CommandExecutor {
    TimedGroupRefund plugin;
    final PowerfulPermsPlugin ppplugin;
    PermissionManager permissionManager;
    Economy econ = null;
    HashMap<String, Double> groupvalue;

    public RgwfCommand(TimedGroupRefund i) {
        this.plugin = i;
        ppplugin = (PowerfulPermsPlugin) Bukkit.getPluginManager().getPlugin("PowerfulPerms");
        permissionManager = ppplugin.getPermissionManager();
        setupEconomy();
        
        groupvalue = new HashMap<String, Double>();
        for(String str : Config.REFUND_GROUP_VALUE.getStringList()){
            String[] strs = str.split(":");
            groupvalue.put(strs[0], Double.valueOf(strs[1]) * Config.REFUND_PERCENT.getDouble());
        }
    }
    
    private boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length != 2 && args.length != 3)return false;
        Boolean slient = false;
        if(args.length == 3 && args[2].equals("-s")) slient = true;
        Player p = Bukkit.getPlayer(args[0]);
        if(p == null) {
            if(!slient)sender.sendMessage("Player not found");
            return true;
        }
        Group group = permissionManager.getGroup(args[1]);
        if(group == null) {
            if(!slient)sender.sendMessage("Group not found");
            return true;
        }
        if(!groupvalue.containsKey(args[1])) {
            if(!slient)sender.sendMessage("Group not in value list, can't refund");
            return true;
        }
        UUID uuid = p.getUniqueId();
        ListenableFuture<LinkedHashMap<String, List<CachedGroup>>> future = permissionManager.getPlayerOwnGroups(uuid);
        future.addListener(new Runnable() {
            Boolean slient;
            public Runnable init(Boolean slient) {
                this.slient = slient;
                return this;
            }
            
            @Override
            public void run() {
                CachedGroup refundgroup = null;
                try {
                    LinkedHashMap<String, List<CachedGroup>> unknow = future.get();
                    if(unknow.keySet().isEmpty()) return;
                    List<CachedGroup> cachedgroups = unknow.get(unknow.keySet().toArray()[0]);
                    for(CachedGroup cachedgroup : cachedgroups){
                        if(group.getId() == cachedgroup.getGroupId()){
                            refundgroup = cachedgroup;
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                if(refundgroup == null) {
                    if(!slient) sender.sendMessage("This player don't have this group, can't refund");
                    return;
                }
                if(!refundgroup.willExpire()){
                    if(!slient) sender.sendMessage("This group is never expired, can't refund");
                    return;
                }
                Date expireddate = refundgroup.getExpirationDate();
                Long diff = (expireddate.getTime() - Calendar.getInstance().getTimeInMillis());
                Double value = Math.floor(groupvalue.get(args[1]) * diff / 2592000000.0);
                p.sendMessage(String.format("您已獲得退款 %.0f M幣", value));
                permissionManager.removePlayerGroup(uuid, refundgroup.getGroupId(), "all", false, expireddate);
                econ.depositPlayer(p, value);
            }

        }.init(slient), MoreExecutors.sameThreadExecutor());
        return true;
    }
}
