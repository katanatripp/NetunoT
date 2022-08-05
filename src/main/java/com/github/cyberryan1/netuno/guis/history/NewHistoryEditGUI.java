package com.github.cyberryan1.netuno.guis.history;

import com.github.cyberryan1.cybercore.CyberCore;
import com.github.cyberryan1.cybercore.helpers.gui.GUI;
import com.github.cyberryan1.cybercore.helpers.gui.GUIItem;
import com.github.cyberryan1.cybercore.utils.CoreGUIUtils;
import com.github.cyberryan1.cybercore.utils.CoreUtils;
import com.github.cyberryan1.cybercore.utils.VaultUtils;
import com.github.cyberryan1.netuno.api.ApiNetuno;
import com.github.cyberryan1.netuno.guis.utils.GUIUtils;
import com.github.cyberryan1.netuno.utils.CommandErrors;
import com.github.cyberryan1.netuno.utils.settings.Settings;
import com.github.cyberryan1.netunoapi.models.punishments.NPunishment;
import com.github.cyberryan1.netunoapi.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class NewHistoryEditGUI {

    private final GUI gui;
    private final OfflinePlayer target;
    private final Player staff;
    private final NPunishment punishment;

    private boolean editingLength = false;
    private boolean editingReason = false;

    public NewHistoryEditGUI( OfflinePlayer target, Player staff, int punId ) {
        this.target = target;
        this.staff = staff;
        this.punishment = ApiNetuno.getData().getNetunoPuns().getPunishment( punId );

        this.gui = new GUI( "&sEdit Punishment &p#" + punId, 6, CoreGUIUtils.getBackgroundGlass() );
        insertItems();
    }

    public NewHistoryEditGUI( OfflinePlayer target, Player staff, NPunishment punishment ) {
        this.target = target;
        this.staff = staff;
        this.punishment = punishment;

        this.gui = new GUI( "&sEdit Punishment &p#" + punishment.getId(), 6, CoreGUIUtils.getBackgroundGlass() );
        insertItems();
    }

    public void insertItems() {
        // info: 13
        // back to history list: 49
        // unpunish layout:
        //      delete punishment barrier: 31
        // no-length layout:
        //      edit reason paper: 30 || delete punishment barrier: 32
        // default:
        //      edit length clock: 29 || edit reason paper: 31
        //      delete punishment barrier: 33

        // Punishment Info
        gui.setItem( 13, new GUIItem( GUIUtils.getPunishmentItem( punishment ), 13 ) );

        if ( punishment.getPunishmentType().hasNoReason() ) {
            // Delete Punishment
            gui.setItem( 31, getDeleteBarrier( 31 ) );
        }

        else if ( punishment.getPunishmentType().hasNoLength() || punishment.isActive() == false ) {
            // Edit Reason
            gui.setItem( 30, getEditReasonPaper( 30 ) );
            // Delete Punishment
            gui.setItem( 32, getDeleteBarrier( 32 ) );
        }

        else {
            // Edit Length
            gui.setItem( 29, getEditLengthClock( 29 ) );
            // Edit Reason
            gui.setItem( 31, getEditReasonPaper( 31 ) );
            // Delete Punishment
            gui.setItem( 33, getDeleteBarrier( 33 ) );
        }

        gui.createInventory();
    }

    public void open() {
        Bukkit.getScheduler().runTask( CyberCore.getPlugin(), () -> {
            gui.openInventory( staff );
        } );
    }

    public void onReasonEditInput( String newReason ) {
        HistoryEditManager.removeEditing( staff );
        editingReason = false;
        if ( newReason.equalsIgnoreCase( "cancel" ) == false ) {
            punishment.setReason( newReason );
            ApiNetuno.getData().getNetunoPuns().updatePunishment( punishment );
        }

        NewHistoryEditGUI newGui = new NewHistoryEditGUI( target, staff, punishment );
        newGui.open();
    }

    public void onLengthEditInput( String newLength ) {
        HistoryEditManager.removeEditing( staff );
        editingLength = false;
        if ( newLength.equalsIgnoreCase( "cancel" ) == false ) {
            if ( TimeUtils.isAllowableLength( newLength ) ) {
                punishment.setLength( TimeUtils.durationFromUnformatted( newLength ).timestamp() );
                ApiNetuno.getData().getNetunoPuns().updatePunishment( punishment );
            }
            else {
                CommandErrors.sendInvalidTimespan( staff, newLength );
                HistoryEditManager.addEditing( staff, this );
                editingLength = true;
                CoreUtils.sendMsg( staff, "&sTry again, or type &p\"cancel\"&s to cancel" );
                return;
            }
        }

        NewHistoryEditGUI newGui = new NewHistoryEditGUI( target, staff, punishment );
        newGui.open();
    }

    private GUIItem getDeleteBarrier( int slot ) {
        return new GUIItem( GUIUtils.createItem( Material.BARRIER, "&sDelete Punishment" ), slot, () -> {
            staff.closeInventory();

            if ( VaultUtils.hasPerms( staff, Settings.HISTORY_DELETE_PERMISSION.string() ) ) {
                NewHistoryDeleteConfirmGUI deleteGui = new NewHistoryDeleteConfirmGUI( target, staff, punishment );
                deleteGui.open();
                staff.playSound( staff.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 10, 2 );
            }

            else {
                CoreUtils.sendMsg( staff, Settings.PERM_DENIED_MSG.string() );
            }
        } );
    }

    private GUIItem getEditReasonPaper( int slot ) {
        return new GUIItem( GUIUtils.createItem( Material.PAPER, "&sEdit Reason" ), slot, () -> {
            HistoryEditManager.addEditing( staff, this );
            staff.closeInventory();
            editingReason = true;
            CoreUtils.sendMsg( staff, "&sPlease enter the new reason for punishment &p#" + punishment.getId() );
            CoreUtils.sendMsg( staff, "&sTo cancel, type &p\"cancel\"" );
            staff.playSound( staff.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 10, 2 );
        } );
    }

    private GUIItem getEditLengthClock( int slot ) {
        return new GUIItem( GUIUtils.createItem( Material.CLOCK, "&sEdit Length" ), slot, () -> {
            HistoryEditManager.addEditing( staff, this );
            staff.closeInventory();
            editingLength = true;
            CoreUtils.sendMsg( staff, "&sPlease enter the new length for punishment &p#" + punishment.getId() );
            CoreUtils.sendMsg( staff, "&sTo cancel, type &p\"cancel\"" );
            staff.playSound( staff.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 10, 2 );
        } );
    }

    //
    // Getters & Setters
    //

    public OfflinePlayer getTarget() { return target; }

    public Player getStaff() { return staff; }

    public boolean isEditingLength() { return editingLength; }

    public boolean isEditingReason() { return editingReason; }

    public void setEditingLength( boolean editingLength ) { this.editingLength = editingLength; }

    public void setEditingReason( boolean editingReason ) { this.editingReason = editingReason; }
}