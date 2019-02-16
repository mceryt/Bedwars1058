package com.andrei1058.bedwars.listeners;

import com.andrei1058.bedwars.Main;
import com.andrei1058.bedwars.api.GameState;
import com.andrei1058.bedwars.api.ServerType;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.arena.SetupSession;
import com.andrei1058.bedwars.language.Language;
import com.andrei1058.bedwars.language.Messages;
import com.andrei1058.bedwars.shop.ShopCache;
import com.andrei1058.bedwars.upgrades.TeamUpgrade;
import com.andrei1058.bedwars.upgrades.UpgradeGroup;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;

import static com.andrei1058.bedwars.Main.debug;
import static com.andrei1058.bedwars.Main.nms;
import static com.andrei1058.bedwars.language.Language.getMsg;
import static org.bukkit.event.inventory.InventoryAction.*;

public class Inventory implements Listener {

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        if (e.getInventory().getName().equalsIgnoreCase(SetupSession.getInvName())) {
            SetupSession ss = SetupSession.getSession(p);
            if (ss != null) {
                if (ss.getSetupType() == null)
                    ss.cancel();
            }
        }
    }

    /**
     * Manage command-items when clicked in inventory
     */
    @EventHandler
    public void onCommandItemClick(InventoryClickEvent e) {
        //block moving from hotBar
        if (e.getAction() == HOTBAR_SWAP && e.getClick() == ClickType.NUMBER_KEY) {
            if (e.getHotbarButton() > -1) {
                ItemStack i = e.getWhoClicked().getInventory().getItem(e.getHotbarButton());
                if (i != null) {
                    if (isCommandItem(i)) {
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }

        //block moving cursor item outside
        if (e.getCursor() != null) {
            if (e.getCursor().getType() != Material.AIR) {
                if (e.getClickedInventory() == null) {
                    if (isCommandItem(e.getCursor())) {
                        e.getWhoClicked().closeInventory();
                        e.setCancelled(true);
                    }
                } else if (e.getClickedInventory().getType() != e.getWhoClicked().getInventory().getType()) {
                    if (isCommandItem(e.getCursor())) {
                        e.getWhoClicked().closeInventory();
                        e.setCancelled(true);
                    }
                } else {
                    if (isCommandItem(e.getCursor())) e.setCancelled(true);
                }
            }
        }

        //block moving current item outside
        if (e.getCurrentItem() != null) {
            if (e.getCurrentItem().getType() != Material.AIR) {
                if (e.getClickedInventory() == null) {
                    if (isCommandItem(e.getCurrentItem())) {
                        e.getWhoClicked().closeInventory();
                        e.setCancelled(true);
                    }
                } else if (e.getClickedInventory().getType() != e.getWhoClicked().getInventory().getType()) {
                    if (isCommandItem(e.getCurrentItem())) {
                        e.getWhoClicked().closeInventory();
                        e.setCancelled(true);
                    }
                } else {
                    if (isCommandItem(e.getCurrentItem())) e.setCancelled(true);
                }
            }
        }

        //block moving with shift
        if (e.getAction() == MOVE_TO_OTHER_INVENTORY) {
            if (isCommandItem(e.getCurrentItem())) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        ItemStack i = e.getCurrentItem();
        if (i == null) return;
        if (i.getType() == Material.AIR) return;

        /*//Prevent moving of command items
        if (nms.isCustomBedWarsItem(i)) {
            if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                e.setCancelled(true);
                return;
            }
            String[] customData = nms.getCustomData(i).split("_");
            if (customData.length >= 2) {
                if (customData[0].equals("RUNCOMMAND")) {
                    e.setCancelled(true);
                    return;
                }
            }
        }*/

        Arena a = Arena.getArenaByPlayer(p);
        if (a != null) {

            //Prevent players from moving items in stats GUI
            if (e.getInventory().getName().equals(Language.getMsg(p, Messages.PLAYER_STATS_GUI_INV_NAME).replace("{player}", p.getName()))) {
                e.setCancelled(true);
                return;
            }

            /* Make it so they can't toggle their armor */
            if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
                e.setCancelled(true);
                return;
            }

            /* Prevent players from placing items in the upgrades gui. Issue 26. */
            if (e.getInventory().getName().equalsIgnoreCase(getMsg(p, "upgrades." + UpgradeGroup.getUpgradeGroup(a.getGroup()).getName() + ".name"))) {
                e.setCancelled(true);
                for (TeamUpgrade tu : UpgradeGroup.getUpgradeGroup(a.getGroup()).getTeamUpgrades()) {
                    if (tu.getSlot() == e.getSlot()) {
                        tu.doAction(p, a.getTeam(p));
                        return;
                    }
                }
            }
        }

        if (!i.hasItemMeta()) return;
        if (!i.getItemMeta().hasDisplayName()) return;
        if (Main.getServerType() != ServerType.BUNGEE) {
            if (e.getWhoClicked().getLocation().getWorld().getName().equalsIgnoreCase(Main.getLobbyWorld())) {
                e.setCancelled(true);
            }
        }

        /* Check setup gui items */
        if (SetupSession.isInSetupSession(p) && e.getInventory().getName().equalsIgnoreCase(SetupSession.getInvName())) {
            SetupSession ss = SetupSession.getSession(p);
            if (e.getSlot() == SetupSession.getAdvancedSlot()) {
                ss.setSetupType(SetupSession.SetupType.ADVANCED);
            } else if (e.getSlot() == SetupSession.getAssistedSlot()) {
                ss.setSetupType(SetupSession.SetupType.ASSISTED);
            }
            ss.startSetup();
            p.closeInventory();
            return;
        }

        /* Manage shop and upgrades */
        if (a != null) {
            if (a.isSpectator(p)) {
                e.setCancelled(true);
                return;
            }
            if (a.isPlayer(p)) {
                if (e.getInventory().getName().equalsIgnoreCase(getMsg(p, "upgrades." + UpgradeGroup.getUpgradeGroup(a.getGroup()).getName() + ".name"))) {
                    for (TeamUpgrade tu : UpgradeGroup.getUpgradeGroup(a.getGroup()).getTeamUpgrades()) {
                        if (tu.getSlot() == e.getSlot()) {
                            e.setCancelled(true);
                            tu.doAction(p, a.getTeam(p));
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Check if an item is command-item
     */
    private static boolean isCommandItem(ItemStack i) {
        if (i == null) return false;
        if (i.getType() == Material.AIR) return false;
        if (nms.isCustomBedWarsItem(i)) {
            String[] customData = nms.getCustomData(i).split("_");
            if (customData.length >= 2) {
                if (customData[0].equals("RUNCOMMAND")) {
                    return true;
                }
            }
        }
        return false;
    }
}
