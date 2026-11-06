package com.minelittlepony.unicopia.compat.trinkets;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import com.minelittlepony.unicopia.Unicopia;
import com.mojang.blaze3d.platform.NativeImage;

public class TrinketSlotBackSprites {
    private static final Identifier BLANK_FACE = Identifier.fromNamespaceAndPath("trinkets", "textures/gui/blank_back.png");

    private static final Map<Identifier, Identifier> CACHE = new HashMap<>();

    public static Identifier getBackSprite(Identifier originalSprite) {
        return CACHE.computeIfAbsent(originalSprite, TrinketSlotBackSprites::generateTransparentSprite);
    }

    private static Identifier generateTransparentSprite(Identifier originalId) {
        return readImage(BLANK_FACE, blank -> {
           return readImage(originalId, original -> {
               NativeImage generatedImage = new NativeImage(original.getWidth(), original.getHeight(), false);
               final float widthScale = original.getWidth() / (float)blank.getWidth();
               final float heightScale = original.getHeight() / (float)blank.getHeight();

               for (int x = 0; x < original.getWidth(); x++) {
                   for (int y = 0; y < original.getHeight(); y++) {
                       int blankColor = blank.getPixel((int)(x * widthScale), (int)(y * heightScale));
                       int originalColor = original.getPixel(x, y);

                       generatedImage.setPixel(x, y, blankColor == originalColor ? 0 : originalColor);
                   }
               }

               Identifier id = originalId.withPrefix("trinket_slot" + originalId);
               Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(() -> "Unicopia.TrinketSlotBackSprites/" + id.toString(), generatedImage));
               return id;
           }).orElse(originalId);
        }).orElse(originalId);
    }

    private static <T> Optional<T> readImage(Identifier id, Function<NativeImage, T> consumer) {
        return Minecraft.getInstance().getResourceManager().getResource(id).map(input -> {
            try (NativeImage image = NativeImage.read(input.open())) {
                return consumer.apply(image);
            } catch (Exception e) {
                Unicopia.LOGGER.error("Error whilst reading slot background resource " + id, e);
            }
            return null;
        });
    }
}
