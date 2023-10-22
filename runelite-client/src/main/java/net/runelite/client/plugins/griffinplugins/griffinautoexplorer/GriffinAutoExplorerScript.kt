package net.runelite.client.plugins.griffinplugins.griffinautoexplorer

import net.runelite.api.coords.WorldArea
import net.runelite.api.coords.WorldPoint
import net.runelite.client.plugins.microbot.Microbot
import net.runelite.client.plugins.microbot.Script
import net.runelite.client.plugins.microbot.staticwalker.pathfinder.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

class GriffinAutoExplorerScript : Script() {
    lateinit var usedWorldPoints: MutableList<WorldPoint>


    fun run(config: GriffinAutoExplorerConfig): Boolean {
        usedWorldPoints = mutableListOf()
//        val worldArea: WorldArea = parseWorldArea(config.worldArea())
        val worldArea = WorldArea(1408, 3728, 109, 161, 0)
        val player = Microbot.getClientForKotlin().localPlayer
        val liveWorldDataLoader = LiveWorldDataLoader()
        var quit = false

        scheduledExecutorService.scheduleWithFixedDelay({
            if (!super.run()) {
                quit = true
            }

            if (quit) {
                scheduledExecutorService.shutdown()
                return@scheduleWithFixedDelay
            }

            val unexploredPoint = findFarthestUnknownPoint(worldArea)
            if (unexploredPoint == null) {
                quit = true
                Microbot.getNotifierForKotlin().notify("No unexplored points found")
                return@scheduleWithFixedDelay

            } else if (usedWorldPoints.contains(unexploredPoint)) {
                println("Unexplored point already used")
                return@scheduleWithFixedDelay
            } else {
                println("Unexplored point: $unexploredPoint")
                usedWorldPoints.add(unexploredPoint)
            }

            var distance = player.worldLocation.distanceTo(unexploredPoint)

            while (distance > 5) {
                val nodeMap = liveWorldDataLoader.getNodeMap()
                val farthestPoint = getFarthestWalkablePoint(unexploredPoint, nodeMap)
                if (farthestPoint == null) {
                    quit = true
                    Microbot.getNotifierForKotlin().notify("No walkable points found, copied remaining coordinates to clipboard")
                    break

                } else {
                    Microbot.getWalkerForKotlin().hybridWalkTo(farthestPoint, true)
                    val pathFinder = PathFinder(nodeMap)
                    val path = pathFinder.findPath(player.worldLocation, farthestPoint)
                    if (PathWalker.getIsInterrupted()) {
                        quit = true
                        break
                    }

                    if (path.isEmpty()) {
                        break
                    }

                    val pathWalker = PathWalker(path)
                    pathWalker.walkPath()
                }

                if (PathWalker.getIsInterrupted()) {
                    quit = true
                    break
                }

                if (GriffinAutoExplorerPlugin.interrupt) {
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

    private fun parseWorldArea(input: String): WorldArea {
        val parts = input.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val x = parts[0].toInt()
        val y = parts[1].toInt()
        val width = parts[2].toInt()
        val height = parts[3].toInt()
        val plane = parts[3].toInt()
        return WorldArea(x, y, width, height, plane)
    }

    private fun findFarthestUnknownPoint(worldArea: WorldArea): WorldPoint? {
        val savedWorldDataLoader = SavedWorldDataLoader(WorldDataDownloader.worldDataFile)
        val pathNodeMap: Map<String, PathNode> = savedWorldDataLoader.getNodeMap()

        val inAreaNodes = getNodesWithinWorldArea(worldArea, pathNodeMap.values.toList())
        val farthestPoint = findFarthestPointFromWalkableNodes(worldArea, inAreaNodes)
        if (farthestPoint == null) {
            println("No farthest point found")
            return null
        }

        return farthestPoint
    }

    private fun getNodesWithinWorldArea(worldArea: WorldArea, nodes: List<PathNode>): List<PathNode> {
        val nodesWithinWorldArea: MutableList<PathNode> = mutableListOf()
        for (node in nodes) {
            if (worldArea.contains(node.worldLocation)) {
                nodesWithinWorldArea.add(node)
            }
        }
        return nodesWithinWorldArea
    }

    private fun findFarthestPointFromWalkableNodes(worldArea: WorldArea, inAreaNodes: List<PathNode>): WorldPoint? {
        val walkableNodes: MutableList<PathNode> = mutableListOf()
        for (node in inAreaNodes) {
            if (node.blocked) {
                continue
            }
            if (node.blockedMovementEast && node.blockedMovementWest && node.blockedMovementNorth && node.blockedMovementSouth) {
                continue
            }
            walkableNodes.add(node)
        }

        if (walkableNodes.isEmpty()) {
            return null
        }

        val walkableNodesMap: MutableMap<String, PathNode> = mutableMapOf()
        for (node in walkableNodes) {
            walkableNodesMap["${node.worldLocation.x}_${node.worldLocation.y}_${node.worldLocation.plane}"] = node
        }

        val unknownWorldPoints = mutableListOf<WorldPoint>()
        for (x in worldArea.x until worldArea.x + worldArea.width) {
            for (y in worldArea.y until worldArea.y + worldArea.height) {
                val node = walkableNodesMap["${x}_${y}_${worldArea.plane}"]
                if (node == null) {
                    unknownWorldPoints.add(WorldPoint(x, y, worldArea.plane))
                }
            }
        }

        if (unknownWorldPoints.isEmpty()) {
            return null
        }

        val unknownFarthestDistanceMap: MutableMap<WorldPoint, Int> = mutableMapOf()
        for (unknownWorldPoint in unknownWorldPoints) {
            var farthestDistance = 0
            for (walkableNode in walkableNodes) {
                val distance = unknownWorldPoint.distanceTo(walkableNode.worldLocation)
                if (distance > farthestDistance) {
                    farthestDistance = distance
                }
            }
            unknownFarthestDistanceMap[unknownWorldPoint] = farthestDistance
        }

        val cleanedUnknownFarthestPoints = unknownFarthestDistanceMap
            .filter { it.value > 0 }
            .filter { !usedWorldPoints.contains(it.key) }

        val farthestWorldPoint = cleanedUnknownFarthestPoints.maxByOrNull { it.value }?.key
        return farthestWorldPoint
    }
}