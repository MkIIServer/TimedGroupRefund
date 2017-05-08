package tw.mics.spigot.plugin.timedgrouprefund.command;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.Node;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.exceptions.ObjectLacksException;
import net.milkbowl.vault.economy.Economy;
import tw.mics.spigot.plugin.timedgrouprefund.TimedGroupRefund;
import tw.mics.spigot.plugin.timedgrouprefund.config.Config;

public class RgwfCommand implements CommandExecutor {
    TimedGroupRefund plugin;
    final LuckPermsApi api = LuckPerms.getApi();
    Economy econ = null;
    HashMap<String, Double> groupvalue;

    public RgwfCommand(TimedGroupRefund i) {
        this.plugin = i;
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
        if(args.length != 2 && args.length != 3) return false;
        Bukkit.getScheduler().runTaskAsynchronously(TimedGroupRefund.getInstance(), new Runnable(){
            @Override
            public void run() {
                Boolean slient = false;
                if(args.length == 3 && args[2].equals("-s")) slient = true;
                Player p = Bukkit.getPlayer(args[0]);
                if(p == null) {
                    if(!slient)sender.sendMessage("Player not found");
                    return;
                }
                String group = args[1];
                if(!p.hasPermission("group."+group)) {
                    if(!slient)sender.sendMessage("Player don't have this group or this group not exist");
                    return;
                }
                if(!groupvalue.containsKey(args[1])) {
                    if(!slient)sender.sendMessage("Group not in value list, can't refund");
                    return;
                }
                UUID uuid = p.getUniqueId();
                User user = api.getUser(uuid);
                Set<Node> nodes = user.getTemporaryPermissionNodes();
                Iterator<Node> itr = nodes.iterator();
                while(itr.hasNext()){
                    Node node = itr.next();
                    if(
                        node.isGroupNode() &&
                        node.getGroupName().equals(group)
                    ){
                        Date expireddate = node.getExpiry();
                        Long diff = (expireddate.getTime() - Calendar.getInstance().getTimeInMillis());
                        Double value = Math.floor(groupvalue.get(args[1]) * diff / 2592000000.0);
                        p.sendMessage(String.format("您已獲得退款 %.0f M幣", value));
                        try {
                            user.unsetPermission(node);
                            api.getStorage().saveUser(user);
                            econ.depositPlayer(p, value);
                        } catch (ObjectLacksException e) {e.printStackTrace();}
                        return;
                    }
                }
                if(!slient) {
                    sender.sendMessage("This player don't have this group or it never expired, can't refund.");
                }
                return;
            }}
        );
        return true;
    }
}
