package org.lins.mmmjjkx.rykenslimefuncustomizer.api.machine;

import java.util.Map;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public class CustomLinkedMachineRecipe extends CustomMachineRecipe {
    private final Map<Integer, ItemStack> linkedInput;
    private final LinkedOutput linkedOutput;

    public CustomLinkedMachineRecipe(
            int seconds,
            Map<Integer, ItemStack> input,
            LinkedOutput linkedOutput,
            boolean chooseOneIfHas,
            boolean forDisplay,
            boolean hide) {
        super(
                seconds,
                input.values().toArray(new ItemStack[0]),
                linkedOutput.toArray(),
                linkedOutput.chancesToArray(),
                chooseOneIfHas,
                forDisplay,
                hide);
        this.linkedInput = input;
        this.linkedOutput = linkedOutput;
    }
}
