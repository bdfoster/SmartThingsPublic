/**
 *  Aeon Labs Aeotec Smart Dimmer 6
 *
 *  Copyright 2017 Brian Foster (https://github.com/bdfoster) and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 * Contributors:
 *     Robert Vandervoort (https://github.com/robertvandervoort)
 *     James Pansarasa (https://gitlab.com/jpansarasa)
 *
 * Last update: August 14th, 2017
 * Hosted at: https://gitlab.bdfoster.com/bdfoster/SmartThings/raw/master/device-handlers/AeonLabsAeotecSmartDimmer6.groovy
 */

 metadata {
  definition (name: "Aeon Labs Aeotec Smart Dimmer 6", namespace: "bdfoster", author: "Brian Foster") {
    capability "Energy Meter"
    capability "Power Meter"
    capability "Actuator"
    capability "Switch"
    capability "Switch Level"
    capability "Configuration"
    capability "Polling"
    capability "Refresh"
    capability "Sensor"
    capability "Color Control"
    capability "Tone"

    command "blink"

    /* Capability notes
    0x26 COMMAND_CLASS_SWITCH_MULTILEVEL 2
    0x27 COMMAND_CLASS_SWITCH_ALL 1
    0x32 COMMAND_CLASS_METER 3
    0x33 COMMAND_CLASS_COLOR_SWITCH 3
    0x59 COMMAND_CLASS_ASSOCIATION_GRP_INFO  1
    0x5A COMMAND_CLASS_DEVICE_RESET_LOCALLY 1
    0x5E COMMAND_CLASS_ZWAVE_PLUS_INFO 2
    0x60 Multi Channel Command Class (V3)
    0x70 COMMAND_CLASS_CONFIGURATION 1
    0x72 COMMAND_CLASS_MANUFACTURER_SPECIFIC 2
    0x73 COMMAND_CLASS_POWERLEVEL 1
    0x7A COMMAND_CLASS_FIRMWARE_UPDATE_MD 2
    0x81 COMMAND_CLASS_CLOCK 1
    0x82 COMMAND_CLASS_HAIL 1
    0x8E COMMAND_CLASS_MULTI_INSTANCE_ASSOCIATION 2
    0x85 COMMAND_CLASS_ASSOCIATION 2
    0x86 COMMAND_CLASS_VERSION 2
    0xEF COMMAND_CLASS_MARK 1
    */

    fingerprint deviceId: "0x1101", inClusters: "0x98"
    fingerprint inClusters: "0x26,0x27,0x32,0x33,0x59,0x5A,0x5E,0x60,0x70,0x72,0x73,0x7A,0x81,0x8E,0x85,0x86,0xEF", outClusters: "0x82"
  }

  // simulator metadata
  simulator {
    status "on":  "command: 2603, payload: FF"
    status "off": "command: 2603, payload: 00"
    status "09%": "command: 2603, payload: 09"
    status "10%": "command: 2603, payload: 0A"
    status "33%": "command: 2603, payload: 21"
    status "66%": "command: 2603, payload: 42"
    status "99%": "command: 2603, payload: 63"
    
    for (int i = 0; i <= 10000; i += 1000) {
      status "power  ${i} W":
        new physicalgraph.zwave.Zwave().meterV3.meterReport(scaledMeterValue: i, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()
    }
      
    for (int i = 0; i <= 100; i += 10) {
      status "energy  ${i} kWh":
        new physicalgraph.zwave.Zwave().meterV3.meterReport(scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
    }
      
    // reply messages
    ["FF", "00", "09", "0A", "21", "42", "63"].each { val ->
      reply "2001$val,delay 100,2602": "command: 2603, payload: $val"
    }
  }
    
  tiles (scale: 2) {
      multiAttributeTile(name:"summary", type:"lighting", width:6, height:4, canChangeIcon: true) {
        tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
          attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "turningOff"
          attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
          attributeState "turningOn", label:'Turning on', icon:"st.switches.switch.on", backgroundColor:"#ffffff", nextState: "turningOff"
          attributeState "turningOff", label:'Turning off', icon:"st.switches.switch.off", backgroundColor:"#79b821", nextState: "turningOn"
        }

        tileAttribute("device.level", key: "SLIDER_CONTROL", range: "0..99") {
          attributeState "level", action:"setLevel"
        }

        tileAttribute("device.color", key: "COLOR_CONTROL") {
          attributeState "color", action: "setColor"
        }
    }
    
    standardTile("refresh", "refresh.refresh", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
      state "default", label: "", icon: "st.secondary.refresh", action: "refresh"
    }
    
    valueTile("power", "device.power", decoration: "flat", width: 2, height: 1) {
    	state "power", label: '${currentValue} W'
    }
    
    valueTile("current", "device.current", decoration: "flat", width: 2, height: 1) {
      state "current", label:'${currentValue} A', action:"poll"
    }
    
    valueTile("voltage", "device.voltage", decoration: "flat", width: 2, height: 1) {
      state "voltage", label:'${currentValue} V', action:"poll"
    }
        
    valueTile("energy", "device.energy", width: 4, height: 1) {
      state "energy", label: '${currentValue} kWh'
    }
        
    standardTile("reset", "device.energy", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
      state "default", label: "Reset", action: "reset"
    }

    standardTile("configure", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
      state "configure", label:"Configure", action:"configure", icon:"st.secondary.preferences"
    }

    standardTile("blink", "device.blink", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
      state "default", label: "Blink", action: "blink"
    }

    main(["summary"])
    details(["summary", "power", "current", "voltage", "refresh", "energy", "reset", "configure", "blink"])
  }

  preferences {
    input "ledBehavior", "enum",
      title: "LED Behavior",
      description: "The LED on the device can react to changes in energy and status or serve as a night light when the load is switched off.",
      options: ["Energy Tracking", "Momentary Status", "Night Light"],
      displayDuringSetup: true,
      defaultValue: "Energy Tracking"
      required: true

    input "ledBrightness", "number",
      title: "LED Brightness",
      description: "Set the % brightness of indicator LEDs",
      defaultValue: 100,
      range: "0..100",
      required: false

    input "blinkDurationSeconds", "number",
      title: "Blink Duration",
      description: "Set the number of seconds to blink",
      range: "1..255",
      defaultValue: 5,
      required: true,
      displayDuringSetup: false

    input "blinkCycleSeconds", "decimal",
      title: "Blink Cycle Duration",
      description: "Set the duration for each cycle in seconds",
      defaultValue: 0.5,
      required: true,
      displayDuringSetup: false
  }
}

def updated() {
  if (state.sec && !isConfigured()) {
    // in case we miss the SCSR
    response(configure())
  }
}

def parse(String description) {
  def result = null
  
  if (description.startsWith("Err 106")) {
    state.sec = 0
    result = createEvent( name: "secureInclusion", value: "failed", isStateChange: true,
    descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.")
  } else if (description != "updated") {
    def cmd = zwave.parse(description, [0x25: 1, 0x26: 1, 0x27: 1, 0x32: 3, 0x33: 3, 0x59: 1, 0x70: 1, 0x72: 2, 0x73: 1, 0x82: 1, 0x85: 2, 0x86: 2])
    if (cmd) {
      result = zwaveEvent(cmd)
    }
  }
  log.debug "Parsed '${description}' to ${result.inspect()}"
  return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
  def encapsulatedCommand = cmd.encapsulatedCommand([0x25: 1, 0x26: 1, 0x27: 1, 0x32: 3, 0x33: 3, 0x59: 1, 0x70: 1, 0x72: 2, 0x73: 1, 0x82: 1, 0x85: 2, 0x86: 2])
  state.sec = 1
  log.debug "Received security encapsulated command: ${encapsulatedCommand}"
  
  if (encapsulatedCommand) {
    zwaveEvent(encapsulatedCommand)
  } else {
    log.warn "Unable to extract encapsulated cmd from $cmd"
    createEvent(descriptionText: cmd.toString())
  }
}


def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
  response(configure())
}

def zwaveEvent(physicalgraph.zwave.commands.powerlevelv1.PowerlevelTestNodeReport cmd) {
  log.debug "Power level test ${cmd.statusOfOperation},with ${cmd.testFrameCount} test frames on node ${cmd.testNodeid}"
  def request = [
    physicalgraph.zwave.commands.powerlevelv1.PowerlevelGet()
  ]
  response(commands(request))
}

// CONFIGURATION EVENTS =======================================================================================

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
  log.debug "Configuration (V2) parameter ${cmd.parameterNumber} with a byte size of ${cmd.size} is set to ${cmd.configurationValue}"
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
  log.debug "Configuration (V1) parameter ${cmd.parameterNumber} with a byte size of ${cmd.size} is set to ${cmd.configurationValue}"
}

