package com.rishi.remindly.ui.model

import com.rishi.remindly.R

enum class SoundOption(val key: String, val label: String, val rawRes: Int?) {
    DEFAULT("default", "Phone Default", null),
    SOFT_BELL("soft_bell", "Soft Bell", R.raw.soft_bell),
    CLEAR_PING("clear_ping", "Clear Ping", R.raw.clear_ping),
    GENTLE_CHIME("gentle_chime", "Gentle Chime", R.raw.gentle_chime),
    DIGITAL_ALERT("digital_alert", "Digital Alert", R.raw.digital_alert),
    WARM_TONE("warm_tone", "Warm Tone", R.raw.warm_tone),
    URGENT_BEEP("urgent_beep", "Urgent Beep", R.raw.urgent_beep);

    companion object {
        fun fromKey(key: String): SoundOption =
            entries.firstOrNull { it.key == key } ?: DEFAULT
    }
}
