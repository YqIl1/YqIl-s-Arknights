package com.YqIl1.yqils_arknights.block.custom;

import com.YqIl1.yqils_arknights.block.entity.ModBlockEntities;
import com.YqIl1.yqils_arknights.block.entity.WeaponForgingStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class WeaponForgingStationBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final VoxelShape base = Block.box(0, 0D, 0, 16, 2, 16);
    public static final VoxelShape pillar = Block.box(7,0,1,9,14,15);
    // Shape as modeled for facing=NORTH (the model's unrotated orientation); other directions are derived from this
    public static final VoxelShape SHAPE = Shapes.or(base,pillar);

    private static final Map<Direction, VoxelShape> SHAPES_BY_FACING = new EnumMap<>(Direction.class);
    static {
        // SHAPE was visually built assuming NORTH, but the actual model geometry lines up with WEST instead
        // (off by one 90-degree step) -- adjust this reference direction if it's still rotated wrong
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            SHAPES_BY_FACING.put(direction, rotateShape(Direction.WEST, direction, SHAPE));
        }
    }

    public WeaponForgingStationBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPES_BY_FACING.get(pState.getValue(FACING));
    }

    // Rotates a VoxelShape in 90-degree steps around the Y axis; VoxelShape has no built-in rotate operation
    private static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{shape, Shapes.empty()};
        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    buffer[1] = Shapes.or(buffer[1], Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }
        return buffer[0];
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState pstate) {
        return RenderShape.MODEL;
    }


    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof WeaponForgingStationBlockEntity) {
                ((WeaponForgingStationBlockEntity) blockEntity).drops();
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if(entity instanceof WeaponForgingStationBlockEntity){
                NetworkHooks.openScreen(((ServerPlayer) pPlayer), (WeaponForgingStationBlockEntity)entity, pPos);
            }else{
                throw new IllegalStateException("Container provider missing");
            }
        }
        return InteractionResult.sidedSuccess(pLevel.isClientSide);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide) {
            return null;
        }

        return createTickerHelper(pBlockEntityType, ModBlockEntities.WEAPON_FORGING_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1, pPos, pState1));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {

        return new WeaponForgingStationBlockEntity(blockPos, blockState);
    }
}
