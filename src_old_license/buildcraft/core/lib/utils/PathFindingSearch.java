/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.utils;

import java.util.*;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IZone;

public class PathFindingSearch implements IIterableAlgorithm {

    public static final int PATH_ITERATIONS = 1000;

    private static final HashMap<Integer, HashSet<BlockPos>> reservations = new HashMap<>();

    private World world;
    private BlockPos start;
    private List<PathFinding> pathFinders;
    private IBlockFilter pathFound;
    private IZone zone;
    private float maxDistance;
    private Iterator<BlockPos> blockIter;

    private double maxDistanceToEnd;

    public PathFindingSearch(World iWorld, BlockPos iStart, Iterator<BlockPos> iBlockIter, IBlockFilter iPathFound, double iMaxDistanceToEnd, float iMaxDistance, IZone iZone) {
        world = iWorld;
        start = iStart;
        pathFound = iPathFound;

        maxDistance = iMaxDistance;
        maxDistanceToEnd = iMaxDistanceToEnd;
        zone = iZone;
        blockIter = iBlockIter;

        pathFinders = new LinkedList<>();
    }

    @Override
    public void iterate() {
        if (pathFinders.size() < 5 && blockIter.hasNext()) {
            iterateSearch(PATH_ITERATIONS * 10);
        }
        iteratePathFind(PATH_ITERATIONS);
    }

    private void iterateSearch(int itNumber) {
        for (int i = 0; i < itNumber; ++i) {
            if (!blockIter.hasNext()) {
                return;
            }

            BlockPos delta = blockIter.next();
            BlockPos block = new BlockPos(start.getX() + delta.getX(), ((start.getY() + delta.getY()) > 0) ? start.getY() + delta.getY() : 0, start.getZ() + delta.getZ());
            if (isLoadedChunk(block.getX(), block.getZ())) {
                if (isTarget(block)) {
                    pathFinders.add(new PathFinding(world, start, block, maxDistanceToEnd, maxDistance));
                }
            }

            if (pathFinders.size() >= 5) {
                return;
            }
        }
    }

    private boolean isTarget(BlockPos block) {
        if (zone != null && !zone.contains(Utils.convert(block))) {
            return false;
        }
        if (!pathFound.matches(world, block)) {
            return false;
        }
        synchronized (reservations) {
            if (reservations.containsKey(world.provider.getDimension())) {
                HashSet<BlockPos> dimReservations = reservations.get(world.provider.getDimension());
                if (dimReservations.contains(block)) {
                    return false;
                }
            }
        }
        if (!BuildCraftAPI.isSoftBlock(world, block.west()) && !BuildCraftAPI.isSoftBlock(world, block.east()) && !BuildCraftAPI.isSoftBlock(world, block.north()) && !BuildCraftAPI.isSoftBlock(world, block.south()) && !BuildCraftAPI.isSoftBlock(
                world, block.down()) && !BuildCraftAPI.isSoftBlock(world, block.up())) {
            return false;
        }
        return true;
    }

    private boolean isLoadedChunk(int x, int z) {
        return world.isBlockLoaded(new BlockPos(x, 0, z));
        // return world.getChunkProvider().chunkExists(x >> 4, z >> 4);
    }

    public void iteratePathFind(int itNumber) {
        for (PathFinding pathFinding : new ArrayList<>(pathFinders)) {
            pathFinding.iterate(itNumber / pathFinders.size());
            if (pathFinding.isDone()) {
                LinkedList<BlockPos> path = pathFinding.getResult();
                if (path != null && path.size() > 0) {
                    if (reserve(pathFinding.end())) {
                        return;
                    }
                }
                pathFinders.remove(pathFinding);
            }
        }
    }

    @Override
    public boolean isDone() {
        for (PathFinding pathFinding : pathFinders) {
            if (pathFinding.isDone()) {
                return true;
            }
        }
        return !blockIter.hasNext();
    }

    public LinkedList<BlockPos> getResult() {
        for (PathFinding pathFinding : pathFinders) {
            if (pathFinding.isDone()) {
                return pathFinding.getResult();
            }
        }
        return new LinkedList<>();
    }

    public BlockPos getResultTarget() {
        for (PathFinding pathFinding : pathFinders) {
            if (pathFinding.isDone()) {
                return pathFinding.end();
            }
        }
        return null;
    }

    private boolean reserve(BlockPos block) {
        synchronized (reservations) {
            if (!reservations.containsKey(world.provider.getDimension())) {
                reservations.put(world.provider.getDimension(), new HashSet<BlockPos>());
            }
            HashSet<BlockPos> dimReservations = reservations.get(world.provider.getDimension());
            if (dimReservations.contains(block)) {
                return false;
            }
            dimReservations.add(block);
            return true;
        }
    }

    public void unreserve(BlockPos block) {
        synchronized (reservations) {
            if (reservations.containsKey(world.provider.getDimension())) {
                reservations.get(world.provider.getDimension()).remove(block);
            }
        }
    }
}
