package cat.daisy.text

import net.kyori.adventure.text.Component

public fun interface DaisyTextRenderer<in C> {
    public fun render(context: C): Component
}
