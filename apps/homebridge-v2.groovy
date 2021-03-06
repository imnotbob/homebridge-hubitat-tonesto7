/**
 *  Homebridge Hubitat Interface
 *  App footer inspired from Hubitat Package Manager (Thanks @dman2306)
 *
 *  Copyright 2018, 2019, 2020 Anthony Santilli
 */

import groovy.transform.Field

definition(
    name: "Homebridge v2",
    namespace: "tonesto7",
    author: "Anthony Santilli",
    description: "Provides the API interface between Homebridge (HomeKit) and ${platformFLD}",
    category: "My Apps",
    iconUrl:   "https://raw.githubusercontent.com/tonesto7/homebridge-hubitat-tonesto7/master/images/hb_tonesto7.png",
    iconX2Url: "https://raw.githubusercontent.com/tonesto7/homebridge-hubitat-tonesto7/master/images/hb_tonesto7.png",
    iconX3Url: "https://raw.githubusercontent.com/tonesto7/homebridge-hubitat-tonesto7/master/images/hb_tonesto7.png",
    importUrl: "https://raw.githubusercontent.com/tonesto7/homebridge-hubitat-tonesto7/master/apps/homebridge-v2.groovy",
    oauth: true)

preferences {
    page(name: "startPage")
    page(name: "mainPage")
    page(name: "deviceSelectPage")
    page(name: "changeLogPage")
    page(name: "capFilterPage")
    page(name: "developmentPage")
    page(name: "pluginConfigPage")
    page(name: "donationPage")
    page(name: "historyPage")
    page(name: "deviceDebugPage")
    page(name: "settingsPage")
    page(name: "confirmPage")
}

// STATICALLY DEFINED VARIABLES
@Field static final String appVersionFLD  = "2.1.4"
@Field static final String appModifiedFLD = "11-17-2020"
@Field static final String branchFLD      = "master"
@Field static final String platformFLD    = "Hubitat"
@Field static final String pluginNameFLD  = "Hubitat-v2"
@Field static final Boolean devModeFLD    = true
@Field static final Map minVersionsFLD = [plugin: 213]
@Field static final String sNULL   = (String) null
@Field static final List   lNULL   = (List) null
@Field static final String sBLANK  = ''
@Field static final String sBULLET = '\u2022'

// IN-MEMORY VARIABLES (Cleared only on HUB REBOOT)

@Field static final Map ignoreListFLD =  [
    commands: ["indicatorWhenOn", "indicatorWhenOff", "ping", "refresh", "indicatorNever", "configure", "poll", "reset"],
    attributes: [
        'DeviceWatch-Enroll', 'DeviceWatch-Status', "checkInterval", "LchildVer", "FchildVer", "LchildCurr", "FchildCurr", "lightStatus", "lastFanMode", "lightLevel",
        "coolingSetpointRange", "heatingSetpointRange", "thermostatSetpointRange", "power", "energy"
    ],
    evt_attributes: [
        'DeviceWatch-DeviceStatus', "DeviceWatch-Enroll", 'checkInterval', 'devTypeVer', 'dayPowerAvg', 'apiStatus', 'yearCost', 'yearUsage','monthUsage', 'monthEst', 'weekCost', 'todayUsage',
        'maxCodeLength', 'maxCodes', 'readingUpdated', 'maxEnergyReading', 'monthCost', 'maxPowerReading', 'minPowerReading', 'monthCost', 'weekUsage', 'minEnergyReading',
        'codeReport', 'scanCodes', 'verticalAccuracy', 'horizontalAccuracyMetric', 'altitudeMetric', 'latitude', 'distanceMetric', 'closestPlaceDistanceMetric',
        'closestPlaceDistance', 'leavingPlace', 'currentPlace', 'codeChanged', 'codeLength', 'lockCodes', 'healthStatus', 'horizontalAccuracy', 'bearing', 'speedMetric',
        'speed', 'verticalAccuracyMetric', 'altitude', 'indicatorStatus', 'todayCost', 'longitude', 'distance', 'previousPlace','closestPlace', 'places', 'minCodeLength',
        'arrivingAtPlace', 'lastUpdatedDt', 'scheduleType', 'zoneStartDate', 'zoneElapsed', 'zoneDuration', 'watering', 'eventTime', 'eventSummary', 'endOffset', 'startOffset',
        'closeTime', 'endMsgTime', 'endMsg', 'openTime', 'startMsgTime', 'startMsg', 'calName', "deleteInfo", "eventTitle", "floor", "sleeping", "ultravioletIndex", "threeAxis",
        "LchildVer", "FchildVer", "LchildCurr", "FchildCurr", "lightStatus", "lastFanMode", "lightLevel", "coolingSetpointRange", "heatingSetpointRange", "thermostatSetpointRange",
        "colorName", "locationForURL", "location", "offsetNotify", "lastActivity", "firmware", "groups", "lastEvent", "colorMode", "RGB", "power", "energy",
        "batteryType", "deviceType", "driverVersionInternal", "outletSwitchable", "outputVoltageNominal", "deviceModel", "driverVersion", "status", "deviceModel", "deviceManufacturer",
        "deviceFirmware", "outletDescription", "driverName", "batteryRuntimeSecs", "outputFrequency", "outputFrequencyNominal", "driverVersionData", "deviceNominalPower", "load"
    ],
    capabilities: ["HealthCheck", "Indicator", "WindowShadePreset", "ChangeLevel", "Outlet", "HealthCheck", "UltravioletIndex", "ColorMode", "VoltageMeasurement", "PowerMeter", "EnergyMeter"]
]

def startPage() {
    if(!getAccessToken()) { return dynamicPage(name: "mainPage", install: false, uninstall: true) { section() { paragraph title: "OAuth Error", "OAuth is not Enabled for ${app?.getName()}!.\n\nPlease click remove and Enable Oauth under the SmartApp App Settings in the IDE", required: true, state: null } } }
    else {
        if(!state?.installData) { state?.installData = [initVer: appVersionFLD, dt: getDtNow().toString(), updatedDt: getDtNow().toString(), shownDonation: false] }
        checkVersionData(true)
        if(showChgLogOk()) { return changeLogPage() }
        if(showDonationOk()) { return donationPage() }
        return mainPage()
    }
}

def mainPage() {
    Boolean isInst = (state?.isInstalled == true)
    return dynamicPage(name: "mainPage", nextPage: (isInst ? "confirmPage" : ""), install: !isInst, uninstall: true) {
        appInfoSect()
        section(sectTS("Device Configuration:", null, true)) {
            Boolean conf = (lightList || buttonList || fanList || fan3SpdList || fan4SpdList || speakerList || shadesList || garageList || tstatList || tstatHeatList) || (sensorList || switchList || deviceList) || (modeList || routineList)
            Integer fansize = (fanList?.size() ?: 0) + (fan3SpdList?.size() ?: 0) + (fan4SpdList?.size() ?: 0)
            String desc = """<small style="color:gray;">Tap to select devices...</small>"""
            Integer devCnt = getDeviceCnt()
            if(conf) {
                desc = ""
                desc += lightList ? """<small style="color:#2784D9;"><b>Light${lightList?.size() > 1 ? "s" : ""}</b> (${lightList?.size()})</small><br>""" : ""
                desc += buttonList ? """<small style="color:#2784D9;"><b>Button${buttonList?.size() > 1 ? "s" : ""}</b> (${buttonList?.size()})</small><br>""" : ""
                desc += (fanList || fan3SpdList || fan4SpdList) ? """<small style="color:#2784D9;"><b>Fan Device${fansize > 1 ? "s" : ""}</b> (${fansize})</small><br>""" : ""
                desc += speakerList ? """<small style="color:#2784D9;"><b>Speaker${speakerList?.size() > 1 ? "s" : ""}</b> (${speakerList?.size()})</small><br>""" : ""
                desc += shadesList ? """<small style="color:#2784D9;"><b>Shade${shadesList?.size() > 1 ? "s" : ""}</b> (${shadesList?.size()})</small><br>""" : ""
                desc += garageList ? """<small style="color:#2784D9;"><b>Garage Door${garageList?.size() > 1 ? "s" : ""}</b> (${garageList?.size()})</small><br>""" : ""
                desc += tstatList ? """<small style="color:#2784D9;"><b>Thermostat${tstatList?.size() > 1 ? "s" : ""}</b> (${tstatList?.size()})</small><br>""" : ""
                desc += tstatFanList ? """<small style="color:#2784D9;"><b>Thermostat${tstatFanList?.size() > 1 ? "s" : ""} w/Fan</b> (${tstatFanList?.size()})</small><br>""" : ""
                desc += tstatHeatList ? """<small style="color:#2784D9;"><b>Thermostat Heat${tstatHeatList?.size() > 1 ? "s" : ""}</b> (${tstatHeatList?.size()})</small><br>""" : ""
                desc += sensorList ? """<small style="color:#2784D9;"><b>Sensor${sensorList?.size() > 1 ? "s" : ""}</b> (${sensorList?.size()})</small><br>""" : ""
                desc += switchList ? """<small style="color:#2784D9;"><b>Switch${switchList?.size() > 1 ? "es" : ""}</b> (${switchList?.size()})</small><br>""" : ""
                desc += deviceList ? """<small style="color:#2784D9;"><b>Other${deviceList?.size() > 1 ? "s" : ""}</b> (${deviceList?.size()})</small><br>""" : ""
                desc += modeList ? """<small style="color:#2784D9;"><b>Mode${modeList?.size() > 1 ? "s" : ""}</b> (${modeList?.size()})</small><br>""" : ""
                desc += routineList ? """<small style="color:#2784D9;"><b>Routine${routineList?.size() > 1 ? "s" : ""}</b> (${routineList?.size()})</small><br>""" : ""
                desc += addSecurityDevice ? """<small style="color:#2784D9;"><b>HSM</b> (1)</small><br>""" : ""
                desc += """<hr style='background-color:#2784D9; height: 1px; width: 150px; border: 0;'><small style="color:#2784D9;"><b>Devices Selected:</b> (${devCnt})</small><br>"""
                desc += (devCnt > 149) ? """<br><medium style="color:red;"><b>NOTICE:</b> Homebridge only allows 149 Devices per HomeKit Bridge!!!</medium><br>""" : ""
                desc += """<br><small style="color:#2784D9;">Tap to modify...</small>"""
            }
            href "deviceSelectPage", title: inputTS("Device Selection", getAppImg("devices2", true)), required: false, image: getAppImg("devices2"), state: (conf ? "complete" : null), description: desc
        }

        inputDupeValidation()

        section(sectTS("Capability Filtering:", null, true)) {
            Boolean conf = (
                removeAcceleration || removeBattery || removeButton || removeContact || removeEnergy || removeHumidity || removeIlluminance || removeLevel || removeLock || removeMotion ||
                removePower || removePresence || removeSwitch || removeTamper || removeTemp || removeValve
            )
            href "capFilterPage", title: inputTS("Filter out capabilities from your devices", getAppImg("filter", true)), required: false, state: (conf ? "complete" : null), description: (conf ? "Tap to modify..." : "Tap to configure")
        }

        section(sectTS("Location Options:", null, true)) {
            input "addSecurityDevice", "bool", title: inputTS("Allow ${getAlarmSystemName()} Control in HomeKit?", getAppImg("alarm_home", true)), required: false, defaultValue: true, submitOnChange: true           
        }

        section(sectTS("HomeBridge Plugin Config:", null, true)) {
            href "pluginConfigPage", style: "embedded", required: false, title: inputTS("View Generated Config for HomeBridge", getAppImg("info", true)), description: "Tap to view...", state: "complete"
        }

        section(sectTS("History Data and Device Debug:", null, true)) {
            href "historyPage", title: inputTS("View Command and Event History", getAppImg("backup", true)), description: "Tap to view...", state: "complete"
            href "deviceDebugPage", title: inputTS("View Device Debug Data", getAppImg("debug", true)), description: "Tap to view...", state: "complete"
        }

        section(sectTS("App Preferences:", null, true)) {
            def sDesc = getSetDesc()
            href "settingsPage", title: inputTS("App Settings", getAppImg("settings", true)), description: sDesc, state: (sDesc?.endsWith("modify...") ? "complete" : null), required: false
            href "changeLogPage", title: inputTS("View Changelog", getAppImg("change_log", true)), description: "Tap to view..."
            label title: inputTS("Label this Instance (optional)", getAppImg("name_tag", true)), description: "Rename this App", defaultValue: app?.name, required: false
        }

        if(devMode()) {
            section(sectTS("Dev Mode Options", null, true)) {
                input "sendViaNgrok", "bool", title: inputTS("Communicate with Plugin via Ngrok Http?", getAppImg("command", true)), defaultValue: false, submitOnChange: true
                if(sendViaNgrok) { input "ngrokHttpUrl", "text", title: inputTS("Enter the ngrok code from the url"), required: true, submitOnChange: true }
            }
            section(sectTS("Other Settings:", null, true)) {
                input "restartService", "bool", title: inputTS("Restart Homebridge plugin when you press Save?", getAppImg("reset", true)), required: false, defaultValue: false, submitOnChange: true
            }
        }
        clearTestDeviceItems()
    }
}

