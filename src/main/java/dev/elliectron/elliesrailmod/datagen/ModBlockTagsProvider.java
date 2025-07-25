package dev.elliectron.elliesrailmod.datagen;

import dev.elliectron.elliesrailmod.ElliesRailImprovements;
import dev.elliectron.elliesrailmod.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, ElliesRailImprovements.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Add our rail to the minecraft:rails tag
        this.tag(BlockTags.RAILS)
                .add(ModBlocks.RAIL_CLASS_1.get())
                .add(ModBlocks.POWERED_RAIL_CLASS_1.get())
                .add(ModBlocks.RAIL_CLASS_2.get())
                .add(ModBlocks.POWERED_RAIL_CLASS_2.get())
                .add(ModBlocks.RAIL_CLASS_3.get())
                .add(ModBlocks.POWERED_RAIL_CLASS_3.get())
                .add(ModBlocks.RAIL_CLASS_4.get())
                .add(ModBlocks.POWERED_RAIL_CLASS_4.get())
                .add(ModBlocks.RAIL_CLASS_5.get())
                .add(ModBlocks.POWERED_RAIL_CLASS_5.get())
                .add(ModBlocks.POWERED_RAIL_CLASS_5A.get());

        // Add our rail to the pickaxe mineable tag
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.RAIL_CLASS_1.get())
                .add(ModBlocks.POWERED_RAIL_CLASS_1.get())
                .add(ModBlocks.RAIL_CLASS_2.get())
                .add(ModBlocks.POWERED_RAIL_CLASS_2.get())
                .add(ModBlocks.RAIL_CLASS_3.get())
                .add(ModBlocks.POWERED_RAIL_CLASS_3.get())
                .add(ModBlocks.RAIL_CLASS_4.get())
                .add(ModBlocks.POWERED_RAIL_CLASS_4.get())
                .add(ModBlocks.RAIL_CLASS_5.get())
                .add(ModBlocks.POWERED_RAIL_CLASS_5.get())
                .add(ModBlocks.POWERED_RAIL_CLASS_5A.get());

        // Add our rail to prevent mob spawning inside
        this.tag(BlockTags.PREVENT_MOB_SPAWNING_INSIDE)
                .add(ModBlocks.RAIL_CLASS_1.get())
                .add(ModBlocks.POWERED_RAIL_CLASS_1.get())
                .add(ModBlocks.RAIL_CLASS_2.get())
                .add(ModBlocks.POWERED_RAIL_CLASS_2.get())
                .add(ModBlocks.RAIL_CLASS_3.get())
                .add(ModBlocks.POWERED_RAIL_CLASS_3.get())
                .add(ModBlocks.RAIL_CLASS_4.get())
                .add(ModBlocks.POWERED_RAIL_CLASS_4.get())
                .add(ModBlocks.RAIL_CLASS_5.get())
                .add(ModBlocks.POWERED_RAIL_CLASS_5.get())
                .add(ModBlocks.POWERED_RAIL_CLASS_5A.get());
    }
}