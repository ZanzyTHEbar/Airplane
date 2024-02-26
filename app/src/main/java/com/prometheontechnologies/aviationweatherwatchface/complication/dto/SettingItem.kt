package com.prometheontechnologies.aviationweatherwatchface.complication.dto

import kotlinx.serialization.ExperimentalSerializationApi

data class SettingItem(
    val id: Int,
    val title: String,
    val description: String,
    val enabled: Boolean = false,
    val type: SettingType = SettingType.SWITCH,
)

enum class SettingType {
    SWITCH,
    BUTTON,
    DROPDOWN,
}

/**
 * Create a list of [SettingItem] available from the [ComplicationsSettingsStore]
 */
@OptIn(ExperimentalSerializationApi::class)
fun createSettingsList(): List<SettingItem> {
    val descriptor = ComplicationsSettingsStore.serializer().descriptor
    val settingsAvailable = mutableListOf<SettingItem>()

    for (i in 0 until descriptor.elementsCount) {
        val propertyName = descriptor.getElementName(i)

        if (propertyName == "weatherServiceUpdatePeriod") continue

        settingsAvailable.add(
            SettingItem(
                id = propertyName.hashCode(),
                title = propertyName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    .replace(Regex("([A-Z])"), " $1").trim(),
                description = "Enable or disable $propertyName",
                enabled = true,
                type = SettingType.SWITCH
            )
        )
    }

    return settingsAvailable
}