def pluginConfigPage() {
    return dynamicPage(name: "pluginConfigPage", title: "", install: false, uninstall: false) {
        section(sectTS("Plugin Communication Options:", null, true)) {
            input "use_cloud_endpoint", "bool", title: inputTS("Communicate with Plugin Using Cloud Endpoint?", getAppImg("command", true)), required: false, defaultValue: false, submitOnChange: true
            input "validate_token",     "bool", title: inputTS("Validate AppID & Token for All Communications?", getAppImg("command", true)), required: false, defaultValue: false, submitOnChange: true
            input "temp_unit",          "enum", title: inputTS("Temperature Unit?", getAppImg("temp_unit", true)), required: true, defaultValue: location?.temperatureScale, options: ["F":"Fahrenheit", "C":"Celcius"], submitOnChange: true
        }
        
        // section(sectTS("Plugin Device Options:", null, true)) {
        //     input "round_up_99", "bool", title: inputTS("Round Up Devices with 9?", getAppImg("command", true)), required: false, defaultValue: false, submitOnChange: true
        //     input "temp_unit",          "enum", title: inputTS("Temperature Unit?", getAppImg("temp_unit", true)), required: true, defaultValue: location?.temperatureScale, options: ["F":"Fahrenheit", "C":"Celcius"], submitOnChange: true
        //     input "validate_token",     "bool", title: inputTS("Validate AppID & Token for All Communications?", getAppImg("command", true)), required: false, defaultValue: false, submitOnChange: true
        // }

        section() {
            paragraph paraTS("Generated HomeBridge Plugin Platform Config", null, true, "#2784D9")
            paragraph """<textarea rows=21 class='mdl-textfield' readonly='true'>${renderConfig()}</textarea>"""
        }
    }
}

def deviceValidationErrors() {
    /*
        NOTE: Define what we require to determine the thermostat is a thermostat so we can support devices like Flair which are custom heat-only thermostats.
    */
    Map reqs = [
        tstat: [ c:["Thermostat Operating State"], a: [r: ["thermostatOperatingState"], o: ["heatingSetpoint", "coolingSetpoint"]] ],
        tstat_heat: [
            c: ["Thermostat Operating State"],
            a: [
                r: ["thermostatOperatingState", "heatingSetpoint"],
                o: []
            ]
        ]
    ]

    // if(tstatHeatList || tstatList || tstatFanList) {}
    return reqs
}

def deviceSelectPage() {
    return dynamicPage(name: "deviceSelectPage", title: "", install: false, uninstall: false) {
        section(sectTS("Define Specific Categories:", null, true)) {
            paragraph paraTS("NOTE: Please do not select a device here and then again in another input below.")
            paragraph paraTS("Each category below will adjust the device attributes to make sure they are recognized as the desired device type under HomeKit", null, false, "#2784D9"), state: "complete"
            input "lightList", "capability.switch", title: inputTS("Lights: (${lightList ? lightList?.size() : 0} Selected)", getAppImg("light_on", true)), multiple: true, submitOnChange: true, required: false
            input "garageList", "capability.garageDoorControl", title: inputTS("Garage Doors: (${garageList ? garageList?.size() : 0} Selected)", getAppImg("garage_door", true)), multiple: true, submitOnChange: true, required: false
            input "buttonList", "capability.button", title: inputTS("Buttons: (${buttonList ? buttonList?.size() : 0} Selected)", getAppImg("button", true)), multiple: true, submitOnChange: true, required: false
            input "speakerList", "capability.switch", title: inputTS("Speakers: (${speakerList ? speakerList?.size() : 0} Selected)", getAppImg("media_player", true)), multiple: true, submitOnChange: true, required: false
            input "shadesList", "capability.windowShade", title: inputTS("Window Shades: (${shadesList ? shadesList?.size() : 0} Selected)", getAppImg("window_shade", true)), multiple: true, submitOnChange: true, required: false
        }

        section(sectTS("Fans:", null, true)) {
            input "fanList", "capability.switch", title: inputTS("Fans: (${fanList ? fanList?.size() : 0} Selected)", getAppImg("fan_on", true)), multiple: true, submitOnChange: true, required: false
            input "fan3SpdList", "capability.switch", title: inputTS("Fans (3 Speeds): (${fan3SpdList ? fan3SpdList?.size() : 0} Selected)", getAppImg("fan_on", true)), multiple: true, submitOnChange: true, required: false
            input "fan4SpdList", "capability.switch", title: inputTS("Fans (4 Speeds): (${fan4SpdList ? fan4SpdList?.size() : 0} Selected)", getAppImg("fan_on", true)), multiple: true, submitOnChange: true, required: false
        }

        section(sectTS("Thermostats:", null, true)) {
            input "tstatList", "capability.thermostat", title: inputTS("Thermostats: (${tstatList ? tstatList?.size() : 0} Selected)", getAppImg("thermostat", true)), multiple: true, submitOnChange: true, required: false
            input "tstatFanList", "capability.thermostat", title: inputTS("Thermostats + Fan: (${tstatFanList ? tstatFanList?.size() : 0} Selected)", getAppImg("thermostat", true)), multiple: true, submitOnChange: true, required: false
            input "tstatHeatList", "capability.thermostat", title: inputTS("Heat Only Thermostats: (${tstatHeatList ? tstatHeatList?.size() : 0} Selected)", getAppImg("thermostat", true)), multiple: true, submitOnChange: true, required: false
        }

        section(sectTS("All Other Devices:", null, true)) {
            input "sensorList", "capability.sensor", title: inputTS("Sensors: (${sensorList ? sensorList?.size() : 0} Selected)", getAppImg("sensors", true)), multiple: true, submitOnChange: true, required: false
            input "switchList", "capability.switch", title: inputTS("Switches: (${switchList ? switchList?.size() : 0} Selected)", getAppImg("switch", true)), multiple: true, submitOnChange: true, required: false
            input "deviceList", "capability.*", title: inputTS("Others: (${deviceList ? deviceList?.size() : 0} Selected)", getAppImg("devices2", true)), multiple: true, submitOnChange: true, required: false

        }

        section(sectTS("Create Devices for Modes in HomeKit?", null, true)) {
            paragraph title: paraTS("What are these for?"), "A virtual switch will be created for each mode in HomeKit.\nThe switch will be ON when that mode is active.", state: "complete"
            def modes = location?.getModes()?.sort{it?.name}?.collect { [(it?.id):it?.name] }
            input "modeList", "enum", title: inputTS("Create Devices for these Modes", getAppImg("mode", true)), required: false, multiple: true, options: modes, submitOnChange: true
        }

        inputDupeValidation()
    }
}

def settingsPage() {
    return dynamicPage(name: "settingsPage", title: "", install: false, uninstall: false) {
        section(sectTS("Logging:", null, true)) {
            input "showEventLogs", "bool", title: inputTS("Show Device/Location Events?", getAppImg("debug", true)), required: false, defaultValue: true, submitOnChange: true
            input "showDebugLogs", "bool", title: inputTS("Show Detailed Logging?", getAppImg("debug", true)), required: false, defaultValue: false, submitOnChange: true
        }
        section(sectTS("Security:", null, true)) {
            paragraph paraTS("This will allow you to clear you existing app accessToken and force a new one to be created.\nYou will need to update the homebridge config with the new token in order to continue using hubitat with HomeKit", null, false)
            input "resetAppToken", "bool", title: inputTS("Revoke and Recreate App Access Token?", getAppImg("reset", true)), defaultValue: false, submitOnChange: true
            if(settings?.resetAppToken) { settingUpdate("resetAppToken", "false", "bool"); resetAppToken() }
        }
        // section(sectTS("HTTP Requests:", null, true)) {
        //     input "use_sync_http", "bool", title: inputTS("Slow Commands? Use Synchronous HTTP?", getAppImg("command", true)), required: false, defaultValue: true, submitOnChange: true
        // }
    }
}

private resetAppToken() {
    logWarn("resetAppToken | Current Access Token Removed...")
    state.remove("accessToken")
    if(getAccessToken()) {
        logInfo("resetAppToken | New Access Token Created...")
    }
}

private resetCapFilters() {
    List items = settings?.each?.findAll { it?.key?.startsWith("remove") }?.collect { it?.key as String }
    if(items?.size()) {
        items?.each { item->
            settingRemove(item)
        }
    }
}

