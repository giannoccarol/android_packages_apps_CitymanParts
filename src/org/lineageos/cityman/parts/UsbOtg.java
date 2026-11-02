/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.cityman.parts;

import android.os.SystemProperties;

/**
 * USB OTG switch. No driver reports the USB-C role on this phone, so the host
 * role is chosen by hand: init.cityman.rc turns vendor.cityman.otg into the
 * usb power supply's usb_otg property, which starts the USB host and VBUS.
 * The property is not persistent: after a reboot the port is back in device
 * mode.
 */
public final class UsbOtg {

    private static final String PROP_OTG = "vendor.cityman.otg";

    private UsbOtg() {}

    public static boolean isEnabled() {
        return SystemProperties.getBoolean(PROP_OTG, false);
    }

    public static void setEnabled(boolean enabled) {
        SystemProperties.set(PROP_OTG, enabled ? "1" : "0");
    }
}
