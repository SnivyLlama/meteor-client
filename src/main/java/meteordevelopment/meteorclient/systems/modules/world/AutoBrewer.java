/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.world;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.PotionSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.MyPotion;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.screen.BrewingStandScreenHandler;

public class AutoBrewer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<MyPotion> potion = sgGeneral.add(new PotionSetting.Builder()
            .name("potion")
            .description("The type of potion to brew.")
            .defaultValue(MyPotion.Strength)
            .build()
    );

    private final Setting<Boolean> splash = sgGeneral.add(new BoolSetting.Builder()
            .name("splash")
            .description("Brew splash potions instead of normal potions.")
            .defaultValue(false)
            .build()
    );

    private int ingredientI;
    private boolean first;
    private int timer;

    public AutoBrewer() {
        super(Categories.World, "auto-brewer", "Automatically brews the specified potion.");
    }

    @Override
    public void onActivate() {
        first = false;
    }

    public void onBrewingStandClose() {
        first = false;
    }

    public void tick(BrewingStandScreenHandler c) {
        timer++;

        // When the brewing stand is opened.
        if (!first) {
            first = true;

            ingredientI = -2;
            timer = 0;
        }

        // Wait for the brewing to complete.
        if (c.getBrewTime() != 0 || timer < 5) return;

        if (ingredientI == -2) {
            // Take the bottles.
            if (takePotions(c)) return;
            ingredientI++;
            timer = 0;
        } else if (ingredientI == -1) {
            // Insert water bottles into the brewing stand.
            if (insertWaterBottles(c)) return;
            ingredientI++;
            timer = 0;
        } else if (ingredientI < potion.get().ingredients.length) {
            // Check for fuel for the brew and add the ingredient.
            if (checkFuel(c)) return;
            if (insertIngredient(c, potion.get().ingredients[ingredientI])) return;
            ingredientI++;
            timer = 0;
        } else if (ingredientI == potion.get().ingredients.length && splash.get()) {
            // Check for fuel... again, and add gunpowder
            if (checkFuel(c)) return;
            if (insertIngredient(c, Items.GUNPOWDER)) return;
            ingredientI++;
            timer = 0;
        } else {
            // Reset the loop.
            ingredientI = -2;
            timer = 0;
        }
    }

    private boolean insertIngredient(BrewingStandScreenHandler c, Item ingredient) {
        int slot = -1;

        for (int slotI = 5; slotI < c.slots.size(); slotI++) {
            if (c.slots.get(slotI).getStack().getItem() == ingredient) {
                slot = slotI;
                break;
            }
        }

        if (slot == -1) {
            error("You do not have any %s left in your inventory... disabling.", ingredient.getName().getString());
            toggle();
            return true;
        }

        moveOneItem(c, slot, 3);

        return false;
    }

    private boolean checkFuel(BrewingStandScreenHandler c) {
        if (c.getFuel() == 0) {
            int slot = -1;

            for (int slotI = 5; slotI < c.slots.size(); slotI++) {
                if (c.slots.get(slotI).getStack().getItem() == Items.BLAZE_POWDER) {
                    slot = slotI;
                    break;
                }
            }

            if (slot == -1) {
                error("You do not have a sufficient amount of blaze powder to use as fuel for the brew... disabling.");
                toggle();
                return true;
            }

            moveOneItem(c, slot, 4);
        }

        return false;
    }

    private void moveOneItem(BrewingStandScreenHandler c, int from, int to) {
        InvUtils.move().fromId(from).toId(to);
    }

    private boolean insertWaterBottles(BrewingStandScreenHandler c) {
        for (int i = 0; i < 3; i++) {
            int slot = -1;

            for (int slotI = 5; slotI < c.slots.size(); slotI++) {
                if (c.slots.get(slotI).getStack().getItem() == Items.POTION) {
                    Potion potion = PotionUtil.getPotion(c.slots.get(slotI).getStack());
                    if (potion == Potions.WATER) {
                        slot = slotI;
                        break;
                    }
                }
            }

            if (slot == -1) {
                error("You do not have a sufficient amount of water bottles to complete this brew... disabling.");
                toggle();
                return true;
            }

            InvUtils.move().fromId(slot).toId(i);
        }

        return false;
    }

    private boolean takePotions(BrewingStandScreenHandler c) {
        for (int i = 0; i < 3; i++) {
            InvUtils.quickMove().slotId(i);

            if (!c.slots.get(i).getStack().isEmpty()) {
                error("You do not have a sufficient amount of inventory space... disabling.");
                toggle();
                return true;
            }
        }

        return false;
    }
}