private inputDupeValidation() {
    Map clnUp = [d: [:], o: [:]]
    Map items = [
        d: ["fanList": "Fans", "fan3SpdList": "Fans (3-Speed)", "fan4SpdList": "Fans (4-Speed)", "buttonList": "Buttons", "lightList": "Lights", "shadesList": "Window Shadse", "speakerList": "Speakers",
            "garageList": "Garage Doors", "tstatList": "Thermostat", "tstatFanList": "Themostat + Fan", "tstatHeatList": "Thermostat (Heat Only)"
        ],
        o: ["deviceList": "Other", "sensorList": "Sensor", "switchList": "Switch"]
    ]
    items?.d?.each { k, v->
        List priItems = (settings?."${k}"?.size()) ? settings?."${k}"?.collect { it?.getLabel() } : null
        if(priItems) {
            items?.d?.each { k2, v2->
                List secItems = (settings?."${k2}"?.size()) ? settings?."${k2}"?.collect { it?.getLabel() } : null
                if(k != k2 && secItems) {
                    secItems?.retainAll(priItems)
                    if(secItems?.size()) {
                        clnUp?.d[k2] = clnUp?.d[k2] ?: []
                        clnUp?.d[k2] = (clnUp?.d[k2] + secItems)?.unique()
                    }
                }
            }

            items?.o?.each { k2, v2->
                List secItems = (settings?."${k2}"?.size()) ? settings?."${k2}"?.collect { it?.getLabel() } : null
                if(secItems) {
                    secItems?.retainAll(priItems)
                    if(secItems?.size()) {
                        clnUp?.o[k2] = clnUp?.o[k2] ?: []
                        clnUp?.o[k2] = (clnUp?.o[k2] + secItems)?.unique()
                    }
                }
            }
        }
    }
    String out = ""
    Boolean show = false
    Boolean first = true
    if(clnUp?.d?.size()) {
        show=true
        clnUp?.d?.each { k,v->
            out += "${first ? "" : "\n"}${items?.d[k]}:\n "
            out += v?.join("\n ") + "\n"
            first = false
        }
    }
    if(clnUp?.o?.size()) {
        show=true
        clnUp?.o?.each { k,v->
            out += "${first ? "" : "\n"}${items?.o[k]}:\n "
            out += v?.join("\n ") + "\n"
            first = false
        }
    }
    if(show && out) {
        section(sectTS("Duplicate Device Validation:")) {
            paragraph title: paraTS("Duplicate Devices Found in these Inputs:"), paraTS(out + "\nPlease remove these duplicate items!", null, false, "red"), required: true, state: null
        }
    }
}

String getSetDesc() {
    def s = []
    if(settings?.showEventLogs == true) s?.push("\u2022 Device Event Logs")
    if(settings?.showDebugLogs == true) s?.push("\u2022 Debug Logging")
    return s?.size() ? "${s?.join("\n")}\n\nTap to modify..." : "Tap to configure..."
}

def historyPage() {
    return dynamicPage(name: "historyPage", title: "", install: false, uninstall: false) {
        List cHist = getCmdHistory()?.sort {it?.dt}?.reverse()
        List eHist = getEvtHistory()?.sort {it?.dt}?.reverse()
        section() {
            paragraph paraTS("This history is only stored in memory.  It is erased after every code update and a Hub reboot.", getAppImg("info", true), false, "#2784D9"), state: "complete"
        }
        section(sectTS("Last (${cHist?.size()}) Commands Received From HomeKit:", null, true)) {
            if(cHist?.size()) {
                cHist?.each { c-> paragraph paraTS(" \u2022 <b>Device:</b> ${c?.data?.device}\n \u2022 <b>Command:</b> (${c?.data?.cmd})${c?.data?.value1 ? "\n \u2022 <b>Value1:</b> (${c?.data?.value1})" : ""}${c?.data?.value2 ? "\n \u2022 <b>Value2:</b> (${c?.data?.value2})" : ""}\n \u2022 <b>Date:</b> ${c?.dt}${c?.data?.execTime ? "\n \u2022 <b>ExecTime: </b> (${c?.data?.execTime}ms)" : ""}", null, false, "#2784D9"), state: "complete" }
            } else { paragraph paraTS("No Command History Found...", null, false) }
        }
        section(sectTS("Last (${eHist?.size()}) Events Sent to HomeKit:", null, true)) {
            if(eHist?.size()) {
                eHist?.each { h-> paragraph title: paraTS(h?.dt), paraTS(" \u2022 <b>Device</b>: ${h?.data?.device}\n \u2022 <b>Event:</b> (${h?.data?.name})${h?.data?.value ? "\n \u2022 <b>Value:</b> (${h?.data?.value})" : ""}\n \u2022 <b>Date:</b> ${h?.dt}${h?.data?.execTime ? "\n \u2022 <b>ExecTime:</b> (${h?.data?.execTime}ms)" : ""}", null, false, "#2784D9"), state: "complete" }
            } else {paragraph paraTS("No Event History Found...", null, false) }
        }
    }
}

def capFilterPage() {
    return dynamicPage(name: "capFilterPage", title: "Capability Filtering", install: false, uninstall: false) {
        section(sectTS("Restrict Temp Device Creation", null, true)) {
            input "noTemp", "bool", title: inputTS("Remove Temperature from All Contacts and Water Sensors?", getAppImg("temperature", true)), required: false, defaultValue: false, submitOnChange: true
            if(settings?.noTemp) {
                input "sensorAllowTemp", "capability.sensor", title: inputTS("Allow Temps on these sensors", getAppImg("temperature", true)), multiple: true, submitOnChange: true, required: false
            }
        }
        section(sectTS("Remove Capabilities from Devices", null, true)) {
            paragraph paraTS("These inputs allow you to remove certain capabilities from a device preventing the creation of unwanted devices under HomeKit", null, false, "#2874D9")
            input "removeAcceleration", "capability.accelerationSensor", title: inputTS("Remove Acceleration from these Devices", getAppImg("acceleration", true)), multiple: true, submitOnChange: true, required: false
            input "removeBattery", "capability.battery", title: inputTS("Remove Battery from these Devices", getAppImg("battery", true)), multiple: true, submitOnChange: true, required: false
            input "removeButton", "capability.button", title: inputTS("Remove Buttons from these Devices", getAppImg("button", true)), multiple: true, submitOnChange: true, required: false
            input "removeContact", "capability.contactSensor", title: inputTS("Remove Contact from these Devices", getAppImg("contact", true)), multiple: true, submitOnChange: true, required: false
            // input "removeEnergy", "capability.energyMeter", title: inputTS("Remove Energy Meter from these Devices", getAppImg("power", true)), multiple: true, submitOnChange: true, required: false
            input "removeHumidity", "capability.relativeHumidityMeasurement", title: inputTS("Remove Humidity from these Devices", getAppImg("humidity", true)), multiple: true, submitOnChange: true, required: false
            input "removeIlluminance", "capability.illuminanceMeasurement", title: inputTS("Remove Illuminance from these Devices", getAppImg("illuminance", true)), multiple: true, submitOnChange: true, required: false
            input "removeLevel", "capability.switchLevel", title: inputTS("Remove Level from these Devices", getAppImg("speed_knob", true)), multiple: true, submitOnChange: true, required: false
            input "removeLock", "capability.lock", title: inputTS("Remove Lock from these Devices", getAppImg("speed_knob", true)), multiple: true, submitOnChange: true, required: false
            input "removeMotion", "capability.motionSensor", title: inputTS("Remove Motion from these Devices", getAppImg("motion", true)), multiple: true, submitOnChange: true, required: false
            // input "removePower", "capability.powerMeter", title: inputTS("Remove Power Meter from these Devices", getAppImg("power", true)), multiple: true, submitOnChange: true, required: false
            input "removePresence", "capability.presenceSensor", title: inputTS("Remove Presence from these Devices", getAppImg("presence", true)), multiple: true, submitOnChange: true, required: false
            input "removeSwitch", "capability.switch", title: inputTS("Remove Switch from these Devices", getAppImg("switch", true)), multiple: true, submitOnChange: true, required: false
            input "removeTamper", "capability.tamperAlert", title: inputTS("Remove Tamper from these Devices", getAppImg("tamper", true)), multiple: true, submitOnChange: true, required: false
            input "removeTemp", "capability.temperatureMeasurement", title: inputTS("Remove Temperature from these Devices", getAppImg("temperature", true)), multiple: true, submitOnChange: true, required: false
            input "removeValve", "capability.valve", title: inputTS("Remove Valve from these Devices", getAppImg("valve", true)), multiple: true, submitOnChange: true, required: false
        }
        section(sectTS("Reset Selected Filters:", null, true), hideable: true, hidden: true) {
            input "resetCapFilters", "bool", title: inputTS("Clear All Selected Filters?", getAppImg("reset", true)), required: false, defaultValue: false, submitOnChange: true
            if(settings?.resetCapFilters) { settingUpdate("resetCapFilters", "false", "bool"); resetCapFilters() }
        }
    }
}

def donationPage() {
    return dynamicPage(name: "donationPage", title: "", nextPage: "mainPage", install: false, uninstall: false) {
        section("") {
            def str = ""
            str += "Hello User, \n\nPlease forgive the interuption but it's been 30 days since you installed/updated this SmartApp and I wanted to present you with this one time reminder that donations are accepted (We do not require them)."
            str += "\n\nIf you have been enjoying the software and devices please remember that we have spent thousand's of hours of our spare time working on features and stability for those applications and devices."
            str += "\n\nIf you have already donated, thank you very much for your support!"

            str += "\n\nIf you are just not interested or have already donated please ignore this message and toggle the setting below"
            str += "\n\nThanks again for using Homebridge Hubitat"
            paragraph paraTS(str, null, "red"), required: true, state: null
            input "sentDonation", "bool", title: inputTS("Already Donated?"), defaultValue: false, submitOnChange: true
            href url: textDonateLink(), style: "external", required: false, title: inputTS("Donations", getAppImg("donata", true)), description: "Tap to open in browser", state: "complete"
        }
        updInstData("shownDonation", true)
    }
}

def confirmPage() {
    return dynamicPage(name: "confirmPage", title: "", install: true, uninstall:true) {
        section(sectH3TS("Confirmation Page", "A plugin restart is no longer required to apply device changes in this app to HomeKit.<br><br>Once you press <b>Done</b> the Homebridge plugin will refresh your device changes after 15-20 seconds.", getAppImg("info", true), "black")) { }
        appFooter()
    }
}

def deviceDebugPage() {
    return dynamicPage(name: "deviceDebugPage", title: "", install: false, uninstall: false) {
        section(sectTS("View All Device Data Sent to HomeBridge:", null, true)) {
            href url: getAppEndpointUrl("alldevices"), style: "embedded", required: false, title: inputTS("View Device Data Sent to Homebridge...", getAppImg("info", true)), description: "", disabled: true
        }

        section(sectTS("View Individual Device Data:", null, true)) {
            paragraph paraTS("Have a device that's not working under homekit like you want?\nSelect a device from one of the inputs below and it will show you all data about the device.", getAppImg("info", true), false, "#2784D9"), state: "complete"
            if(!debug_switch && !debug_other && !debug_garage && !debug_tstat)
                input "debug_sensor", "capability.sensor", title:  inputTS("Sensors: ", getAppImg("sensors", true)), multiple: false, submitOnChange: true, required: false
            if(!debug_sensor && !debug_other && !debug_garage && !debug_tstat)
                input "debug_switch", "capability.actuator", title: inputTS("Switches: ", getAppImg("switch", true)) , multiple: false, submitOnChange: true, required: false
            if(!debug_switch && !debug_sensor && !debug_garage && !debug_tstat)
                input "debug_other", "capability.*", title: inputTS("Others Devices: ", getAppImg("devices2", true)), multiple: false, submitOnChange: true, required: false
            if(!debug_sensor && !debug_other && !debug_switch && !debug_tstat)
                input "debug_garage", "capability.garageDoorControl", title: inputTS("Garage Doors: ", getAppImg("garage_door", true)), multiple: false, submitOnChange: true, required: false
            if(!debug_sensor && !debug_other && !debug_switch && !debug_garage)
                input "debug_tstat", "capability.thermostat", title: inputTS("Thermostats: ", getAppImg("thermostat", true)), multiple: false, submitOnChange: true, required: false
            if(debug_other || debug_sensor || debug_switch || debug_garage || debug_tstat) {
                href url: getAppEndpointUrl("deviceDebug"), style: "embedded", required: false, title: inputTS("Tap here to view Device Data...", getAppImg("info", true)), description: "", state: "complete"
            }
        }
    }
}

