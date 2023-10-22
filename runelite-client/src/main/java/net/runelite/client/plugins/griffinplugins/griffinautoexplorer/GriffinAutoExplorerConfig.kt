package net.runelite.client.plugins.griffinplugins.griffinautoexplorer

import net.runelite.client.config.Config
import net.runelite.client.config.ConfigGroup
import net.runelite.client.config.ConfigItem
import net.runelite.client.config.ConfigSection

@ConfigGroup(GriffinAutoExplorerPlugin.CONFIG_GROUP)
interface GriffinAutoExplorerConfig : Config {
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
        keyName = "worldArea",
        name = "World Area",
        description = "World Area, x1,y1,x2,y2",
        position = 0,
        section = generalSection
    )
    fun worldArea(): String {
        return ""
    }
}