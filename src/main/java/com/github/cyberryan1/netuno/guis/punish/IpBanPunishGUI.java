package com.github.cyberryan1.netuno.guis.punish;

import com.github.cyberryan1.cybercore.spigot.CyberCore;
import com.github.cyberryan1.cybercore.spigot.gui.Gui;
import com.github.cyberryan1.cybercore.spigot.gui.GuiItem;
import com.github.cyberryan1.cybercore.spigot.utils.CyberGuiUtils;
import com.github.cyberryan1.netuno.guis.punish.utils.MultiPunishButton;
import com.github.cyberryan1.netuno.guis.punish.utils.PunishSettings;
import com.github.cyberryan1.netuno.guis.punish.utils.SinglePunishButton;
import com.github.cyberryan1.netuno.managers.StaffPlayerPunishManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class IpBanPunishGUI {

    private final Gui gui;
    private final Player staff;
    private final OfflinePlayer target;
    private final MultiPunishButton punishButtons;
    private final int rowCount;

    public IpBanPunishGUI( Player staff, OfflinePlayer target ) {
        this.staff = staff;
        this.target = target;
        this.punishButtons = PunishSettings.IPBAN_BUTTONS.multiButton();

        this.rowCount = determineRowCount();
        this.gui = new Gui( PunishSettings.IPBAN_INVENTORY_NAME.coloredString().replace( "[TARGET]", target.getName() ),
                this.rowCount, CyberGuiUtils.getBackgroundGlass() );
        insertItems();
    }

    public void insertItems() {
        // Warn buttons are at 10-12 and 14-16, then repeat every row as needed
        final List<SinglePunishButton> buttonsList = this.punishButtons.getButtons();

        for ( SinglePunishButton button : buttonsList ) {
            if ( button.getItemMaterial().isAir() ) { continue; }

            GuiItem item = new GuiItem( button.getItem( this.target ), button.getIndex(), ( i ) -> {
                button.executePunish( this.staff, this.target );
                staff.closeInventory();
            } );

            this.gui.addItem( item );
        }

        gui.openInventory( staff );
    }

    public void open() {
        Bukkit.getScheduler().runTask( CyberCore.getPlugin(), () -> {
            gui.openInventory( this.staff );
            gui.setCloseEvent( () -> {
                StaffPlayerPunishManager.removeStaff( this.staff );
                StaffPlayerPunishManager.removeStaffSilent( this.staff );
            } );
        } );
    }

    private int determineRowCount() {
        int highestIndex = 0;
        for ( SinglePunishButton button : this.punishButtons.getButtons() ) {
            if ( button.getItemMaterial().isAir() == false && button.getIndex() > highestIndex ) {
                highestIndex = button.getIndex();
            }
        }

        if ( highestIndex <= 16 ) { return 3; }
        if ( highestIndex <= 25 ) { return 4; }
        return 5;
    }
}