public void clearTestDeviceItems() {
    settingRemove("debug_sensor")
    settingRemove("debug_switch")
    settingRemove("debug_other")
    settingRemove("debug_garage")
    settingRemove("debug_tstat")
}

private viewDeviceDebug() {
    def sDev = null;
    if(debug_other) sDev = debug_other
    if(debug_sensor) sDev = debug_sensor
    if(debug_switch) sDev = debug_switch
    if(debug_garage) sDev = debug_garage
    if(debug_tstat)  sDev = debug_tstat
    def json = new groovy.json.JsonOutput().toJson(getDeviceDebugMap(sDev))
    def jsonStr = new groovy.json.JsonOutput().prettyPrint(json)
    render contentType: "application/json", data: jsonStr
}

private Map getDeviceDebugMap(dev) {
    Map r = [result: "No Data Returned"]
    if(dev) {
        try {
            r = [:]
            r?.name = dev?.displayName?.toString()?.replaceAll("[#\$()!%&@^']", "");
            r?.basename = dev?.getName();
            r?.deviceid = dev?.getId();
            r?.status = dev?.getStatus();
            r?.manufacturer = dev?.manufacturerName ?: "Unknown";
            r?.model = dev?.modelName ?: dev?.getTypeName();
            r?.deviceNetworkId = dev?.getDeviceNetworkId();
            r?.lastActivity = dev?.getLastActivity() ?: null;
            r?.capabilities = dev?.capabilities?.collect { it?.name as String }?.unique()?.sort() ?: [];
            r?.commands = dev?.supportedCommands?.collect { it?.name as String }?.unique()?.sort() ?: [];
            r?.customflags = getDeviceFlags(dev) ?: [];
            r?.attributes = [:];
            r?.eventHistory = dev?.eventsSince(new Date() - 1, [max: 20])?.collect { "${it?.date} | [${it?.name}] | (${it?.value}${it?.unit ? " ${it?.unit}" : ""})" };
            dev?.supportedAttributes?.collect { it?.name as String }?.unique()?.sort()?.each { r?.attributes[it] = dev?.currentValue(it as String); };
        } catch(ex) {
            logError("Error while generating device data: ", ex);
        }
    }
    return r
}

private Integer getDeviceCnt(phyOnly=false) {
    List devices = []
    List items = deviceSettingKeys()?.collect { it?.key as String }
    items?.each { item -> if(settings[item]?.size() > 0) devices = devices + settings[item] }
    if(!phyOnly) {
        ["modeList", "routineList"]?.each { item->
            if(settings[item]?.size() > 0) devices = devices + settings[item]
        }
    }
    def dSize = devices?.unique()?.size() ?: 0
    if(settings?.addSecurityDevice) dSize = dSize + 1
    return dSize
}

def installed() {
    logDebug("${app.name} | installed() has been called...")
    state?.isInstalled = true
    state?.installData = [initVer: appVersionFLD, dt: getDtNow().toString(), updatedDt: "Not Set", showDonation: false, shownChgLog: true]
    initialize()
}

def updated() {
    log.debug("${app.name} | updated() has been called...")
    state?.isInstalled = true
    if(!state?.installData) { state?.installData = [initVer: appVersionFLD, dt: getDtNow().toString(), updatedDt: getDtNow().toString(), shownDonation: false] }
    unsubscribe()
    stateCleanup()
    initialize()
}

def initialize() {
    if(getAccessToken()) {
        subscribeToEvts()
        runEvery5Minutes("healthCheck")
    } else { logError("initialize error: Unable to get or generate smartapp access token") }
}

def getAccessToken() {
    try {
        if(!atomicState?.accessToken) {
            atomicState?.accessToken = createAccessToken();
            logWarn("SmartApp Access Token Missing... Generating New Token!!!")
            return true;
        }
        return true
    } catch (ex) {
        def msg = "Error: OAuth is not Enabled for ${appName()}!. Please click remove and Enable Oauth under the SmartApp App Settings in the IDE"
        logError("getAccessToken Exception: ${msg}")
        return false
    }
}

private void subscribeToEvts() {
    runIn(4, "registerDevices")
    logInfo("Starting Device Subscription Process")
    if(settings?.addSecurityDevice) {
        subscribe(location, "hsmStatus", changeHandler)
    }
    if(settings?.modeList) {
        logDebug("Registering (${settings?.modeList?.size() ?: 0}) Virtual Mode Devices")
        subscribe(location, "mode", changeHandler)
        if(state?.lastMode == null) { state?.lastMode = location?.getMode() }
    }
    state?.subscriptionRenewed = 0
    if(settings?.routineList) {
        logDebug("Registering (${settings?.routineList?.size() ?: 0}) Virtual Routine Devices")
        subscribe(location, "routineExecuted", changeHandler)
    }
}

private void healthCheck() {
    checkVersionData()
    if(checkIfCodeUpdated()) {
        logWarn("Code Version Change Detected... Health Check will occur on next cycle.")
        return
    }
}

private Boolean checkIfCodeUpdated() {
    logDebug("Code versions: ${state?.codeVersions}")
    if(state?.codeVersions) {
        if(state?.codeVersions?.mainApp != appVersionFLD) {
            checkVersionData(true)
            state?.pollBlocked = true
            updCodeVerMap("mainApp", appVersionFLD)
            Map iData = atomicState?.installData ?: [:]
            iData["updatedDt"] = getDtNow().toString()
            iData["shownChgLog"] = false
            if(iData?.shownDonation == null) {
                iData["shownDonation"] = false
            }
            atomicState?.installData = iData
            logInfo("Code Version Change Detected... | Re-Initializing SmartApp in 5 seconds")
            return true
        }
    }
    return false
}

private void stateCleanup() {
    List removeItems = ["hubPlatform", "cmdHistory", "evtHistory"]
    if(state?.directIP && state?.directPort) {
        state?.pluginDetails = [
            directIP: state?.directIP,
            directPort: state?.directPort
        ]
        removeItems?.push("directIP")
        removeItems?.push("directPort")
    }
    removeItems?.each { if(state?.containsKey(it)) state?.remove(it) }
}

private List renderDevices() {
    Map devMap = [:]
    List devList = []
    List items = deviceSettingKeys()?.collect { it?.key as String }
    items = items+["modeList", "routineList"]
    items?.each { item ->
        if(settings[item]?.size()) {
            settings[item]?.each { dev->
                try {
                    Map devObj = getDeviceData(item, dev) ?: [:]
                    if(devObj?.size()) { devMap[dev] = devObj }
                } catch (ex) {
                    // log.error "Device (${dev?.displayName}) Render Exception: ${ex}"
                    logError("Device (${dev?.displayName}) Render Exception: ${ex.message}")
                }
            }
        }
    }
    if(settings?.addSecurityDevice == true) { devList?.push(getSecurityDevice()) }
    if(devMap?.size()) { devMap?.sort{ it?.value?.name }?.each { k,v-> devList?.push(v) } }
    return devList
}

private Map getDeviceData(type, sItem) {
    // log.debug "getDeviceData($type, $sItem)"
    String curType = null
    String devId = sItem
    Boolean isVirtual = false
    String firmware = null
    String name = null
    Map optFlags = [:]
    def attrVal = null
    def obj = null
    switch(type) {
        case "routineList":
            isVirtual = true
            curType = "Routine"
            optFlags["virtual_routine"] = 1
            obj = getRoutineById(sItem)
            if(obj) {
                name = "Routine - " + obj?.label
                attrVal = "off"
            }
            break
        case "modeList":
            isVirtual = true
            curType = "Mode"
            optFlags["virtual_mode"] = 1
            obj = getModeById(sItem)
            if(obj) {
                name = "Mode - " + obj?.name
                attrVal = modeSwitchState(obj?.name)
            }
            break
        default:
            curType = "device"
            obj = sItem
            // Define firmware variable and initialize it out of device handler attribute`
            try {
                if (sItem?.hasAttribute("firmware")) { firmware = sItem?.currentValue("firmware")?.toString() }
            } catch (ex) { firmware = null }
            break
    }
    if(curType && obj) {
        return [
            name: !isVirtual ? sItem?.displayName?.toString()?.replaceAll("[#\$()!%&@^']", "") : name?.toString()?.replaceAll("[#\$()!%&@^']", ""),
            basename: !isVirtual ? sItem?.name : name,
            deviceid: !isVirtual ? sItem?.id : devId,
            status: !isVirtual ? sItem?.status : "Online",
            manufacturerName: (!isVirtual ? sItem?.manufacturerName : pluginNameFLD) ?: pluginNameFLD,
            modelName: !isVirtual ? (sItem?.modelName ?: sItem?.getTypeName()) : "${curType} Device",
            serialNumber: !isVirtual ? sItem?.getDeviceNetworkId() : "${curType}${devId}",
            firmwareVersion: firmware ?: "1.0.0",
            lastTime: !isVirtual ? (sItem?.getLastActivity() ?: null) : now(),
            capabilities: !isVirtual ? deviceCapabilityList(sItem) : ["${curType}": 1],
            commands: !isVirtual ? deviceCommandList(sItem) : [on: 1],
            deviceflags: !isVirtual ? getDeviceFlags(sItem) : optFlags,
            attributes: !isVirtual ? deviceAttributeList(sItem) : ["switch": attrVal]
        ]
    }
    return null
}

String modeSwitchState(String mode) {
    return (location?.getMode() == mode) ? "on" : "off"
}

def getSecurityDevice() {
    return [
        name: getAlarmSystemName(),
        basename: getAlarmSystemName(),
        deviceid: "alarmSystemStatus_${location?.id}",
        status: "ACTIVE",
        manufacturerName: pluginNameFLD,
        modelName: getAlarmSystemName(),
        serialNumber: getAlarmSystemName(true),
        firmwareVersion: "1.0.0",
        lastTime: null,
        capabilities: ["Alarm System Status": 1, "Alarm": 1],
        commands: [],
        attributes: ["alarmSystemStatus": getSecurityStatus()]
    ]
}

def getDeviceFlags(device) {
    Map opts = [:]
    if(settings?.fan3SpdList?.find { it?.id == device?.id }) {
        opts["fan_3_spd"] = 1
    }
    if(settings?.fan4SpdList?.find { it?.id == device?.id }) {
        opts["fan_4_spd"] = 1
    }
    // if(opts?.size()) log.debug "opts: ${opts}"
    return opts
}

def findDevice(dev_id) {
    List allDevs = []
    deviceSettingKeys()?.collect { it?.key as String }?.each { key-> allDevs = allDevs + (settings?."${key}" ?: []) }
    return allDevs?.find { it?.id == dev_id } ?: null
}

def authError() {
    return [error: "Permission denied"]
}

String getAlarmSystemName(abbr=false) {
    return (abbr ? "HSM" : "Hubitat Safety Monitor")
}

def getSecurityStatus(retInt=false) {
    def cur = location?.hsmStatus
    // def inc = getShmIncidents()
    // if(inc != null && inc?.size()) { cur = 'alarm_active' }
    if(retInt) {
        switch (cur) {
            case 'armedHome':
            case 'stay':
                return 0
            case 'armedAway':
            case 'away':
                return 1
            case 'armedNight':
            case 'night':
                return 2
            case 'disarmed':
            case 'off':
                return 3
            case 'alarm_active':
                return 4
        }
    } else { return cur ?: "disarmed" }
}

