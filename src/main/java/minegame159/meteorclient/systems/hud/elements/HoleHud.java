package minegame159.meteorclient.systems.hud.elements;

import minegame159.meteorclient.rendering.DrawMode;
import minegame159.meteorclient.rendering.Renderer;
import minegame159.meteorclient.settings.BlockListSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.systems.hud.ElementRegister;
import minegame159.meteorclient.systems.hud.HudRenderer;
import minegame159.meteorclient.systems.hud.ScaleableHudElement;
import minegame159.meteorclient.utils.Utils;
import minegame159.meteorclient.utils.render.RenderUtils;
import minegame159.meteorclient.utils.render.color.Color;
import minegame159.meteorclient.utils.world.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.List;

@ElementRegister(name = "hole")
public class HoleHud extends ScaleableHudElement {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<List<Block>> safe = sgGeneral.add(new BlockListSetting.Builder()
            .name("safe-blocks")
            .description("Which blocks to consider safe.")
            .defaultValue(Arrays.asList(Blocks.OBSIDIAN, Blocks.BEDROCK, Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK))
            .build()
    );

    private final Color BG_COLOR = new Color(255, 25, 25, 100);
    private final Color OL_COLOR = new Color(255, 25, 25, 255);

    public HoleHud() {
        super("hole", "Displays information about the hole you are standing in.");
    }

    @Override
    public void update(HudRenderer renderer) {
        box.setSize(16 * 3 * getScale(), 16 * 3 * getScale());
    }

    @Override
    public void render(HudRenderer renderer) {
        double x = box.getX();
        double y = box.getY();

        drawBlock(get(Facing.Left), x, y + 16 * getScale()); // Left
        drawBlock(get(Facing.Front), x + 16 * getScale(), y); // Front
        drawBlock(get(Facing.Right), x + 32 * getScale(), y + 16 * getScale()); // Right
        drawBlock(get(Facing.Back), x + 16 * getScale(), y + 32 * getScale()); // Back
    }

    private Direction get(Facing dir) {
        if (!Utils.canUpdate() || isInEditor()) return Direction.DOWN;
        return Direction.fromRotation(MathHelper.wrapDegrees(mc.player.yaw + dir.offset));
    }

    private void drawBlock(Direction dir, double x, double y) {
        Block block = dir == Direction.DOWN ? Blocks.OBSIDIAN : mc.world.getBlockState(mc.player.getBlockPos().offset(dir)).getBlock();
        if (!safe.get().contains(block)) block = Blocks.AIR;

        RenderUtils.drawItem(block.asItem().getDefaultStack(), (int) x, (int) y, getScale(),false);

        if (dir == Direction.DOWN) return;

        BlockUtils.breakingBlocks.values().forEach(info -> {
            if (info.getPos().equals(mc.player.getBlockPos().offset(dir)) && safe.get().contains(mc.world.getBlockState(info.getPos()).getBlock())) {
                renderBreaking(x, y, info.getStage() / 9f);
            }
        });
    }

    private void renderBreaking(double x, double y, double percent) {
        Renderer.NORMAL.begin(null, DrawMode.Triangles, VertexFormats.POSITION_COLOR);
        Renderer.NORMAL.quad(x, y, (16 * percent) * getScale(), 16 * getScale(), BG_COLOR);
        Renderer.NORMAL.quad(x, y, 16 * getScale(), 1 * getScale(), OL_COLOR);
        Renderer.NORMAL.quad(x, y + 15 * getScale(), 16 * getScale(), 1 * getScale(), OL_COLOR);
        Renderer.NORMAL.quad(x, y, 1 * getScale(), 16 * getScale(),OL_COLOR);
        Renderer.NORMAL.quad(x + 15 * getScale(), y, 1 * getScale(), 16 * getScale(), OL_COLOR);
        Renderer.NORMAL.end();
    }

    private enum Facing {
        Left(-90),
        Right(90),
        Front(0),
        Back(180);

        int offset;

        Facing(int offset) {
            this.offset = offset;
        }
    }
}
