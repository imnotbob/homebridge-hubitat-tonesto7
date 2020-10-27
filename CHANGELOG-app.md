## v2.0.0 Changes
- [NEW] **_Important NOTICE:_**
- **Due to the changes in the plugin API you can not directly update the plugin from v1, you will need to add as a new accessory and setup your devices/automations/scenes again.
  On a positive note, you can use the same Hubitat App instance as long as you update to the latest code.**

- [NEW] Ported v2.0 app and plugin from stable SmartThings version
- [NEW] Added duplicate device detection cleanups so Homekit doesn't try to create duplicate devices and throw an error.
- [NEW] Added support for bringing acceleration sensors into homekit as motion sensors.
- [NEW] Fixed new version info when using beta version of plugin.
- [NEW] Added garage door, thermostat inputs to define device types
- [NEW] Added a Device Event and Command history page to review events and commands sent and received by the plugin.
- [NEW] Added a new device data input where you can select a device and see all available attributes, capabilities, commands, and the last 30 events.
- [NEW] Added new capability filter options.
- [UPDATE] Reworked and cleaned up the UI so it's now more organized and easier to follow.
- [UPDATE] Optimized the command/event streaming system to perform faster and more reliably.
- [UPDATE] Added support for passing the pressed button number when provided.
- [UPDATE] The app can now optionally validate the appId on all local commands made to hubitat app so if you have more than one instance of the homebridge smartapp it doesn't start sending events to wrong plugin.
- [UPDATE] Cleaned up some of the unnecessary attributes from the subscription logic.
- [UPDATE] Modified the device event subscription process to reduce timeouts.
- [FIX] Fixed issue with new Garage and Thermostat define type inputs from actually bringing in the devices.
- [FIX] Minor tweaks to support shades fixes in the plugin.
- [FIX] Other minor bugfixes and optimizations.
- [FIX] Refactored the accessToken logic to be more consistent.
- [FIX] Tons of other bugfixes, optimizations, and cleanups from v1
- [REMOVE] Support for Energy and Power capabilities removed (for now).