public setAlarmSystemMode(mode) {
    switch(mode) {
        case "armAway":
        case "away":
            mode = "armAway"
            break
        case "armNight":
            mode = "armNight"
            break;
        case "armHome":
        case "night":
        case "stay":
            mode = "armHome"
            break
        case "disarm":
        case "off":
            mode = "disarm"
            break
    }
    logInfo("Setting the ${getAlarmSystemName()} Mode to (${mode})...")
    sendLocationEvent(name: "hsmSetArm", value: mode.toString())
}

String getAppEndpointUrl(subPath)   { return "${getApiServerUrl()}/${getHubUID()}/apps/${app?.id}${subPath ? "/${subPath}" : ""}?access_token=${state?.accessToken}" }
String getLocalEndpointUrl(subPath) { return "${getLocalApiServerUrl()}/apps/${app?.id}${subPath ? "/${subPath}" : ""}?access_token=${state?.accessToken}" }
String getLocalUrl(subPath) { return "${getLocalApiServerUrl()}/apps/${app?.id}${subPath ? "/${subPath}" : ""}?access_token=${state?.accessToken}" }

def renderConfig() {
    Map jsonMap = [
        platform: pluginNameFLD,
        name: pluginNameFLD,
        app_url_local: "${getLocalApiServerUrl()}/",
        app_url_cloud: "${getApiServerUrl()}/${getHubUID()}/apps/",
        app_id: app?.getId(),
        app_platform: platformFLD,
        use_cloud: (settings?.use_cloud_endpoint == true),
        access_token: atomicState?.accessToken,
        temperature_unit: settings?.temp_unit ?: location?.temperatureScale,
        validateTokenId: (settings?.validate_token == true),
        logConfig: [
            debug: false,
            showChanges: true,
            hideTimestamp: false,
            hideNamePrefix: false,
            file: [
                enabled: true
            ]
        ]
    ]
    def cj = new groovy.json.JsonOutput().toJson(jsonMap)
    def cs = new groovy.json.JsonOutput().prettyPrint(cj)
    return cs
}

def renderLocation() {
    return [
        latitude: location?.latitude,
        longitude: location?.longitude,
        mode: location?.mode,
        name: location?.name,
        temperature_scale: settings?.temp_unit ?: location?.temperatureScale,
        zip_code: location?.zipCode,
        hubIP: location?.hubs[0]?.localIP,
        use_cloud: (settings?.use_cloud_endpoint == true),
        app_version: appVersionFLD
    ]
}

def CommandReply(statusOut, messageOut, code) {
    def replyJson = new groovy.json.JsonOutput().toJson([status: statusOut, message: messageOut])
    logInfo(messageOut)
    render contentType: "application/json", data: replyJson, code: code
}

private getHttpHeaders(headers) {
  def obj = [:]
    new String(headers.decodeBase64()).split("\r\n")?.each {param ->
    def nameAndValue = param.split(":")
    obj[nameAndValue[0]] = (nameAndValue.length == 1) ? "" : nameAndValue[1].trim()
  }
  return obj
}

def deviceCommand() {
    // log.info("Command Request: $params")
    def val1 = request?.JSON?.value1 ?: null
    def val2 = request?.JSON?.value2 ?: null
    return processCmd(params?.id, params?.command, val1, val2)
}

private processCmd(devId, cmd, value1, value2, local=false) {
    Long execDt = now()
    def device = findDevice(devId)
    logInfo("Process Command${local ? "(LOCAL)" : ""} | DeviceId: $devId | Command: ($cmd)${value1 ? " | Param1: ($value1)" : ""}${value2 ? " | Param2: ($value2)" : ""}")
    def command = cmd
    if(settings?.addSecurityDevice != false && devId == "alarmSystemStatus_${location?.id}") {
        setAlarmSystemMode(command)
        logCmd([cmd: command, device: getAlarmSystemName(), value1: value1, value2: value2, execTime: execDt ? (now()-execDt) : 0])
        return CommandReply("Success", "Security Alarm, Command $command", 200)
    }  else if (settings?.modeList && command == "mode" && devId) {
        logDebug("Virtual Mode Received: ${devId}")
        changeMode(devId)
        Long pt = execDt ? (now()-execDt) : 0
        logCmd([cmd: command, device: "Mode Device", value1: value1, value2: value2, execTime: pt])
        return CommandReply("Success", "Mode Device | Command $command | Process Time: (${pt}ms)", 200)
    } else if (settings?.routineList && command == "routine" && devId) {
        logDebug("Virtual Routine Received: ${devId}")
        runRoutine(devId)
        Long pt = execDt ? (now()-execDt) : 0
        logCmd([cmd: command, device: "Routine Device", value1: value1, value2: value2, execTime: pt])
        return CommandReply("Success", "Routine | ${device?.displayName} | Command $command | Process Time: (${pt}ms)", 200)
    } else {
        if (!device) {
            logError("Device Not Found")
            return CommandReply("Failure", "Device Not Found", 500)
        } else if (!device?.hasCommand(command as String)) {
            logError("Device ${device.displayName} does not have the command $command")
            return CommandReply("Failure", "Device ${device.displayName} does not have the command $command", 500)
        } else {
            try {
                if (value2 != null) {
                    device?."$command"(value1,value2)
                    logInfo("Command Successful for Device ${device.displayName} | Command [${command}($value1, $value2)]")
                } else if (value1 != null) {
                    device?."$command"(value1)
                    logInfo("Command Successful for Device ${device.displayName} | Command [${command}($value1)]")
                } else {
                    device?."$command"()
                    logInfo("Command Successful for Device ${device.displayName} | Command [${command}()]")
                }
                Long pt = execDt ? (now()-execDt) : 0
                logCmd([cmd: command, device: device?.displayName, value1: value1, value2: value2, execTime: pt])
                return CommandReply("Success", " | ${device?.displayName} | Command [${command}()] | Process Time: (${pt}ms)", 200)
            } catch (e) {
                logError("Error Occurred for Device ${device.displayName} | Command [${command}()]")
                return CommandReply("Failure", "Error Occurred For Device ${device.displayName} | Command [${command}()]", 500)
            }
        }
    }
}

private void changeMode(modeId) {
    if(modeId) {
        def mode = findVirtModeDevice(modeId)
        if(mode) {
            logInfo("Setting the Location Mode to (${mode})...")
            setLocationMode(mode as String)
            state?.lastMode = mode as String
        } else { logError("Unable to find a matching mode for the id: ${modeId}") }
    }
}

private void runRoutine(rtId) {
    if(rtId) {
        def rt = findVirtRoutineDevice(rtId)
        if(rt?.label) {
            logInfo("Executing the (${rt?.label}) Routine...")
            location?.helloHome?.execute(rt?.label)
        } else { logError("Unable to find a matching routine for the id: ${rtId}") }
    }
}

def deviceAttribute() {
    def device = findDevice(params?.id)
    def attribute = params?.attribute
    if (!device) {
        httpError(404, "Device not found")
    } else {
        return [currentValue: device?.currentValue(attribute)]
    }
}

def findVirtModeDevice(id) {
    return getModeById(id) ?: null
}

def findVirtRoutineDevice(id) {
    return getRoutineById(id) ?: null
}

def deviceQuery() {
    log.trace "deviceQuery(${params?.id}"
    def device = findDevice(params?.id)
    if (!device) {
        def mode = findVirtModeDevice(params?.id)
        def routine = findVirtModeDevice(params?.id)
        def obj = mode ? mode : routine ?: null
        if(!obj) {
            device = null
            httpError(404, "Device not found")
        } else {
            def name = routine ? obj?.label : obj?.name
            def type = routine ? "Routine" : "Mode"
            def attrVal = routine ? "off" : modeSwitchState(obj?.name)
            try {
                deviceData?.push([
                    name: name,
                    deviceid: params?.id,
                    capabilities: ["${type}": 1],
                    commands: [on:1],
                    attributes: ["switch": attrVal]
                ])
            } catch (e) {
                logError("Error Occurred Parsing ${item} ${type} ${name}, Error: ${ex}")
            }
        }
    }

    if (result) {
        def jsonData = [
            name: device.displayName,
            deviceid: device.id,
            capabilities: deviceCapabilityList(device),
            commands: deviceCommandList(device),
            attributes: deviceAttributeList(device)
        ]
        def resultJson = new groovy.json.JsonOutput().toJson(jsonData)
        render contentType: "application/json", data: resultJson
    }
}

def deviceCapabilityList(device) {
    String devId = device?.getId()
    def capItems = device?.capabilities?.findAll{ !(it?.name in ignoreListFLD?.capabilities) }?.collectEntries { capability-> [ (capability?.name as String):1 ] }
    if(isDeviceInInput("lightList", device?.id)) {
        capItems["LightBulb"] = 1
    }
    if(isDeviceInInput("buttonList", device?.id)) {
        capItems["Button"] = 1
    }
    if(isDeviceInInput("fanList", device?.id)) {
        capItems["Fan"] = 1
    }
    if(isDeviceInInput("speakerList", device?.id)) {
        capItems["Speaker"] = 1
    }
    if(isDeviceInInput("shadesList", device?.id)) {
        capItems["WindowShade"] = 1
    }
    if(isDeviceInInput("garageList", device?.id)) {
        capItems["GarageDoorControl"] = 1
    }
    if(isDeviceInInput("tstatList", device?.id)) {
        capItems["Thermostat"] = 1
        capItems["ThermostatOperatingState"] = 1
        capItems?.remove("ThermostatFanMode")
    }
    if(isDeviceInInput("tstatFanList", device?.id)) {
        capItems["Thermostat"] = 1
        capItems["ThermostatOperatingState"] = 1
    }
    if(isDeviceInInput("tstatHeatList", device?.id)) {
        capItems["Thermostat"] = 1
        capItems["ThermostatOperatingState"] = 1
        capItems?.remove("ThermostatCoolingSetpoint")
    }
    if(settings?.noTemp && capItems["TemperatureMeasurement"] && (capItems["ContactSensor"] || capItems["WaterSensor"])) {
        Boolean remTemp = true
        if(settings?.sensorAllowTemp && isDeviceInInput("sensorAllowTemp", device?.id)) remTemp = false
        if(remTemp) { capItems?.remove("TemperatureMeasurement") }
    }

    //This will filter out selected capabilities from the devices selected in filtering inputs.
    Map remCaps = [
       "Acceleration": "AccelerationSensor", "Battery": "Battery", "Button": "Button", "Contact": "ContactSensor", "Energy": "EnergyMeter", "Humidity": "RelativeHumidityMeasurement",
       "Illuminance": "IlluminanceMeasurement", "Level": "SwitchLevel", "Lock": "Lock", "Motion": "MotionSensor", "Power": "PowerMeter", "Presence": "PresenceSensor", "Switch": "Switch",
       "Tamper": "TamperAlert", "Temp": "TemperatureMeasurement", "Valve": "Valve"
    ]
    List remKeys = settings?.findAll { it?.key?.toString()?.startsWith("remove") && it?.value != null }?.collect { it?.key as String } ?: []
    remKeys?.each { k->
        String capName = k?.replaceAll("remove", "")
        if(remCaps[capName] && capItems[remCaps[capName]] && isDeviceInInput(k, device?.id)) { capItems?.remove(remCaps[capName]);  if(showDebugLogs) { logDebug("Filtering ${capName}"); } }
    }
    return capItems?.sort { it?.key }
}

