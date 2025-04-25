package me.m4nst3in.m4plugins.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

public class HeadUtil {
    
    /**
     * Cria um item de cabeça customizada com a textura especificada
     * @param textureValue Valor da textura (Base64)
     * @return ItemStack com a cabeça customizada
     */
    public static ItemStack getCustomHead(String textureValue) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (textureValue.isEmpty()) return head;
        
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return head;
        
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        profile.getProperties().put("textures", new Property("textures", textureValue));
        
        try {
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return head;
        }
        
        head.setItemMeta(meta);
        return head;
    }
}