// METER EVENTS ===============================================================================================

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
  def meterTypes = ["Unknown", "Electric", "Gas", "Water"]
  def electricNames = ["energy", "energy", "power", "count",  "voltage", "current", "powerFactor",  "unknown"]
  def electricUnits = ["kWh",    "kVAh",   "W",     "pulses", "V",       "A",       "Power Factor", ""]

  // NOTE ScaledPreviousMeterValue does not always contain a value
  def previousValue = cmd.scaledPreviousMeterValue ?: 0

  def map = [ name: electricNames[cmd.scale], unit: electricUnits[cmd.scale], displayed: false]
  switch(cmd.scale) {
    case 0: //kWh
      previousValue = device.currentValue("energy") ?: cmd.scaledPreviousMeterValue ?: 0
      map.value = cmd.scaledMeterValue
      break;
    case 1: //kVAh
      map.value = cmd.scaledMeterValue
      break;
    case 2: //Watts
      previousValue = device.currentValue("power") ?: cmd.scaledPreviousMeterValue ?: 0
      map.value = Math.round(cmd.scaledMeterValue)
      break;
    case 3: //pulses
      map.value = Math.round(cmd.scaledMeterValue)
      break;
    case 4: //Volts
      previousValue = device.currentValue("voltage") ?: cmd.scaledPreviousMeterValue ?: 0
      map.value = cmd.scaledMeterValue
      break;
    case 5: //Amps
      previousValue = device.currentValue("current") ?: cmd.scaledPreviousMeterValue ?: 0
      map.value = cmd.scaledMeterValue
      break;
    case 6: //Power Factor
      previousValue = device.currentValue("powerFactor") ?: cmd.scaledPreviousMeterValue ?: 0
      map.value = cmd.scaledMeterValue
      break;
    case 7: //Unknown
      map.value = cmd.scaledMeterValue
      break;
    default:
      break;
  }
  
  createEvent(map)
}