def deviceCommandList(device) {
    def cmds = device?.supportedCommands?.findAll { !(it?.name in ignoreListFLD?.commands) }?.collectEntries { c-> [ (c?.name): 1 ] }
    if(isDeviceInInput("tstatList", device?.id)) { cmds?.remove("setThermostatFanMode"); cmds?.remove("fanAuto"); cmds?.remove("fanOn"); cmds?.remove("fanCirculate"); }
    if(isDeviceInInput("tstatHeatList", device?.id)) { cmds?.remove("setCoolingSetpoint"); cmds?.remove("auto"); cmds?.remove("cool"); }
    return cmds
}

def deviceAttributeList(device) {
    def atts = device?.supportedAttributes?.findAll { !(it?.name in ignoreListFLD?.attributes) }?.collectEntries { attribute->
        try {
            [(attribute?.name): device?.currentValue(attribute?.name)]
        } catch(e) {
            [(attribute?.name): null]
        }
    }
    if(isDeviceInInput("tstatHeatList", device?.id)) { atts?.remove("coolingSetpoint"); atts?.remove("coolingSetpointRange"); }
    return atts
}

def getAllData() {
    state?.subscriptionRenewed = now()
    state?.devchanges = []
    def deviceJson = new groovy.json.JsonOutput().toJson([location: renderLocation(), deviceList: renderDevices()])
    updTsVal("lastDeviceDataQueryDt")
    render contentType: "application/json", data: deviceJson
}

Map deviceSettingKeys() {
    return [
        "fanList": "Fan Devices", "fan3SpdList": "Fans (3Spd) Devices", "fan4SpdList": "Fans (4Spd) Devices", "buttonList": "Button Devices", "deviceList": "Other Devices",
        "sensorList": "Sensor Devices", "speakerList": "Speaker Devices", "switchList": "Switch Devices", "lightList": "Light Devices", "shadesList": "Window Shade Devices",
        "garageList": "Garage Devices", "tstatList": "T-Stat Devices", "tstatFanList": "T-Stat + Fan Devices", "tstatHeatList": "T-Stat Devices (Heat)"
    ]
}

def registerDevices() {
    //This has to be done at startup because it takes too long for a normal command.
    ["lightList": "Light Devices", "fanList": "Fan Devices", "fan3SpdList": "Fans (3SPD) Devices", "fan4SpdList": "Fans (4SPD) Devices", "buttonList": "Button Devices"]?.each { k,v->
        logDebug("Registering (${settings?."${k}"?.size() ?: 0}) ${v}")
        registerChangeHandler(settings?."${k}")
    }
    runIn(3, "registerDevices2")
}

def registerDevices2() {
    //This has to be done at startup because it takes too long for a normal command.
    ["sensorList": "Sensor Devices", "speakerList": "Speaker Devices", "deviceList": "Other Devices"]?.each { k,v->
        logDebug("Registering (${settings?."${k}"?.size() ?: 0}) ${v}")
        registerChangeHandler(settings?."${k}")
    }
    runIn(3, "registerDevices3")
}

def registerDevices3() {
    //This has to be done at startup because it takes too long for a normal command.
    ["switchList": "Switch Devices", "shadesList": "Window Shade Devices", "garageList": "Garage Door Devices", "tstatList": "Thermostat Devices", "tstatFanList": "Thermostat + Fan Devices", "tstatHeatList": "Thermostat (HeatOnly) Devices"]?.each { k,v->
        logDebug("Registering (${settings?."${k}"?.size() ?: 0}) ${v}")
        registerChangeHandler(settings?."${k}")
    }
    logDebug("Registered (${getDeviceCnt(true)} Devices)")
    logDebug("-----------------------------------------------")

    if(settings?.restartService == true) {
        logWarn("Sent Request to Homebridge Service to Stop... Service should restart automatically")
        attemptServiceRestart()
        settingUpdate("restartService", "false", "bool")
    }
    runIn(5, "updateServicePrefs")
    runIn(8, "sendDeviceRefreshCmd")
}

Boolean isDeviceInInput(setKey, devId) {
    if(settings[setKey]) {
        return (settings[setKey]?.find { it?.getId() == devId })
    }
    return false
}

def registerChangeHandler(devices, showlog=false) {
    devices?.each { device ->
        List theAtts = device?.supportedAttributes?.collect { it?.name as String }?.unique()
        if(showlog) { log.debug "atts: ${theAtts}" }
        theAtts?.each {att ->
            if(!(ignoreListFLD?.evt_attributes?.contains(att))) {
                if(settings?.noTemp && att == "temperature" && (device?.hasAttribute("contact") || device?.hasAttribute("water"))) {
                    Boolean skipAtt = true
                    if(settings?.sensorAllowTemp) {
                        skipAtt = isDeviceInInput('sensorAllowTemp', device?.id)
                    }
                    if(skipAtt) { return }
                }
                Map attMap = [
                    "acceleration": "Acceleration", "battery": "Battery", "button": "Button", "contact": "Contact", "energy": "Energy", "humidity": "Humidity", "illuminance": "Illuminance",
                    "level": "Level", "lock": "Lock", "motion": "Motion", "power": "Power", "presence": "Presence", "switch": "Switch", "tamper": "Tamper",
                    "temperature": "Temp", "valve": "Valve"
                ]?.each { k,v -> if(att == k && isDeviceInInput('remove${v}', device?.id)) { return } }
                subscribe(device, att, "changeHandler")
                if(showlog) { log.debug "Registering ${device?.displayName} for ${att} events" }
            }
        }
    }
}

def changeHandler(evt) {
    Long execDt = now()
    List sendItems = []
    Integer sendNum = 1
    String src = evt?.source
    def deviceid = evt?.deviceId
    String deviceName = evt?.displayName
    String attr = evt?.name
    def value = evt?.value
    def dt = evt?.date
    Boolean sendEvt = true

    switch(evt?.name) {
        case "hsmStatus":
            deviceid = "alarmSystemStatus_${location?.id}"
            attr = "alarmSystemStatus"
            sendItems?.push([evtSource: src, evtDeviceName: deviceName, evtDeviceId: deviceid, evtAttr: attr, evtValue: value, evtUnit: evt?.unit ?: "", evtDate: dt])
            break
        case "hsmAlert":
            if(evt?.value == "intrusion") {
                deviceid = "alarmSystemStatus_${location?.id}"
                attr = "alarmSystemStatus"
                value = "alarm_active"
                sendItems?.push([evtSource: src, evtDeviceName: deviceName, evtDeviceId: deviceid, evtAttr: attr, evtValue: value, evtUnit: evt?.unit ?: "", evtDate: dt])
            } else { sendEvt = false }
            break
        case "hsmRules":
        case "hsmSetArm":
            sendEvt = false
            break
        case "alarmSystemStatus":
            deviceid = "alarmSystemStatus_${location?.id}"
            sendItems?.push([evtSource: src, evtDeviceName: deviceName, evtDeviceId: deviceid, evtAttr: attr, evtValue: value, evtUnit: evt?.unit ?: "", evtDate: dt])
            break
        case "mode":
            settings?.modeList?.each { id->
                def md = getModeById(id)
                if(md && md?.id) { sendItems?.push([evtSource: "MODE", evtDeviceName: "Mode - ${md?.name}", evtDeviceId: md?.id, evtAttr: "switch", evtValue: modeSwitchState(md?.name), evtUnit: "", evtDate: dt]) }
            }
            break
        case "routineExecuted":
            settings?.routineList?.each { id->
                def rt = getRoutineById(id)
                if(rt && rt?.id) {
                    sendItems?.push([evtSource: "ROUTINE", evtDeviceName: "Routine - ${rt?.label}", evtDeviceId: rt?.id, evtAttr: "switch", evtValue: "off", evtUnit: "", evtDate: dt])
                }
            }
            break
        default:
            def evtData = null
            if(attr == "button") { evtData = parseJson(evt?.data) }
            sendItems?.push([evtSource: src, evtDeviceName: deviceName, evtDeviceId: deviceid, evtAttr: attr, evtValue: value, evtUnit: evt?.unit ?: "", evtDate: dt, evtData: evtData])
            break
    }

    if (sendEvt && state?.pluginDetails?.directIP != "" && sendItems?.size()) {
        //Send Using the Direct Mechanism
        sendItems?.each { send->
            if(settings?.showEventLogs) {
                String unitStr = ""
                switch(send?.evtAttr as String) {
                    case "temperature":
                        unitStr = "\u00b0${send?.evtUnit}"
                        break
                    case "humidity":
                    case "level":
                    case "battery":
                        unitStr = "%"
                        break
                    case "power":
                        unitStr = "W"
                        break
                    case "illuminance":
                        unitStr = " Lux"
                        break
                    default:
                        unitStr = "${send?.evtUnit}"
                        break
                }
                logInfo("Sending${" ${send?.evtSource}" ?: ""} Event (${send?.evtDeviceName} | ${send?.evtAttr.toUpperCase()}: ${send?.evtValue}${unitStr}) ${send?.evtData ? "Data: ${send?.evtData}" : ""} to Homebridge at (${state?.pluginDetails?.directIP}:${state?.pluginDetails?.directPort})")
            }
            sendHttpPost("update", [
                change_name: send?.evtDeviceName,
                change_device: send?.evtDeviceId,
                change_attribute: send?.evtAttr,
                change_value: send?.evtValue,
                change_data: send?.evtData,
                change_date: send?.evtDate,
                app_id: app?.getId(),
                access_token: atomicState?.accessToken
            ], "EventUpdate")
            logEvt([name: send?.evtAttr, value: send?.evtValue, device: send?.evtDeviceName, execTime: execDt ? (now()-execDt) : 0])
        }
    }
}

private sendHttpGet(path, contentType) {
    if(settings?.sendViaNgrok && settings?.ngrokHttpUrl) {
        httpGet([
            uri: (settings?.sendViaNgrok && settings?.ngrokHttpUrl) ? "https://${settings?.ngrokHttpUrl}.ngrok.io/${path}" : "http://${getServerAddress()}/${path}",
            requestContentType: contentType,
            contentType: contentType,
            timeout: 20
        ])
    } else { sendHubCommand(new hubitat.device.HubAction(method: "GET", path: "/${path}", headers: [HOST: getServerAddress()])) }
}

private sendHttpPost(path, body, src="", contentType = "application/json") {
    Map params = [
        uri: (settings?.sendViaNgrok && settings?.ngrokHttpUrl) ? "https://${settings?.ngrokHttpUrl}.ngrok.io/${path}" : "http://${getServerAddress()}/${path}",
        requestContentType: contentType,
        contentType: contentType,
        body: body,
        timeout: 20
    ]
    execAsyncHttpCmd("POST", params, [execDt: now(), src: src])
}

private execAsyncHttpCmd(String method, Map params, Map otherData = null) {
    if(method && params) {
        String m = method?.toString()?.toLowerCase()
        "asynchttp${m?.capitalize()}"("asyncHttpCmdResp", params, otherData)
    } else { logError("execAsyncHttpCmd Error | Missing a required parameter") }
}

