package eu.midnightdust.visualoverhaul.block.renderer;

import eu.midnightdust.visualoverhaul.VisualOverhaulClient;
import eu.midnightdust.visualoverhaul.config.VOConfig;
import eu.midnightdust.visualoverhaul.util.SoundTest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.SideShapeType;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.apache.logging.log4j.LogManager;
import org.joml.AxisAngle4f;
import org.joml.Math;
import org.joml.Quaternionf;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class JukeboxBlockEntityRenderer implements BlockEntityRenderer<JukeboxBlockEntity> {
    private ItemStack record;
    private Identifier discItem;

    public JukeboxBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(JukeboxBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (VOConfig.jukebox) {
            int lightAbove = WorldRenderer.getLightmapCoordinates(Objects.requireNonNull(blockEntity.getWorld()), blockEntity.getPos().up());

            // Tries to get the disc using the serverside method
            if (blockEntity.getStack() != ItemStack.EMPTY) {
                record = blockEntity.getStack().copy();
            }
            // Else gets the record sound played at the position of the jukebox //
            else if (SoundTest.getSound(blockEntity.getPos()) != null) {
                // Converts the Sound ID to the item ID of the appropriate disc (minecraft:music_disc.cat -> minecraft:music_disc_cat) //
                discItem = new Identifier(String.valueOf(SoundTest.getSound(blockEntity.getPos())).replace(".", "_"));

                // Tries to get the disc item from the registry //
                if (Registries.ITEM.getOrEmpty(discItem).isPresent()) {
                    record = new ItemStack(Registries.ITEM.get(discItem));
                }
                else {
                    if (VOConfig.debug) LogManager.getLogger("VisualOverhaul").warn("Error getting music disc item for " + SoundTest.getSound(blockEntity.getPos()));
                    discItem = null;
                    record = ItemStack.EMPTY;
                }
            }
            // If the sound is stopped or no sound is playing, the stack is set to an empty stack //
            else {
                discItem = null;
                record = ItemStack.EMPTY;
            }

            if (!record.isEmpty()) {
                record.setCount(2);
                matrices.push();

                matrices.translate(0.5f, 1.03f, 0.5f);
                matrices.scale(0.75f, 0.75f, 0.75f);
                matrices.multiply(new Quaternionf(new AxisAngle4f(Math.toRadians(Util.getMeasuringTimeMs() / 9.0f), 0, 1, 0)));
                MinecraftClient.getInstance().getItemRenderer().renderItem(record, ModelTransformationMode.GROUND, lightAbove, overlay, matrices, vertexConsumers, blockEntity.getWorld(), 0);

                matrices.pop();
            }
            if (VOConfig.jukebox_fake_block && !blockEntity.getWorld().getBlockState(blockEntity.getPos().up()).isSideSolid(blockEntity.getWorld(),blockEntity.getPos().up(), Direction.DOWN, SideShapeType.FULL)) {
                matrices.push();
                matrices.translate(0f, 1f, 0f);
                if (record == ItemStack.EMPTY) {
                    MinecraftClient.getInstance().getBlockRenderManager().renderBlock(VisualOverhaulClient.JukeBoxTop.getDefaultState().with(Properties.HAS_RECORD, false), blockEntity.getPos().up(), blockEntity.getWorld(), matrices, vertexConsumers.getBuffer(RenderLayer.getCutout()), false, Random.create());
                } else {
                    MinecraftClient.getInstance().getBlockRenderManager().renderBlock(VisualOverhaulClient.JukeBoxTop.getDefaultState().with(Properties.HAS_RECORD, true), blockEntity.getPos().up(), blockEntity.getWorld(), matrices, vertexConsumers.getBuffer(RenderLayer.getCutout()), false, Random.create());
                }
                matrices.pop();
            }
        }
    }
}