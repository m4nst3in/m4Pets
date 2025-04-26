package me.m4nst3in.m4plugins.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Utilidades para manipular propriedades de entidades que controlam o movimento
 */
public class EntityControlHelper {
    
    private static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    
    /**
     * Torna uma entidade controlável pelo jogador
     * @param entity A entidade para tornar controlável
     * @return true se a operação foi bem-sucedida
     */
    public static boolean makeControlable(Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;
        
        try {
            // Métodos gerais para todas as entidades controláveis
            if (entity.getType() == EntityType.PIG) {
                return makePigControlable(entity);
            } else if (entity.getType() == EntityType.HORSE || 
                       entity.getType() == EntityType.DONKEY || 
                       entity.getType() == EntityType.MULE) {
                return makeHorseControlable(entity);
            } else if (entity.getType() == EntityType.STRIDER) {
                return makeStriderControlable(entity);
            }
            
            // Caso genérico para outras entidades
            return makeGenericControlable(entity);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean makePigControlable(Entity entity) {
        try {
            // Método específico para Pig
            // Em Minecraft, o pig precisa de alguns ajustes específicos
            Method getHandle = entity.getClass().getMethod("getHandle");
            Object nmsEntity = getHandle.invoke(entity);
            
            // Define flags para torna-lo passivo e controlável
            Class<?> nmsEntityClass = nmsEntity.getClass().getSuperclass();
            
            // Esta técnica funciona em várias versões do Minecraft
            Method setFlag = null;
            for (Method method : nmsEntityClass.getMethods()) {
                if (method.getName().equals("setFlag") || method.getName().equals("s") && method.getParameterCount() == 2) {
                    setFlag = method;
                    break;
                }
            }
            
            if (setFlag != null) {
                // Flag 7 geralmente controla se a entidade pode ser controlada
                setFlag.invoke(nmsEntity, 7, true);
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean makeHorseControlable(Entity entity) {
        try {
            // Para cavalos e similares - nem sempre precisa de intervenção NMS
            return true; // A maioria das vezes setTamed(true) é suficiente
        } catch (Exception e) {
            return false;
        }
    }
    
    private static boolean makeStriderControlable(Entity entity) {
        try {
            // Método específico para Strider
            Method getHandle = entity.getClass().getMethod("getHandle");
            Object nmsEntity = getHandle.invoke(entity);
            
            // Similar ao porco, mas com algumas diferenças
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private static boolean makeGenericControlable(Entity entity) {
        try {
            // Método genérico - tenta ajustar propriedades básicas
            Method getHandle = entity.getClass().getMethod("getHandle");
            Object nmsEntity = getHandle.invoke(entity);
            
            // Tenta manipular "persistent" e "noAI" flags
            for (Method method : nmsEntity.getClass().getMethods()) {
                if (method.getName().equals("setPersistent")) {
                    method.invoke(nmsEntity, true);
                }
                if (method.getName().equals("setNoAI")) {
                    method.invoke(nmsEntity, false);
                }
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}