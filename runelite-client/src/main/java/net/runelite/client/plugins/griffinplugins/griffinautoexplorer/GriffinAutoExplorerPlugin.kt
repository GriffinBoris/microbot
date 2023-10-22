package net.runelite.client.plugins.griffinplugins.griffinautoexplorer

import com.google.inject.Provides
import net.runelite.client.config.ConfigManager
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDescriptor
import net.runelite.client.plugins.microbot.Microbot
import net.runelite.client.plugins.microbot.staticwalker.pathfinder.PathWalker
import javax.inject.Inject

@PluginDescriptor(name = PluginDescriptor.Griffin + GriffinAutoExplorerPlugin.CONFIG_GROUP, enabledByDefault = false)
class GriffinAutoExplorerPlugin : Plugin() {
    companion object {
        const val CONFIG_GROUP = "Auto Explorer"
        var interrupt = false
    }

    @Inject
    private lateinit var config: GriffinAutoExplorerConfig

    @Provides
    fun provideConfig(configManager: ConfigManager): GriffinAutoExplorerConfig {
        return configManager.getConfig(GriffinAutoExplorerConfig::class.java)
    }

    lateinit var autoExplorerScript: GriffinAutoExplorerScript
    override fun startUp() {
        Microbot.enableAutoRunOn = false
        interrupt = false
        autoExplorerScript = GriffinAutoExplorerScript()
        autoExplorerScript.run(config)
    }

    override fun shutDown() {
        autoExplorerScript.shutdown()
        interrupt = true
        PathWalker.interrupt()
        Microbot.enableAutoRunOn = true
    }
}
