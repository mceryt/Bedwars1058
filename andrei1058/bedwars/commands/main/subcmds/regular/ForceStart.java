package com.andrei1058.bedwars.commands.main.subcmds.regular;

import com.andrei1058.bedwars.api.GameState;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.commands.ParentCommand;
import com.andrei1058.bedwars.commands.SubCommand;
import com.andrei1058.bedwars.commands.main.MainCommand;
import com.andrei1058.bedwars.configuration.Messages;
import com.andrei1058.bedwars.configuration.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import static com.andrei1058.bedwars.configuration.Language.getMsg;

public class ForceStart extends SubCommand {
    /**
     * Create a sub-command for a bedWars command
     * Make sure you return true or it will say command not found
     *
     * @param parent parent command
     * @param name   sub-command name
     * @since 0.6.1 api v6
     */
    public ForceStart(ParentCommand parent, String name) {
        super(parent, name);
        setPriority(15);
        showInList(false);
        setDisplayInfo(MainCommand.createTC("§6 ▪ §7/"+MainCommand.getInstance().getName()+" "+getSubCommandName()+" §8 - §eforce start an arena",
                "/"+getParent().getName()+" "+getSubCommandName(), "§fForcestart an arena.\n§fPermission: §c"+Permissions.PERMISSION_FORCESTART));
    }

    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (s instanceof ConsoleCommandSender) return false;
        Player p = (Player) s;
        Arena a = Arena.getArenaByPlayer(p);
        if (a == null){
            p.sendMessage(getMsg(p, Messages.COMMAND_FORCESTART_NOT_IN_GAME));
            return true;
        }
        if (!a.isPlayer(p)){
            p.sendMessage(getMsg(p, Messages.COMMAND_FORCESTART_NOT_IN_GAME));
            return true;
        }
        if (!(p.hasPermission(Permissions.PERMISSION_ALL) || p.hasPermission(Permissions.PERMISSION_FORCESTART))){
            p.sendMessage(getMsg(p, Messages.COMMAND_FORCESTART_NO_PERM));
            return true;
        }
        if (a.getStatus() == GameState.playing) return true;
        if (a.getStatus() == GameState.restarting) return true;
        if (a.getCountdownS() < 5) return true;
        a.setCountdownS(5);
        p.sendMessage(getMsg(p, Messages.COMMAND_FORCESTART_SUCCESS));
        return true;
    }
}
