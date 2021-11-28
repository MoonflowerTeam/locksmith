package gg.moonflower.locksmith.common.lock;

import gg.moonflower.locksmith.api.lock.AbstractLock;
import gg.moonflower.locksmith.client.lock.ClientLockManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface LockManager {

    /**
     * Gets a lock manager for a level.
     *
     * @param level The level storing the locks.
     * @return The lock manager.
     */
    static LockManager get(Level level) {
        return level.isClientSide() ? ClientLockManager.getOrCreate((ClientLevel) level) : ServerLockManager.getOrCreate((ServerLevel) level);
    }

    /**
     * Gets the lock at a specified position.
     * <p>Used to support doors and chests.
     *
     * @param level The level of the lock.
     * @param pos   The position of the lock
     * @return <code>null</code> if there is no lock at this position or the other half of a door/chest, otherwise returns the lock.
     */
    @Nullable
    static AbstractLock getLock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Direction chestDirection = null;
        if (state.hasProperty(ChestBlock.TYPE) && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            chestDirection = ChestBlock.getConnectedDirection(state);
        }

        LockManager manager = LockManager.get(level);
        AbstractLock lock = manager.getLock(pos);
        if (lock != null)
            return lock;

        if (chestDirection != null) {
            return manager.getLock(pos.relative(chestDirection));
        }

        if (state.getBlock() instanceof DoorBlock) {
            boolean top = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER;
            if (top) {
                return manager.getLock(pos.relative(Direction.DOWN));
            } else {
                return manager.getLock(pos.relative(Direction.UP));
            }
        }

        return null;
    }

    Collection<AbstractLock> getLocks(ChunkPos chunkPos);

    @Nullable
    AbstractLock getLock(BlockPos pos);

    void addLock(AbstractLock data);

    void removeLock(BlockPos pos);

}