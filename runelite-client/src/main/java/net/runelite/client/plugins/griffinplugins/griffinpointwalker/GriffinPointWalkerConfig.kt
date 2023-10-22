package net.runelite.client.plugins.griffinplugins.griffinpointwalker

import net.runelite.client.config.Config
import net.runelite.client.config.ConfigGroup
import net.runelite.client.config.ConfigItem
import net.runelite.client.config.ConfigSection

@ConfigGroup(GriffinPointWalkerPlugin.CONFIG_GROUP)
interface GriffinPointWalkerConfig : Config {
    companion object {
        @ConfigSection(
            name = "General",
            description = "General",
            position = 0,
            closedByDefault = false
        )
        const val generalSection = "general"
    }

    @ConfigItem(
        keyName = "points",
        name = "Points",
        description = "Points, each line is x,y,z",
        position = 0,
        section = generalSection
    )
    fun points(): String {
        return ""
    }
}