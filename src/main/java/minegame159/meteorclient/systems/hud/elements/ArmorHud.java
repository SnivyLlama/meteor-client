/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.systems.hud.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.systems.hud.ElementRegister;
import minegame159.meteorclient.systems.hud.HudRenderer;
import minegame159.meteorclient.systems.hud.ScaleableHudElement;
import minegame159.meteorclient.utils.render.RenderUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@ElementRegister(name = "armor")
public class ArmorHud extends ScaleableHudElement {
    public enum Durability {
        None,
        Default,
        Numbers,
        Percentage
    }

    public enum Orientation {
        Horizontal,
        Vertical
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Orientation> orientation = sgGeneral.add(new EnumSetting.Builder<Orientation>()
            .name("orientation")
            .description("How to display armor.")
            .defaultValue(Orientation.Horizontal)
            .build()
    );

    private final Setting<Durability> durability = sgGeneral.add(new EnumSetting.Builder<Durability>()
            .name("durability")
            .description("How to display armor durability.")
            .defaultValue(Durability.Default)
            .build()
    );

    public ArmorHud() {
        super("armor", "Displays information about your armor.");
    }

    @Override
    public void update(HudRenderer renderer) {
        switch (orientation.get()) {
            case Horizontal:
                box.setSize(16 * getScale() * 4 + 2 * 4, 16 * getScale());
                break;
            case Vertical:
                box.setSize(16 * getScale(), 16 * getScale() * 4 + 2 * 4);
        }
    }

    @Override
    public void render(HudRenderer renderer) {
        double x = box.getX();
        double y = box.getY();
        double armorX;
        double armorY;

        int slot = 3;
        for (int position = 0; position < 4; position++) {
            ItemStack itemStack = getItem(slot);

            RenderSystem.pushMatrix();
            RenderSystem.scaled(getScale(), getScale(), 1);

            if (orientation.get() == Orientation.Vertical) {
                armorX = x / getScale();
                armorY = y / getScale() + position * 18;
            } else {
                armorX = x / getScale() + position * 18;
                armorY = y / getScale();
            }

            RenderUtils.drawItem(itemStack, (int) armorX, (int) armorY, (itemStack.isDamageable() && durability.get() == Durability.Default));

            if (itemStack.isDamageable() && !isInEditor() && durability.get() != Durability.Default && durability.get() != Durability.None) {
                String message = "err";

                switch (durability.get()) {
                    case Numbers:
                        message = Integer.toString(itemStack.getMaxDamage() - itemStack.getDamage());
                        break;
                    case Percentage:
                        message = Integer.toString(Math.round(((itemStack.getMaxDamage() - itemStack.getDamage()) * 100f) / (float) itemStack.getMaxDamage()));
                        break;
                }

                double messageWidth = renderer.textWidth(message);

                if (orientation.get() == Orientation.Vertical) {
                    armorX = x + 8 * getScale() - messageWidth / 2.0;
                    armorY = y + (18 * position * getScale()) + (18 * getScale() - renderer.textHeight());
                } else {
                    armorX = x + 18 * position * getScale() + 8 * getScale() - messageWidth / 2.0;
                    armorY = y + (box.height - renderer.textHeight());
                }

                renderer.text(message, armorX, armorY, hud.primaryColor.get());
            }

            RenderSystem.popMatrix();

            slot--;
        }
    }

    private ItemStack getItem(int i) {
        if (isInEditor()) {
            switch (i) {
                default: return Items.NETHERITE_BOOTS.getDefaultStack();
                case 1:  return Items.NETHERITE_LEGGINGS.getDefaultStack();
                case 2:  return Items.NETHERITE_CHESTPLATE.getDefaultStack();
                case 3:  return Items.NETHERITE_HELMET.getDefaultStack();
            }
        }

        return mc.player.inventory.getArmorStack(i);
    }
}
