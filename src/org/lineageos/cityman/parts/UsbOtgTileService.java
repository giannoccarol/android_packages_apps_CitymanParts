/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.cityman.parts;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

/**
 * Quick Settings tile that switches the USB-C port between device mode
 * (charging, adb, MTP) and host mode (keyboards, controllers, USB drives).
 */
public class UsbOtgTileService extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();
        update();
    }

    @Override
    public void onClick() {
        super.onClick();
        UsbOtg.setEnabled(!UsbOtg.isEnabled());
        update();
    }

    private void update() {
        Tile tile = getQsTile();
        if (tile == null) {
            return;
        }
        boolean on = UsbOtg.isEnabled();
        tile.setState(on ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.setSubtitle(getString(on ? R.string.usb_otg_on : R.string.usb_otg_off));
        tile.updateTile();
    }
}
