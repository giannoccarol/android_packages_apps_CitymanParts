/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.cityman.parts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import org.lineageos.cityman.keyhandler.CameraKeyConfig;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

/** Chooses what the camera button does on a press and on a long press. */
public class CameraKeyFragment extends PreferenceFragmentCompat {

    private Preference mShort;
    private Preference mLong;
    private SwitchPreference mScreenOff;

    private final ContentObserver mObserver = new ContentObserver(
            new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange) {
            update();
        }
    };

    private static final class AppEntry {
        final ComponentName component;
        final String label;
        final Drawable icon;

        AppEntry(ComponentName component, String label, Drawable icon) {
            this.component = component;
            this.label = label;
            this.icon = icon;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.camera_key, rootKey);
        mShort = findPreference("camera_key_short");
        mLong = findPreference("camera_key_long");
        mScreenOff = findPreference("camera_key_screen_off");
        mShort.setOnPreferenceClickListener(p -> {
            chooseAction(CameraKeyConfig.SETTING_SHORT, mShort.getTitle());
            return true;
        });
        mLong.setOnPreferenceClickListener(p -> {
            chooseAction(CameraKeyConfig.SETTING_LONG, mLong.getTitle());
            return true;
        });
        mScreenOff.setOnPreferenceChangeListener((p, value) -> {
            CameraKeyConfig.setScreenOffEnabled(requireContext().getContentResolver(),
                    (Boolean) value);
            return true;
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        ContentResolver resolver = requireContext().getContentResolver();
        for (String setting : new String[] {CameraKeyConfig.SETTING_SHORT,
                CameraKeyConfig.SETTING_LONG, CameraKeyConfig.SETTING_SCREEN_OFF}) {
            resolver.registerContentObserver(CameraKeyConfig.uri(setting), false, mObserver,
                    UserHandle.USER_ALL);
        }
        update();
    }

    @Override
    public void onStop() {
        requireContext().getContentResolver().unregisterContentObserver(mObserver);
        super.onStop();
    }

    private void update() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        ContentResolver resolver = context.getContentResolver();
        mShort.setSummary(CameraKey.label(context, CameraKeyConfig.getShort(resolver)));
        mLong.setSummary(CameraKey.label(context, CameraKeyConfig.getLong(resolver)));
        mScreenOff.setChecked(CameraKeyConfig.isScreenOffEnabled(resolver));
    }

    private void chooseAction(String setting, CharSequence title) {
        final Activity activity = requireActivity();
        final ContentResolver resolver = activity.getContentResolver();
        final List<String> values = new ArrayList<>();
        final List<CharSequence> labels = new ArrayList<>();
        values.add(CameraKeyConfig.ACTION_CAMERA);
        labels.add(activity.getString(R.string.camera_key_action_camera));
        if (CameraKey.hasTorch(activity)) {
            values.add(CameraKeyConfig.ACTION_TORCH);
            labels.add(activity.getString(R.string.camera_key_action_torch));
        }
        values.add(CameraKeyConfig.ACTION_APP);
        labels.add(activity.getString(R.string.camera_key_action_app));
        values.add(CameraKeyConfig.ACTION_NONE);
        labels.add(activity.getString(R.string.camera_key_action_none));

        String current = CameraKeyConfig.SETTING_SHORT.equals(setting)
                ? CameraKeyConfig.getShort(resolver) : CameraKeyConfig.getLong(resolver);
        int checked = values.indexOf(
                CameraKeyConfig.isApp(current) ? CameraKeyConfig.ACTION_APP : current);

        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setSingleChoiceItems(labels.toArray(new CharSequence[0]), checked,
                        (dialog, which) -> {
                            dialog.dismiss();
                            String value = values.get(which);
                            if (CameraKeyConfig.ACTION_APP.equals(value)) {
                                pickApp(setting);
                            } else {
                                CameraKeyConfig.set(resolver, setting, value);
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void pickApp(String setting) {
        final Activity activity = requireActivity();
        final PackageManager pm = activity.getPackageManager();
        new Thread(() -> {
            final List<AppEntry> apps = loadApps(activity, pm);
            activity.runOnUiThread(() -> {
                if (isAdded() && !activity.isFinishing()) {
                    showPicker(activity, apps, setting);
                }
            });
        }, "CitymanPartsApps").start();
    }

    private static List<AppEntry> loadApps(Context context, PackageManager pm) {
        Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        int size = Math.round(40 * context.getResources().getDisplayMetrics().density);
        List<AppEntry> apps = new ArrayList<>();
        for (ResolveInfo info : pm.queryIntentActivities(launcher, 0)) {
            Drawable icon = info.loadIcon(pm);
            icon.setBounds(0, 0, size, size);
            apps.add(new AppEntry(
                    new ComponentName(info.activityInfo.packageName, info.activityInfo.name),
                    info.loadLabel(pm).toString(), icon));
        }
        Collator collator = Collator.getInstance();
        apps.sort((a, b) -> collator.compare(a.label, b.label));
        return apps;
    }

    private void showPicker(Activity activity, List<AppEntry> apps, String setting) {
        ArrayAdapter<AppEntry> adapter = new ArrayAdapter<AppEntry>(activity,
                android.R.layout.select_dialog_item, android.R.id.text1, apps) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setCompoundDrawablesRelative(getItem(position).icon, null, null, null);
                view.setCompoundDrawablePadding(Math.round(
                        16 * view.getResources().getDisplayMetrics().density));
                return view;
            }
        };
        new AlertDialog.Builder(activity)
                .setTitle(R.string.camera_key_pick_app)
                .setAdapter(adapter, (dialog, which) -> CameraKeyConfig.set(
                        activity.getContentResolver(), setting,
                        CameraKeyConfig.appAction(apps.get(which).component)))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
