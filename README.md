# CitymanParts

Device settings for the Microsoft Lumia 950 XL (`cityman`) LineageOS 23
(Android 16) port. A system app that adds:

- a **Performance profile** launcher icon;
- **Settings > Battery > Performance profile**: power save, balanced or
  performance, with the profile in use as the entry's summary;
- a **Performance** Quick Settings tile that cycles the profiles (long press
  opens the page).

The choice is stored in `persist.vendor.power.profile`. The power HAL
extension of the device tree
([`giannoccarol/android_device_msft_cityman`](https://github.com/giannoccarol/android_device_msft_cityman),
branch `lineage-23-cityman`, `power/`) turns it into perfd locks:

| Profile | Result |
|---|---|
| Power save | A57 cluster offline, A53 up to 1.25 GHz |
| Balanced (default) | at most two A57 cores, up to 1.44 GHz |
| Performance | all eight cores, no profile limit (thermal-engine still throttles the A57s) |

Battery Saver uses power save while it is on; the page and the tile show it
and keep the stored choice for when it turns off.

LineageOS 23 no longer has performance profiles of its own (the lineage-sdk
`PerformanceManager` was removed), so the device provides the page.

## Build

Checked out at `packages/apps/CitymanParts` (see `lineageos-23/` in
`giannoccarol/talkman-manifests`) and added to `PRODUCT_PACKAGES` by the
device tree. It is signed with the platform key and runs with the system
uid, so that it can set the property; the device sepolicy allows
`system_app` to do so.

## Credits

- The LineageOS Project and AOSP: SettingsLib (collapsing toolbar activity,
  selector and footer preferences, Settings theme) and the Settings tile
  injection used here.
- Google, Material icon "speed" (Apache 2.0): the icon of the Settings entry
  and of the tile.

## License

Apache License 2.0.
