/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.cityman.parts;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

import org.lineageos.cityman.keyhandler.CameraKeyConfig;

/** Labels and availability of the camera button settings. */
final class CameraKey {

    private CameraKey() {}

    static boolean hasTorch(Context context) {
        CameraManager manager = context.getSystemService(CameraManager.class);
        try {
            for (String id : manager.getCameraIdList()) {
                CameraCharacteristics c = manager.getCameraCharacteristics(id);
                Boolean flash = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer facing = c.get(CameraCharacteristics.LENS_FACING);
                if (Boolean.TRUE.equals(flash) && facing != null
                        && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    return true;
                }
            }
        } catch (CameraAccessException | IllegalArgumentException e) {
            return false;
        }
        return false;
    }

    static String label(Context context, String action) {
        if (CameraKeyConfig.ACTION_CAMERA.equals(action)) {
            return context.getString(R.string.camera_key_action_camera);
        }
        if (CameraKeyConfig.ACTION_TORCH.equals(action)) {
            return context.getString(R.string.camera_key_action_torch);
        }
        if (CameraKeyConfig.isApp(action)) {
            return appLabel(context, CameraKeyConfig.appComponent(action));
        }
        return context.getString(R.string.camera_key_action_none);
    }

    private static String appLabel(Context context, ComponentName component) {
        if (component != null) {
            PackageManager pm = context.getPackageManager();
            try {
                return context.getString(R.string.camera_key_action_app_named,
                        pm.getActivityInfo(component, 0).loadLabel(pm));
            } catch (PackageManager.NameNotFoundException e) {
                // Uninstalled since it was chosen
            }
        }
        return context.getString(R.string.camera_key_app_missing);
    }
}