def asyncHttpCmdResp(response, data) {
    logDebug("asyncHttpCmdResp | Src: ${data?.src ?: "Unknown"} | Resp: ${response?.getData()} | Status: ${response?.getStatus()} | Data: ${data}")
    def resp = response?.getData() || null
    if(resp) {
        logDebug("${data?.src != "" ? data?.src : "${method}"} Command Completed | Process Time: (${data?.execDt ? (now()-data?.execDt) : 0}ms)")
    }
}

private getServerAddress() { return "${state?.pluginDetails?.directIP}:${state?.pluginDetails?.directPort}" }

def getModeById(String mId) {
    return location?.getModes()?.find{it?.id?.toString() == mId}
}

def getModeByName(String name) {
    return location?.getModes()?.find{it?.name?.toString() == name}
}


def getRoutineById(String rId) {
    return location?.helloHome?.getPhrases()?.find{it?.id == rId}
}

def getRoutineByName(String name) {
    return location?.helloHome?.getPhrases()?.find{it?.label == name}
}

def getShmIncidents() {
    //Thanks Adrian
    def incidentThreshold = now() - 604800000
    return location.activeIncidents.collect{[date: it?.date?.time, title: it?.getTitle(), message: it?.getMessage(), args: it?.getMessageArgs(), sourceType: it?.getSourceType()]}.findAll{ it?.date >= incidentThreshold } ?: null
}

void settingUpdate(name, value, type=null) {
    if(name && type) {
        app?.updateSetting("$name", [type: "$type", value: value])
    }
    else if (name && type == null){ app?.updateSetting(name.toString(), value) }
}

void settingRemove(String name) {
    // logTrace("settingRemove($name)...")
    if(name && settings?.containsKey(name as String)) { app?.removeSetting(name as String) }
}

Boolean devMode() {
    return (devModeFLD == true)
}

private activateDirectUpdates(isLocal=false) {
    logTrace("activateDirectUpdates: ${getServerAddress()}${isLocal ? " | (Local)" : ""}")
    sendHttpPost("initial", [
        app_id: app?.getId(),
        access_token: atomicState?.accessToken
    ], "activateDirectUpdates")
}

private attemptServiceRestart(isLocal=false) {
    logTrace("attemptServiceRestart: ${getServerAddress()}${isLocal ? " | (Local)" : ""}")
    sendHttpPost("restart", [
        app_id: app?.getId(),
        access_token: atomicState?.accessToken
    ], "attemptServiceRestart")
}

private sendDeviceRefreshCmd(isLocal=false) {
    logTrace("sendDeviceRefreshCmd: ${getServerAddress()}${isLocal ? " | (Local)" : ""}")
    sendHttpPost("refreshDevices", [
        app_id: app?.getId(),
        access_token: atomicState?.accessToken
    ], "sendDeviceRefreshCmd")
}

private updateServicePrefs(isLocal=false) {
    logTrace("updateServicePrefs: ${getServerAddress()}${isLocal ? " | (Local)" : ""}")
    sendHttpPost("updateprefs", [
        app_id: app?.getId(),
        access_token: atomicState?.accessToken,
        use_cloud: (settings?.use_cloud_endpoint == true),
        validateTokenId: (settings?.validate_token == true),
        local_hub_ip: location?.hubs[0]?.localIP
    ], "updateServicePrefs")
}

def pluginStatus() {
    def body = request?.JSON;
    state?.pluginUpdates = [hasUpdate: (body?.hasUpdate == true), newVersion: (body?.newVersion ?: null)]
    if(body?.version) { updCodeVerMap("plugin", body?.version)}
    def resultJson = new groovy.json.JsonOutput().toJson([status: 'OK'])
    render contentType: "application/json", data: resultJson
}

def enableDirectUpdates() {
    // log.trace "enableDirectUpdates: ($params)"
    state?.pluginDetails = [
        directIP: params?.ip,
        directPort: params?.port,
        version: params?.version ?: null
    ]
    updCodeVerMap("plugin", params?.version ?: null)
    activateDirectUpdates()
    updTsVal("lastDirectUpdsEnabled")
    def resultJson = new groovy.json.JsonOutput().toJson([status: 'OK'])
    render contentType: "application/json", data: resultJson
}

mappings {
    path("/devices")					{ action: [GET: "getAllData"]       }
    path("/alldevices")                 { action: [GET: "renderDevices"]    }
    path("/deviceDebug")			    { action: [GET: "viewDeviceDebug"]  }
    path("/location")					{ action: [GET: "renderLocation"]   }
    path("/pluginStatus")			    { action: [POST: "pluginStatus"]    }
    path("/:id/command/:command")		{ action: [POST: "deviceCommand"]   }
    path("/:id/query")					{ action: [GET: "deviceQuery"]      }
    path("/:id/attribute/:attribute")	{ action: [GET: "deviceAttribute"]  }
    path("/startDirect/:ip/:port/:version")		{ action: [POST: "enableDirectUpdates"] }
}

def appInfoSect() {
    Map codeVer = state?.codeVersions ?: null
    Boolean isNote = false
    String tStr = """<small style="color: gray;"><b>Version:</b> v${appVersionFLD}</small>${state?.pluginDetails?.version ? """<br><small style="color: gray;"><b>Plugin:</b> v${state?.pluginDetails?.version}</small>""" : ""}"""
    section (sectH3TS(app?.name, tStr, getAppImg("hb_tonesto7", true), "orange")) {
        Map minUpdMap = getMinVerUpdsRequired()
        List codeUpdItems = codeUpdateItems(true)
        if(minUpdMap?.updRequired && minUpdMap?.updItems?.size()) {
            isNote=true
            String str3 = """<small style="color: red;"><b>Updates Required:</b></small>"""
            minUpdMap?.updItems?.each { item-> str3 += """<br><small style="color: red;">  \u2022 ${item}</small>""" }
            str3 += """<br><br><small style="color: red; font-weight: bold;">If you just updated the code please press Done/Next to let the app process the changes.</small>"""
            paragraph str3
        } else if(codeUpdItems?.size()) {
            isNote=true
            String str2 = """<small style="color: red;"><b>Code Updates Available:</b></small>"""
            codeUpdItems?.each { item-> str2 += """<br><small style="color: red;">  \u2022 ${item}</small>""" }
            paragraph str2
        }
        if(!isNote) { paragraph """<small style="color: gray;">No Issues to Report</small>""" }
        paragraph htmlLine("orange")
	}
}

