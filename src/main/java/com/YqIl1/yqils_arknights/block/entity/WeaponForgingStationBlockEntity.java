package com.YqIl1.yqils_arknights.block.entity;

import com.YqIl1.yqils_arknights.recipe.WeaponForgeRecipe;
import com.YqIl1.yqils_arknights.screen.WeaponForgingStationMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class WeaponForgingStationBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler itemHandler = new ItemStackHandler(2){
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide()){
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };
    private LazyOptional<ItemStackHandler> lazyItemHandler = LazyOptional.empty();
    private final int INPUT_SLOT = 0;
    private final int OUTPUT_SLOT = 1;
    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress = 100;
    private int selectedRecipeIndex = -1;
    private boolean isProcessing = false;

    public WeaponForgingStationBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.WEAPON_FORGING_BE.get(),pPos, pBlockState);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> WeaponForgingStationBlockEntity.this.progress;
                    case 1 -> WeaponForgingStationBlockEntity.this.maxProgress;
                    case 2 -> WeaponForgingStationBlockEntity.this.selectedRecipeIndex;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex){
                    case 0 -> WeaponForgingStationBlockEntity.this.progress = pValue;
                    case 1 -> WeaponForgingStationBlockEntity.this.maxProgress = pValue;
                    case 2 -> WeaponForgingStationBlockEntity.this.selectedRecipeIndex = pValue;
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    public void drops(){
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for(int i = 0; i < itemHandler.getSlots(); i++){
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.yqils_arknights.weapon_forging_station");
    }



    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new WeaponForgingStationMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("inventory", itemHandler.serializeNBT());
        pTag.putInt("weapon_forging_station.progress", progress);
        pTag.putInt("weapon_forging_station.selected_recipe", selectedRecipeIndex);
        pTag.putBoolean("weapon_forging_station.processing", isProcessing);
        super.saveAdditional(pTag);
    }
    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemHandler.deserializeNBT(pTag.getCompound("inventory"));
        progress = pTag.getInt("weapon_forging_station.progress");
        selectedRecipeIndex = pTag.getInt("weapon_forging_station.selected_recipe");
        isProcessing = pTag.getBoolean("weapon_forging_station.processing");
    }

    public ItemStack getRenderStack() {
        if(itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty()){
            return itemHandler.getStackInSlot(INPUT_SLOT);
        }else{
            return itemHandler.getStackInSlot(OUTPUT_SLOT);
        }

    }

    // Separate from saveAdditional/load (disk persistence) -- this is what actually reaches the
    // client's copy of the block entity, needed since the renderer runs whether or not the GUI is open.
    // Must be the FULL state (same as disk save), not just processing/progress: Forge's default
    // handleUpdateTag() calls load(tag) on the client, and our load() deserializes "inventory" from
    // whatever tag it's given -- a partial tag here means load() wipes the client's item handler back
    // to empty every time an update packet arrives, racing against and clobbering the menu's own
    // (correct) slot sync.
    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public boolean isProcessing() {
        return isProcessing;
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    // All recipes whose input matches the current input slot -- the player picks one of these, rather than
    // the station auto-selecting the first match like a furnace would
    public List<WeaponForgeRecipe> getRecipesForInput() {
        SimpleContainer inventory = new SimpleContainer(this.itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        return this.level.getRecipeManager().getRecipesFor(WeaponForgeRecipe.Type.INSTANCE, inventory, level);
    }

    public void setSelectedRecipeIndex(int index) {
        this.selectedRecipeIndex = index;
        setChanged();
    }

    // Called when the player presses the Start button; does nothing if there's no valid selection or a craft is already running
    public void startProcessing() {
        if (isProcessing) {
            return;
        }
        if (!hasSelectedRecipe()) {
            return;
        }
        isProcessing = true;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (!isProcessing) {
            return;
        }

        if (!hasSelectedRecipe()) {
            // Input changed (or was removed) mid-craft and no longer matches the selected recipe
            resetProgress();
            isProcessing = false;
            setChanged(pLevel, pPos, pState);
            pLevel.sendBlockUpdated(pPos, pState, pState, 3);
            return;
        }

        increaseCraftingProgress();
        setChanged(pLevel, pPos, pState);

        if (hasProgressFinished()) {
            craftItem();
            resetProgress();
            isProcessing = false;
            pLevel.sendBlockUpdated(pPos, pState, pState, 3);
        }
    }

    private void resetProgress() {
        progress = 0;
    }

    private void craftItem() {
        WeaponForgeRecipe recipe = getSelectedRecipe().orElseThrow();
        ItemStack result = recipe.getResultItem(null);

        this.itemHandler.extractItem(INPUT_SLOT, 1, false);

        this.itemHandler.setStackInSlot(OUTPUT_SLOT, new ItemStack(result.getItem(),
                this.itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + result.getCount()));
    }

    private boolean hasProgressFinished() {
        return progress >= maxProgress;
    }

    private void increaseCraftingProgress() {
        progress ++;
    }

    private boolean hasSelectedRecipe() {
        Optional<WeaponForgeRecipe> recipe = getSelectedRecipe();
        if(recipe.isEmpty()){
            return false;
        }
        ItemStack result = recipe.get().getResultItem(getLevel().registryAccess());
        return  canInsertAmountIntoOutputSlot(result.getCount()) && canInsertItemIntoOutputSlot(result.getItem());
    }

    private Optional<WeaponForgeRecipe> getSelectedRecipe() {
        List<WeaponForgeRecipe> recipes = getRecipesForInput();
        if (selectedRecipeIndex < 0 || selectedRecipeIndex >= recipes.size()) {
            return Optional.empty();
        }
        return Optional.of(recipes.get(selectedRecipeIndex));
    }

    private boolean canInsertItemIntoOutputSlot(Item item) {
        return this.itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() || this.itemHandler.getStackInSlot(OUTPUT_SLOT).is(item);
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        return this.itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + count <= this.itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
    }

}
