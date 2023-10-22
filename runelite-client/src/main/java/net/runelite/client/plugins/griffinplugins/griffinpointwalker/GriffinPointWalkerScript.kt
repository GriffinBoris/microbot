package net.runelite.client.plugins.griffinplugins.griffinpointwalker

import net.runelite.api.coords.WorldPoint
import net.runelite.client.plugins.microbot.Microbot
import net.runelite.client.plugins.microbot.Script
import net.runelite.client.plugins.microbot.staticwalker.pathfinder.LiveWorldDataLoader
import net.runelite.client.plugins.microbot.staticwalker.pathfinder.PathFinder
import net.runelite.client.plugins.microbot.staticwalker.pathfinder.PathNode
import net.runelite.client.plugins.microbot.staticwalker.pathfinder.PathWalker
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

class GriffinPointWalkerScript : Script() {


    fun run(config: GriffinPointWalkerConfig): Boolean {
        val unexploredPoints: MutableList<WorldPoint> = parseCoordinates(config.points())
        val player = Microbot.getClientForKotlin().localPlayer
        val liveWorldDataLoader = LiveWorldDataLoader()
        var quit = false

        scheduledExecutorService.scheduleWithFixedDelay({
            if (!super.run()) {
                quit = true
            }

            if (unexploredPoints.isEmpty()) {
                quit = true
            }

            if (quit) {
                scheduledExecutorService.shutdown()
                return@scheduleWithFixedDelay
            }

            val unexploredPoint = unexploredPoints.removeAt(0)
            var distance = player.worldLocation.distanceTo(unexploredPoint)

            while (distance > 5) {
                val nodeMap = liveWorldDataLoader.getNodeMap()
                val farthestPoint = getFarthestWalkablePoint(unexploredPoint, nodeMap)
                if (farthestPoint == null) {
                    quit = true
                    printRemainingCoordinates(unexploredPoints)
                    Microbot.getNotifierForKotlin().notify("No walkable points found, copied remaining coordinates to clipboard")
                    break

                } else {
                    val pathFinder = PathFinder(nodeMap)
                    val path = pathFinder.findPath(player.worldLocation, farthestPoint)
                    val pathWalker = PathWalker(path)
                    pathWalker.walkPath()
                }

                if (PathWalker.getIsInterrupted()) {
                    quit = true
                    break
                }

                if (GriffinPointWalkerPlugin.interrupt) {
                    println("Interrupted")
                    quit = true
                    break
                }

                distance = player.worldLocation.distanceTo(unexploredPoint)
            }

            if (quit) {
                scheduledExecutorService.shutdown()
                return@scheduleWithFixedDelay
            }

        }, 0, 500, TimeUnit.MILLISECONDS)

        return true
    }

    private fun getFarthestWalkablePoint(target: WorldPoint, nodeMap: MutableMap<String, PathNode>): WorldPoint? {
        var walkablePoints: MutableList<WorldPoint> = ArrayList()

        for (node in nodeMap.values) {
            if (node.blocked) {
                continue
            }
            if (node.blockedMovementEast && node.blockedMovementWest && node.blockedMovementNorth && node.blockedMovementSouth) {
                continue
            }
            walkablePoints.add(node.worldLocation)
        }

        if (walkablePoints.isEmpty()) {
            return null
        }

        walkablePoints = walkablePoints.stream().sorted(Comparator.comparingInt { wp: WorldPoint -> wp.distanceTo(target) }).collect(Collectors.toList())
        return walkablePoints[0]
    }

    private fun parseCoordinates(input: String): MutableList<WorldPoint> {
        val coordinates: MutableList<WorldPoint> = ArrayList()
        val lines = input.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (line in lines) {
            val parts = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (parts.size == 3) {
                val x = parts[0].toInt()
                val y = parts[1].toInt()
                val z = parts[2].toInt()
                coordinates.add(WorldPoint(x, y, z))
            }
        }
        return coordinates
    }

    private fun printRemainingCoordinates(coordinates: List<WorldPoint>) {
        var coordinateString = ""
        val stringSelection = StringSelection(coordinateString)
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(stringSelection, null)

        println("====================================")
        println("Remaining Coordinates:")
        for (coordinate in coordinates) {
            coordinateString += coordinate.x.toString() + "," + coordinate.y + "," + coordinate.plane + "\n"
        }
        println(coordinateString)
        println("====================================")
    }
}