package org.dimdev.jeid;

import org.spongepowered.asm.mixin.Mixins;
import zone.rong.mixinbooter.MixinLoader;

@MixinLoader
public class JEIDMixinLoader {

    {
        Mixins.addConfiguration("mixins.jeid.modsupport.json");
        Mixins.addConfiguration("mixins.jeid.twilightforest.json");
    }

}
