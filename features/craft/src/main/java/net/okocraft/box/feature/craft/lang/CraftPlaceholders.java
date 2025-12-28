package net.okocraft.box.feature.craft.lang;

import dev.siroshun.mcmsgdef.Placeholder;
import net.kyori.adventure.text.minimessage.translation.Argument;

public class CraftPlaceholders {

    public static final Placeholder<Integer> TIMES = times -> Argument.numeric("times", times);

}