// SWITCH EVENTS ==============================================================================================

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
  switchEvent(cmd, 'physical')
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
  switchEvent(cmd, 'physical')
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
  switchEvent(cmd, 'digital')
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinarySet cmd) {
  switchEvent(cmd, 'digital')
}

def switchEvent(cmd, type) {
  def previousValue = device.currentValue('switch')
  def currentValue = cmd.value ? "on" : "off"
  def isStateChange = previousValue == currentValue ? false : true
  
  if (isStateChange) {
    log.info "${device.displayName} switch is ${currentValue} (${cmd.value})"
  }
  
  createEvent([name: "switch", value: currentValue, type: type, displayed: isStateChange, isStateChange: isStateChange])
}

// LEVEL EVENTS ===============================================================================================

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
  levelEvent(cmd, 'digital')
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd) {
  levelEvent(cmd, 'digital')
}

def levelEvent(cmd, type) {
  def previousValue = device.currentValue('level')
  def isStateChange = previousValue == cmd.value ? false : true
  
  if (isStateChange) {
    log.info "${device.displayName} level is ${cmd.value}%"
  }
  
  if (cmd.value >= 0 && cmd.value < 99) {
    createEvent([name: "level", value: cmd.value, type: type, unit: '%', displayed: isStateChange, isStateChange: isStateChange])
  } else if (cmd.value >= 99) {
    createEvent([name: "level", value: 100, type: type, unit: '%', displayed: isStateChange, isStateChange: isStateChange])
  } else {
    refresh()
  }
}

// UNKNOWN EVENTS =============================================================================================

