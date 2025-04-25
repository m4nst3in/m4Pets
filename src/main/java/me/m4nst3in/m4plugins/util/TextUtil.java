package me.m4nst3in.m4plugins.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final boolean SUPPORTS_RGB = checkRGBSupport();
    
    // Mapa para SmallCaps
    private static final Map<Character, Character> SMALL_CAPS = new HashMap<>();
    
    static {
        // Inicializar mapa de small caps
        String normal = "abcdefghijklmnopqrstuvwxyz0123456789";
        String smallCaps = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴩqʀꜱᴛᴜᴠᴡxʏᴢ₀₁₂₃₄₅₆₇₈₉";
        
        for (int i = 0; i < normal.length(); i++) {
            SMALL_CAPS.put(normal.charAt(i), smallCaps.charAt(i));
        }
    }
    
    /**
     * Verifica se o servidor suporta cores RGB
     */
    private static boolean checkRGBSupport() {
        try {
            // Verificar se o método para RGB existe (1.16+)
            Class<?> chatColorClass = ChatColor.class;
            chatColorClass.getMethod("of", String.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
    
    /**
     * Converte códigos de cor para cores no texto
     */
    public static String color(String text) {
        if (text == null) return "";
        
        // Processar hexadecimal se suportado
        if (SUPPORTS_RGB) {
            Matcher matcher = HEX_PATTERN.matcher(text);
            StringBuffer buffer = new StringBuffer();
            
            while (matcher.find()) {
                String hex = matcher.group(1);
                matcher.appendReplacement(buffer, ChatColor.of("#" + hex).toString());
            }
            
            matcher.appendTail(buffer);
            text = buffer.toString();
        }
        
        // Processar códigos de cor normais
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    
    /**
     * Converte texto normal para small caps
     */
    public static String toSmallCaps(String text) {
        if (text == null) return "";
        
        StringBuilder result = new StringBuilder();
        
        for (char c : text.toCharArray()) {
            if (SMALL_CAPS.containsKey(Character.toLowerCase(c))) {
                result.append(Character.isUpperCase(c) ? c : SMALL_CAPS.get(c));
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    /**
     * Formata um texto com small caps e cores
     */
    public static String formatWithSmallCaps(String text) {
        return color(toSmallCaps(text));
    }
}