/**********************************************
        APP HELPER FUNCTIONS
***********************************************/
String getAppImg(String imgName, frc=false, ext=".png") { return frc ? "https://raw.githubusercontent.com/tonesto7/homebridge-hubitat-tonesto7/${branchFLD}/images/${imgName}${ext}" : "" }
String getPublicImg(String imgName, frc=false) { return frc ? "https://raw.githubusercontent.com/tonesto7/SmartThings-tonesto7-public/master/resources/icons/${imgName}.png" : "" }
String sectTS(String t, String i = null, bold=false) { return """<h3>${i ? """<img src="${i}" width="48"> """ : ""} ${bold ? "<b>" : ""}${t?.replaceAll("\\n", "<br>")}${bold ? "</b>" : ""}</h3>""" }
String sectH3TS(String t, String st, String i = null, c="#1A77C9") { return """<h3 style="color:${c};font-weight: bold">${i ? """<img src="${i}" width="48"> """ : ""} ${t?.replaceAll("\\n", "<br>")}</h3>${st ? "${st}" : ""}""" }
String paraTS(String t, String i = null, bold=true, color=null) { return "${color ? """<div style="color: $color;">""" : ""}${bold ? "<b>" : ""}${i ? """<img src="${i}" width="48"> """ : ""}${t?.replaceAll("\\n", "<br>")}${bold ? "</b>" : ""}${color ? "</div>" : ""}" }
String inputTS(String t, String i = null, color=null, under=true) { return """${color ? """<div style="color: $color;">""" : ""}${i ? """<img src="${i}" width="48"> """ : ""} ${under ? "<u>" : ""}${t?.replaceAll("\\n", " ")}${under ? "</u>" : ""}${color ? "</div>" : ""}""" }
String htmlLine(color="#1A77C9") { return "<hr style='background-color:${color}; height: 1px; border: 0;'>" }
def appFooter() {
	section() {
		paragraph htmlLine("orange")
		paragraph """<div style='color:orange;text-align:center'>Homebridge Hubitat<br><a href='https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=RVFJTG8H86SK8&source=url' target="_blank"><img width="120" height="120" src="https://raw.githubusercontent.com/tonesto7/homebridge-hubitat-tonesto7/master/images/donation_qr.png"></a><br><br>Please consider donating if you find this integration useful.</div>"""
	}       
}

String bulletItem(String inStr, String strVal) { return "${inStr == "" ? "" : "\n"} \u2022 ${strVal}" }
String dashItem(String inStr, String strVal, newLine=false) { return "${(inStr == "" && !newLine) ? "" : "\n"} - ${strVal}" }
String textDonateLink() { return "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=RVFJTG8H86SK8&source=url" }
Integer versionStr2Int(str) { return str ? str?.toString()?.tokenize("-")[0]?.replaceAll("\\.", "")?.toInteger() : null }
String versionCleanup(str) { return str ? str?.toString()?.tokenize("-")[0] : null }
Boolean codeUpdIsAvail(String newVer, String curVer, String type) {
    Boolean result = false
    def latestVer
    if(newVer && curVer) {
        newVer = versionCleanup(newVer)
        curVer = versionCleanup(curVer)
        List versions = [newVer, curVer]
        if(newVer != curVer) {
            latestVer = versions?.max { a, b ->
                List verA = a?.tokenize('.'); List verB = b?.tokenize('.'); Integer commonIndices = Math.min(verA?.size(), verB?.size());
                for (int i = 0; i < commonIndices; ++i) { if(verA[i]?.toInteger() != verB[i]?.toInteger()) { return verA[i]?.toInteger() <=> verB[i]?.toInteger() }; }
                verA?.size() <=> verB?.size()
            }
            result = (latestVer == newVer)
        }
    }
    return result
}
Boolean appUpdAvail() { return (state?.appData?.versions && state?.codeVersions?.mainApp && codeUpdIsAvail(state?.appData?.versions?.mainApp, appVersionFLD, "main_app")) }
Boolean pluginUpdAvail() { return (state?.appData?.versions && state?.codeVersions?.plugin && codeUpdIsAvail(state?.appData?.versions?.plugin, state?.codeVersions?.plugin, "plugin")) }
private Map getMinVerUpdsRequired() {
    Boolean updRequired = false
    List updItems = []
    Map codeItems = [plugin: "Homebridge Plugin"]
    Map codeVers = state?.codeVersions ?: [:]
    codeVers?.each { k,v->
        try {
            if(codeItems?.containsKey(k as String) && v != null && (versionStr2Int(v) < minVersionsFLD[k as String])) { updRequired = true; updItems?.push(codeItems[k]); }
        } catch (ex) {
            logError("getMinVerUpdsRequired Error: ${ex}")
        }
    }
    return [updRequired: updRequired, updItems: updItems]
}

private List codeUpdateItems(shrt=false) {
    Boolean appUpd = appUpdAvail()
    Boolean plugUpd = pluginUpdAvail()
    List updItems = []
    if(appUpd || servUpd) {
        if(appUpd) updItems.push("${!shrt ? "\nHomebridge " : ""}App: (v${state?.appData?.versions?.mainApp?.toString()})")
        if(plugUpd) updItems.push("${!shrt ? "\n" : ""}Plugin: (v${state?.appData?.versions?.server?.toString()})")
    }
    return updItems
}

Integer getLastTsValSecs(val, nullVal=1000000) {
    def tsMap = atomicState?.tsDtMap
    return (val && tsMap && tsMap[val]) ? GetTimeDiffSeconds(tsMap[val]).toInteger() : nullVal
}

private updTsVal(key, dt=null) {
    def data = atomicState?.tsDtMap ?: [:]
    if(key) { data[key] = dt ?: getDtNow() }
    atomicState?.tsDtMap = data
}

private remTsVal(key) {
    def data = atomicState?.tsDtMap ?: [:]
    if(key) {
        if(key instanceof List) {
            key?.each { k-> if(data?.containsKey(k)) { data?.remove(k) } }
        } else { if(data?.containsKey(key)) { data?.remove(key) } }
        atomicState?.tsDtMap = data
    }
}

private getTsVal(val) {
    def tsMap = atomicState?.tsDtMap
    if(val && tsMap && tsMap[val]) { return tsMap[val] }
    return null
}

private updCodeVerMap(key, val) {
    Map cv = atomicState?.codeVersions ?: [:]
    if(val && (!cv.containsKey(key) || (cv?.containsKey(key) && cv[key] != val))) { cv[key as String] = val }
    if (cv?.containsKey(key) && val == null) { cv?.remove(key) }
    atomicState?.codeVersions = cv
}

private cleanUpdVerMap() {
    Map cv = atomicState?.codeVersions ?: [:]
    cv?.each { k, v-> if(v == null) ri?.push(k) }
    ri?.each { cv?.remove(it) }
    atomicState?.codeVersions = cv
}

private updInstData(key, val) {
    Map iData = atomicState?.installData ?: [:]
    iData[key] = val
    atomicState?.installData = iData
}

private getInstData(key) {
    def iMap = atomicState?.installData
    if(val && iMap && iMap[val]) { return iMap[val] }
    return null
}

private checkVersionData(now = false) { //This reads a JSON file from GitHub with version numbers
    def lastUpd = getLastTsValSecs("lastAppDataUpdDt")
    if (now || !state?.appData || (lastUpd > (3600*6))) {
        if(now && (lastUpd < 300)) { return }
        getConfigData()
    }
}

private getConfigData() {
    Map params = [
        uri: "https://raw.githubusercontent.com/tonesto7/homebridge-hubitat-tonesto7/master/appData.json",
        contentType: "application/json",
        timeout: 20
    ]
    def data = getWebData(params, "appData", false)
    if(data) {
        state?.appData = data
        updTsVal("lastAppDataUpdDt")
        logDebug("Successfully Retrieved (v${data?.appDataVer}) of AppData Content from GitHub Repo...")
    }
}

private getWebData(params, desc, text=true) {
    try {
        httpGet(params) { resp ->
            if(resp?.data) {
                if(text) { return resp?.data?.text.toString() }
                return resp?.data
            }
        }
    } catch (ex) {
        if(ex instanceof groovyx.net.http.HttpResponseException) {
            logWarn("${desc} file not found")
        } else { logError("getWebData(params: $params, desc: $desc, text: $text) Exception: ${ex}") }
        return "${label} info not found"
    }
}

/******************************************
|       DATE | TIME HELPERS
******************************************/
def formatDt(dt, tzChg=true) {
    def tf = new java.text.SimpleDateFormat("E MMM dd HH:mm:ss z yyyy")
    if(tzChg) { if(location.timeZone) { tf.setTimeZone(location?.timeZone) } }
    return tf?.format(dt)
}

def getDtNow() {
    def now = new Date()
    return formatDt(now)
}

def GetTimeDiffSeconds(lastDate, sender=null) {
    try {
        if(lastDate?.contains("dtNow")) { return 10000 }
        def now = new Date()
        def lastDt = Date.parse("E MMM dd HH:mm:ss z yyyy", lastDate)
        def start = Date.parse("E MMM dd HH:mm:ss z yyyy", formatDt(lastDt)).getTime()
        def stop = Date.parse("E MMM dd HH:mm:ss z yyyy", formatDt(now)).getTime()
        def diff = (int) (long) (stop - start) / 1000
        return diff?.abs()
    } catch (ex) {
        logError("GetTimeDiffSeconds Exception: (${sender ? "$sender | " : ""}lastDate: $lastDate): ${ex}")
        return 10000
    }
}

/******************************************
|       Changelog Logic
******************************************/
Boolean showDonationOk() { return (state?.isInstalled && !atomicState?.installData?.shownDonation && getDaysSinceUpdated() >= 30 && !settings?.sentDonation) }
Integer getDaysSinceUpdated() {
    def updDt = atomicState?.installData?.updatedDt ?: null
    if(updDt == null || updDt == "Not Set") {
        updInstData("updatedDt", getDtNow().toString())
        return 0
    }
    def start = Date.parse("E MMM dd HH:mm:ss z yyyy", updDt)
    def stop = new Date()
    if(start && stop) {	return (stop - start) }
    return 0
}

String changeLogData() { 
    String txt = getWebData([uri: "https://raw.githubusercontent.com/tonesto7/homebridge-hubitat-tonesto7/master/CHANGELOG-app.md", contentType: "text/plain; charset=UTF-8"], "changelog")
    return txt?.toString()?.replaceAll("##", "\u2022")?.replaceAll("[\\**_]", ""); // Replaces ## then **_ and _** in changelog data
}
Boolean showChgLogOk() { return (state?.isInstalled && (state?.curAppVer != appVersionFLD || state?.installData?.shownChgLog != true)) }
private changeLogPage() {
    return dynamicPage(name: "changeLogPage", title: "", nextPage: "mainPage", install: false) {
        section(sectTS("Release Notes:", getAppImg("change_log", true), true)) { paragraph changeLogData() }
        state?.curAppVer = appVersionFLD
        updInstData("shownChgLog", true)
    }
}

Integer stateSize() { def j = new groovy.json.JsonOutput().toJson(state); return j?.toString().length(); }
Integer stateSizePerc() { return (int) ((stateSize() / 100000)*100).toDouble().round(0); }

private addToHistory(String logKey, data, Integer max=10) {
    Boolean ssOk = (stateSizePerc() > 70)
    List eData = getMemStoreItem(logKey) ?: []
    if(eData?.find { it?.data == data }) { return; }
    eData?.push([dt: getDtNow(), data: data])
    if(!ssOk || eData?.size() > max) { eData = eData?.drop( (eData?.size()-max) ) }
    updMemStoreItem(logKey, eData)
}

private void logDebug(msg)  { if(showDebugLogs) log.debug "Homebridge (v${appVersionFLD}) | ${msg}"; }
private void logTrace(msg)  { if(showDebugLogs) log.trace "Homebridge (v${appVersionFLD}) | ${msg}"; }
private void logInfo(msg)   { log.info " Homebridge (v${appVersionFLD}) | ${msg}"; }
private void logWarn(msg)   { log.warn " Homebridge (v${appVersionFLD}) | ${msg}"; }
private void logError(msg)  { log.error "Homebridge (v${appVersionFLD}) | ${msg}"; }

private List getCmdHistory() { return getMemStoreItem("cmdHistory") ?: [] }
private List getEvtHistory() { return getMemStoreItem("evtHistory") ?: [] }
private void clearHistory()  { historyMapFLD = [:]; mb(); }

private void logEvt(evtData) { addToHistory("evtHistory", evtData, 25) }
private void logCmd(cmdData) { addToHistory("cmdHistory", cmdData, 25) }


@Field volatile static Map historyMapFLD = [:]
// FIELD VARIABLE FUNCTIONS
private void updMemStoreItem(key, val) {
    String appId = app.getId()
    Boolean aa = getTheLock(sHMLF, "updMemStoreItem(${key})")
    // log.trace "lock wait: ${aa}"
    Map memStore = historyMapFLD[appId] ?: [:]
    memStore[key] = val
    historyMapFLD[appId] = memStore
    historyMapFLD = historyMapFLD
    // log.debug("updMemStoreItem(${key}): ${memStore[key]}")
    releaseTheLock(sHMLF)
}

private List getMemStoreItem(key){
    String appId = app.getId()
    Map memStore = historyMapFLD[appId] ?: [:]
    return memStore[key] ?: null
}

// Memory Barrier
@Field static java.util.concurrent.Semaphore theMBLockFLD=new java.util.concurrent.Semaphore(0)
static void mb(String meth=sNULL){
    if((Boolean)theMBLockFLD.tryAcquire()){
        theMBLockFLD.release()
    }
}

@Field static final String sHMLF = 'theHistMapLockFLD'
@Field static java.util.concurrent.Semaphore histMapLockFLD = new java.util.concurrent.Semaphore(1)

private Integer getSemaNum(String name) {
	if(name == sHMLF) { 
        return 0
    }
    log.warn("unrecognized lock name...")
    return 0
	// Integer stripes=22
	// if(name.isNumber()) return name.toInteger()%stripes
	// Integer hash=smear(name.hashCode())
	// return Math.abs(hash)%stripes
    // log.info "sema $name # $sema"
}

java.util.concurrent.Semaphore getSema(Integer snum) {
	switch(snum) {
		case 0: return histMapLockFLD
		default: log.error "bad hash result $snum"
			return null
	}
}

@Field volatile static Map<String,Long> lockTimesFLD = [:]
@Field volatile static Map<String,String> lockHolderFLD = [:]

Boolean getTheLock(String qname, String meth=sNULL, Boolean longWait=false) {
    Long waitT = longWait ? 1000L : 60L
    Boolean wait = false
    Integer semaNum = getSemaNum(qname)
    String semaSNum = semaNum.toString()
    def sema = getSema(semaNum)
    while(!((Boolean)sema.tryAcquire())) {
        // did not get the lock
        Long timeL = lockTimesFLD[semaSNum]
        if(timeL == null){
            timeL = now()
            lockTimesFLD[semaSNum] = timeL
            lockTimesFLD = lockTimesFLD
        }
        if(devModeFLD) log.warn "waiting for ${qname} ${semaSNum} lock access, $meth, long: $longWait, holder: ${(String)lockHolderFLD[semaSNum]}"
        pauseExecution(waitT)
        wait = true
        if((now() - timeL) > 30000L) {
            releaseTheLock(qname)
            if(devModeFLD) log.warn "overriding lock $meth"
        }
    }
    lockTimesFLD[semaSNum] = now()
    lockTimesFLD = lockTimesFLD
    lockHolderFLD[semaSNum] = "${app.getId()} ${meth}"
    lockHolderFLD = lockHolderFLD
    return wait
}

void releaseTheLock(String qname){
    Integer semaNum=getSemaNum(qname)
    String semaSNum=semaNum.toString()
    def sema=getSema(semaNum)
    lockTimesFLD[semaSNum]=null
    lockTimesFLD=lockTimesFLD
    // lockHolderFLD[semaSNum]=sNULL
    // lockHolderFLD=lockHolderFLD
    sema.release()
}