def zwaveEvent(physicalgraph.zwave.Command cmd) {
  log.debug "Unhandled: $cmd sent"
  createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

// COMMANDS ===================================================================================================

def on() {
  log.debug "Turning on ${device.displayName}"
  setLevel(0xFF)
}

def off() {
  log.debug "Turning off ${device.displayName}"
  setLevel(0x00)
}

def blink() {  
  def blinkCycleSecondsValue = (blinkCycleSeconds * 10) as Integer
  log.debug "Blinking ${device.displayName} for $blinkDurationSeconds seconds"
  command(zwave.configurationV1.configurationSet(parameterNumber: 2, size: 2, configurationValue: [blinkDurationSeconds, blinkCycleSecondsValue]))
}

def beep() {
  blink()
}

def setLevel(value) {
  if (value == 0xFF) {
    value = 255
  }

  if (value == 0x00 || value < 0) {
    value = 0
  }

  if (value > 0 && value < 255) {
    value = Math.min(value as Integer, 99)
  } else {
    return command(zwave.switchMultilevelV2.switchMultilevelSet(value: value, dimmingDuration: 0))
  }
  
  command(zwave.switchMultilevelV2.switchMultilevelSet(value: value))
}

def setColor(value) {
  def result = []
  log.debug "Setting color for Night Light on ${device.displayName} to ${value}"
  
  if (value.hex) {
    def c = value.hex.findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) }
    result << zwave.configurationV1.configurationSet(parameterNumber: 83, size: 3, configurationValue: [c[0], c[1], c[2]])
  }
  
  if (value.hex) {
    sendEvent(name: "color", value: value.hex)
  }
  
  commands(result)
}

def poll() {
  log.debug "Sending poll request"
  
  def request = [
    zwave.switchBinaryV1.switchBinaryGet(),
    zwave.basicV1.basicGet(),
    zwave.switchMultilevelV1.switchMultilevelGet(),
    zwave.meterV3.meterGet(scale: 0),  //kWh
    zwave.meterV3.meterGet(scale: 1),  //kVAh
    zwave.meterV3.meterGet(scale: 2),  //Wattage
    zwave.meterV3.meterGet(scale: 4),  //Volts
    zwave.meterV3.meterGet(scale: 5),  //Amps
    zwave.meterV3.meterGet(scale: 6)   //Power Factor
  ]
  
  commands(request)
}

def refresh() {
  poll()
}

def reset() {
  log.debug "Sending meter reset command to ${device.displayName}"
  sendEvent([name: 'reset', descriptionText: '${device.displayName} energy meter has been reset', displayed: true, isStateChange: true])
  def request = [
    zwave.meterV3.meterReset(),
    zwave.meterV3.meterGet(scale: 0),  //kWh
    zwave.meterV3.meterGet(scale: 1),  //kVAh
    zwave.meterV3.meterGet(scale: 2),  //Wattage
    zwave.meterV3.meterGet(scale: 4),  //Volts
    zwave.meterV3.meterGet(scale: 5),  //Amps
    zwave.meterV3.meterGet(scale: 6)   //Power Factor
  ]
  
  sendEvent([name: "resetDate", value: new Date().format("MMM d, Y h:mm a", location.timeZone), displayed: false])
  sendEvent([name: "powerLow", value: null, unit: 'W'])
  sendEvent([name: "powerHigh", value: null, unit: 'W'])
  commands(request)
}

