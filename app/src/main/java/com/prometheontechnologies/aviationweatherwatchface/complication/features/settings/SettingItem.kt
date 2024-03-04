package com.prometheontechnologies.aviationweatherwatchface.complication.features.settings

import kotlinx.serialization.ExperimentalSerializationApi

data class SettingItem(
    val id: Int,
    val title: String,
    val description: String,
    var enabled: Boolean = false,
    var checked: Boolean = false,
    val type: SettingType = SettingType.SWITCH,
)

enum class SettingType {
    SWITCH,
    BUTTON,
    DROPDOWN,
}

/**
 * Create a list of [SettingItem] available from the [UserPreferences] data class.
 */
@OptIn(ExperimentalSerializationApi::class)
fun createSettingsList(): List<SettingItem> {
    val descriptor = UserPreferences.serializer().descriptor

    val settingsAvailable = mutableListOf<SettingItem>()

    for (i in 0 until descriptor.elementsCount) {
        val propertyName = descriptor.getElementName(i)
        val type = when (propertyName) {
            "weatherServiceUpdatePeriod" -> SettingType.DROPDOWN
            else -> SettingType.SWITCH
        }

        val enabled = when (propertyName) {
            "isLeadingZeroTime" -> false
            "notificationsEnabled" -> false
            else -> true
        }

        val checked = when (propertyName) {
            "locationServiceEnabled" -> true
            else -> false
        }

        if (propertyName == "weatherServiceUpdatePeriod") {
            continue
        }

        var title =
            propertyName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                .replace(Regex("([A-Z])"), " $1").trim()

        // check if the title is greater than 2 words
        if (title.split(" ").size > 2) {
            // if it is, them remove everything after the second word
            val split = title.split(" ")
            val first = split[0]
            val second = split[1]
            title = "$first $second"
        }

        settingsAvailable.add(
            SettingItem(
                id = i,
                title = title,
                description = "Enable or disable $propertyName",
                enabled = enabled,
                checked = checked,
                type = type
            )
        )
    }

    return settingsAvailable
}
