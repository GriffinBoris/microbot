package net.runelite.client.plugins.griffinplugins.griffinpointwalker

import com.google.inject.Provides
import net.runelite.client.config.ConfigManager
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDescriptor
import net.runelite.client.plugins.griffinplugins.griffintrainer.GriffinTrainerConfig
import net.runelite.client.plugins.microbot.Microbot
import net.runelite.client.plugins.microbot.staticwalker.pathfinder.PathWalker
import javax.inject.Inject

@PluginDescriptor(name = PluginDescriptor.Griffin + GriffinPointWalkerPlugin.CONFIG_GROUP, enabledByDefault = false)
class GriffinPointWalkerPlugin : Plugin() {
    companion object {
        const val CONFIG_GROUP = "Point Walker"
        var interrupt = false
    }

    @Inject
    private lateinit var config: GriffinPointWalkerConfig

    @Provides
    fun provideConfig(configManager: ConfigManager): GriffinPointWalkerConfig {
        return configManager.getConfig(GriffinPointWalkerConfig::class.java)
    }

    lateinit var pointWalkerScript: GriffinPointWalkerScript
    override fun startUp() {
        Microbot.enableAutoRunOn = false
        interrupt = false
        pointWalkerScript = GriffinPointWalkerScript()
        pointWalkerScript.run(config)
    }

    override fun shutDown() {
        pointWalkerScript.shutdown()
        interrupt = true
        PathWalker.interrupt()
        Microbot.enableAutoRunOn = true
    }
}