def configure() {
  if (state.sec) {
    log.debug "Secure configuration being set to ${device.displayName}"
  } else {
    log.debug "Non-secure configuration being sent to ${device.displayName}"
  }
  
  def monitorInterval = 60 as Integer
  def ledBehaviorValue;
  
  switch (ledBehavior) {
  	case "Momentary Status":
      ledBehaviorValue = 1
      break;
    case "Night Light":
      ledBehaviorValue = 2
      break;
    case "Energy Tracking":
    default:
      ledBehaviorValue = 0
      break;
  }
      
  log.debug "LED Behavior is set to ${ledBehavior} (${ledBehaviorValue})"
  def request = [
    // Reset switch configuration to defaults
    zwave.configurationV1.configurationSet(parameterNumber: 255, size: 1, scaledConfigurationValue: 1),
    
    // Enable to send notifications to associated devices (Group 1) when the state of Micro Switch’s load changed (0=nothing, 1=hail CC, 2=basic CC report)
    zwave.configurationV1.configurationSet(parameterNumber: 80, size: 1, configurationValue: [2]),
    
    // set LED behavior 0 energy mode 1 momentary display 2 night light
    zwave.configurationV1.configurationSet(parameterNumber: 81, size: 1, scaledConfigurationValue: ledBehaviorValue),
    
    // Set LED brightness
    zwave.configurationV1.configurationSet(parameterNumber: 84, size: 3, configurationValue: [ledBrightness,ledBrightness,ledBrightness]),
    
    // Send report on wattage change
    zwave.configurationV1.configurationSet(parameterNumber: 90, size: 1, scaledConfigurationValue: 1),
    
    // Send report on minimum of 2% of change in wattage
    zwave.configurationV1.configurationSet(parameterNumber: 92, size: 1, scaledConfigurationValue: 2),
    
    // Which reports need to send in Report group 1
    zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 4|2|1),
    
    // Which reports need to send in Report group 2
    zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 8),
    
    // Which reports need to send in Report group 3
    zwave.configurationV1.configurationSet(parameterNumber: 103, size: 4, scaledConfigurationValue: 0),
    
    // Interval to send Report group 1
    zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: monitorInterval),
    
    // Interval to send Report group 2
    zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: 60),
    
    // Interval to send Report group 3
    zwave.configurationV1.configurationSet(parameterNumber: 113, size: 4, scaledConfigurationValue: 0),
    
    // get notification behavior
    zwave.configurationV1.configurationGet(parameterNumber: 80),
    
    // get LED behavior
    zwave.configurationV1.configurationGet(parameterNumber: 81),
    
    // get night light RGB value
    zwave.configurationV1.configurationGet(parameterNumber: 83),
    
    // get Energy Mode/momentary indicate LED brightness value
    zwave.configurationV1.configurationGet(parameterNumber: 84),
    
    // Which reports need to send in Report group 1
    zwave.configurationV1.configurationGet(parameterNumber: 101),
    
    // Which reports need to send in Report group 2
    zwave.configurationV1.configurationGet(parameterNumber: 102),
    
    // Which reports need to send in Report group 3
    zwave.configurationV1.configurationGet(parameterNumber: 103),
    
    // Interval to send Report group 1
    zwave.configurationV1.configurationGet(parameterNumber: 111),
    
    // Interval to send Report group 2
    zwave.configurationV1.configurationGet(parameterNumber: 112),
    
    // Interval to send Report group 3
    zwave.configurationV1.configurationGet(parameterNumber: 113),

    // Can use the zwaveHubNodeId variable to add the hub to the device's associations:
    zwave.associationV1.associationSet(groupingIdentifier: 1, nodeId: zwaveHubNodeId),
    zwave.associationV1.associationSet(groupingIdentifier: 2, nodeId: zwaveHubNodeId),
    zwave.associationV1.associationSet(groupingIdentifier: 3, nodeId: zwaveHubNodeId)
  ]
  commands(request)
}

// HELPERS ====================================================================================================
private command(physicalgraph.zwave.Command cmd) {
  if (state.sec) {
    zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
  } else {
    cmd.format()
  }
}

private commands(commands, delay=500) {
  delayBetween(commands.collect{ command(it) }, delay)
}

/**
 * Compute the dimming duration based on the `stepsPerSecond` preference and the number of steps to take.
 * For example:
 *     if `stepsPerSecond` is 20 and there is 20 steps to take: 1 second
 *     if `stepsPerSecond` is 20 and there is 10 steps to take: 0.5 seconds
 *     if `stepsPerSecond` is 20 and there is 30 steps to take: 1.5 seconds
 *
 * The duration is then encoded per the technical specs of the command class.
 */
private computeDimmingDuration(Integer steps) {
  def stepsPerSecond = device.currentValue("stepsPerSecond")

  if (stepsPerSecond == null) {
    stepsPerSecond = 30
  }

  //log.debug "Computing dimming duration with $steps steps and $stepsPerSecond per second"

  def durationSeconds = (steps / stepsPerSecond)
  return durationSeconds < 128 ? durationSeconds : 128 + Math.round(durationSeconds / 60)
}