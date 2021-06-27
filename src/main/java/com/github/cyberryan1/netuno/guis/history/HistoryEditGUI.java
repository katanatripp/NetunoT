package com.github.cyberryan1.netuno.guis.history;

import com.github.cyberryan1.netuno.classes.Punishment;
import com.github.cyberryan1.netuno.guis.events.GUIEventInterface;
import com.github.cyberryan1.netuno.guis.events.GUIEventManager;
import com.github.cyberryan1.netuno.guis.events.GUIEventType;
import com.github.cyberryan1.netuno.guis.utils.GUIUtils;
import com.github.cyberryan1.netuno.utils.*;
import com.github.cyberryan1.netuno.utils.database.Database;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class HistoryEditGUI implements Listener {

    private final Database DATA = Utils.getDatabase();

    private final Inventory GUI;
    private final OfflinePlayer target;
    private final Player staff;
    private final Punishment punishment;

    private boolean editingLength = false;
    private boolean editingReason = false;

    public HistoryEditGUI( OfflinePlayer target, Player staff, int punID ) {
        this.target = target;
        this.staff = staff;

        Punishment workingWith;
        if ( DATA.checkPunIDExists( punID ) ) { punishment = DATA.getPunishment( punID ); }
        // else is an ip punishment
        else { punishment = DATA.getIPPunishment( punID ); }

        String guiName = Utils.getColored( "&7Edit Punishment &6#" + punID );
        GUI = Bukkit.createInventory( null, 54, guiName );
        insertItems();

        GUIEventManager.addEvent( this );
    }

    public void insertItems() {
        // glass: everything else
        // pun info sign: 13
        // back to history list: 49
        // unpunish layout:
        //      delete punishment barrier: 31
        // no-length layout:
        //      edit reason paper: 30 || delete punishment barrier: 32
        // default:
        //      edit length clock: 29 || edit reason paper: 31
        //      delete punishment barrier: 33

        ItemStack items[] = new ItemStack[54];
        for ( int index = 0; index < items.length; index++ ) {
            items[index] = GUIUtils.getBackgroundGlass();
        }

        items[13] = punishment.getPunishmentAsSign();

        if ( punishment.checkIsUnpunish() ) {
            items[31] = GUIUtils.createItem( Material.BARRIER, "&7Delete punishment" );
        }
        else if ( punishment.checkHasNoTime() || punishment.getActive() == false ) {
            items[30] = GUIUtils.createItem( Material.PAPER, "&7Edit reason" );
            items[32] = GUIUtils.createItem( Material.BARRIER, "&7Delete punishment" );
        }
        else {
            items[29] = GUIUtils.createItem( Material.CLOCK, "&7Edit length" );
            items[31] = GUIUtils.createItem( Material.PAPER, "&7Edit reason" );
            items[33] = GUIUtils.createItem( Material.BARRIER, "&7Delete punishment" );
        }

        items[49] = GUIUtils.createItem( Material.ARROW, "&7Go back" );
        GUI.setContents( items );
    }

    public void openInventory( Player player ) {
        player.openInventory( GUI );
    }

    @GUIEventInterface( type = GUIEventType.INVENTORY_CLICK )
    public void onInventoryClick( InventoryClickEvent event ) {
        if ( event.getWhoClicked().getName().equals( staff.getName() ) == false ) { return; }
        if ( event.getView().getTitle().equals( Utils.getColored( "&7Edit Punishment &6#" + punishment.getID() ) ) == false ) { return; }

        event.setCancelled( true );
        ItemStack itemClicked = event.getCurrentItem();
        if ( itemClicked == null || itemClicked.getType().isAir() ) { return; }
        String itemName = itemClicked.getItemMeta().getDisplayName();
        if ( itemName.equals( Utils.getColored( "&7Edit length" ) ) == false
            && itemName.equals( Utils.getColored( "&7Edit reason" ) ) == false
            && itemName.equals( Utils.getColored( "&7Delete punishment" ) ) == false
            && itemName.equals( Utils.getColored( "&7Go back" ) ) == false ) { return; }

        if ( itemClicked.equals( GUIUtils.createItem( Material.CLOCK, "&7Edit length" ) ) ) {
            if ( VaultUtils.hasPerms( staff, ConfigUtils.getStr( "history.time.perm" ) ) == false ) {
                CommandErrors.sendInvalidPerms( staff );
            }

            else if ( editingLength == false ) {
                event.getWhoClicked().closeInventory();
                event.getWhoClicked().sendMessage( Utils.getColored( "&7Please type the new length for punishment &6#" + punishment.getID() ) );
                event.getWhoClicked().sendMessage( Utils.getColored( "&7To cancel, type &6\"cancel\"" ) );
                editingLength = true;
            }
        }

        else if ( itemClicked.equals( GUIUtils.createItem( Material.PAPER, "&7Edit reason" ) ) ) {
            if ( VaultUtils.hasPerms( staff, ConfigUtils.getStr( "history.reason.perm" ) ) == false ) {
                CommandErrors.sendInvalidPerms( staff );
            }

            else if ( editingReason == false ) {
                event.getWhoClicked().closeInventory();
                event.getWhoClicked().sendMessage( Utils.getColored( "&7Please type the new reason for punishment &6#" + punishment.getID() ) );
                event.getWhoClicked().sendMessage( Utils.getColored( "&7To cancel, type &6\"cancel\"" ) );
                editingReason = true;
            }
        }

        else if ( itemClicked.equals( GUIUtils.createItem( Material.BARRIER, "&7Delete punishment" ) ) ) {
            if ( VaultUtils.hasPerms( staff, ConfigUtils.getStr( "history.delete.perm" ) ) == false ) {
                CommandErrors.sendInvalidPerms( staff );
            }

            else {
                event.getWhoClicked().closeInventory(); // like this close inventory here, helps prevent accidental deletes
                HistoryDeleteConfirmGUI deleteGUI = new HistoryDeleteConfirmGUI( target, staff, punishment );
                deleteGUI.openInventory( staff );
            }
        }

        else if ( itemClicked.equals( GUIUtils.createItem( Material.ARROW, "&7Go back" ) ) ) {
            event.getWhoClicked().closeInventory();
            HistoryListGUI gui = new HistoryListGUI( target, staff, 1 );
            gui.openInventory( staff );

            GUIEventManager.removeEvent( this );
        }

    }

    @GUIEventInterface( type = GUIEventType.INVENTORY_DRAG )
    public void onInventoryDrag( InventoryDragEvent event ) {
        if ( event.getWhoClicked().getName().equals( staff.getName() ) == false ) { return; }
        if ( event.getView().getTitle().equals( Utils.getColored( "&7Edit Punishment &6#" + punishment.getID() ) ) == false ) { return; }

        event.setCancelled( true );
    }

    @GUIEventInterface( type = GUIEventType.INVENTORY_CLOSE )
    public void onInventoryClose( InventoryCloseEvent event ) {
        if ( event.getPlayer().getName().equals( staff.getName() ) == false ) { return; }
        if ( event.getView().getTitle().equals( Utils.getColored( "&7Edit Punishment &6#" + punishment.getID() ) ) == false ) { return; }

        if ( editingLength == false || editingReason == false ) { return; }

        GUIEventManager.removeEvent( this );
    }

    @GUIEventInterface( type = GUIEventType.PLAYER_CHAT )
    public void onPlayerChatEvent( PlayerChatEvent event ) {
        Utils.logWarn( "onPlayerChatEvent fired sucessfully" ); // ! debug
        if ( editingLength ) {
            event.setCancelled( true );

            if ( event.getMessage().equalsIgnoreCase( "cancel" ) ) {
                HistoryEditGUI newGUI = new HistoryEditGUI( target, event.getPlayer(), punishment.getID() );
                newGUI.openInventory( event.getPlayer() );
                editingLength = false;
            }

            else if ( Time.isAllowableLength( event.getMessage() ) ) {
                punishment.setLength( Time.getTimestampFromLength( event.getMessage() ) );
                DATA.setPunishmentLength( punishment.getID(), punishment.getLength() );

                HistoryEditGUI newGUI = new HistoryEditGUI( target, event.getPlayer(), punishment.getID() );
                newGUI.openInventory( event.getPlayer() );
                editingLength = false;
            }

            else {
                CommandErrors.sendInvalidTimespan( event.getPlayer(), event.getMessage() );
                event.getPlayer().sendMessage( Utils.getColored( "&7Try again, or say &6\"cancel\"&7 to cancel" ) );
            }
        }

        else if ( editingReason ) {
            event.setCancelled( true );

            if ( event.getMessage().equalsIgnoreCase( "cancel" ) == false ) {
                punishment.setReason( event.getMessage() );
                DATA.setPunishmentReason( punishment.getID(), punishment.getReason() );
            }

            HistoryEditGUI newGUI = new HistoryEditGUI( target, event.getPlayer(), punishment.getID() );
            newGUI.openInventory( event.getPlayer() );
            editingReason = false;
        }
    }
}
