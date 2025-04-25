package me.m4nst3in.m4plugins.util;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.m4nst3in.m4plugins.M4Pets;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HologramUtil {
    
    private final M4Pets plugin;
    private final ConcurrentHashMap<UUID, String> petHolograms;
    
    public HologramUtil(M4Pets plugin) {
        this.plugin = plugin;
        this.petHolograms = new ConcurrentHashMap<>();
    }
    
    /**
     * Cria ou atualiza um holograma para um pet
     * @param petId ID do pet
     * @param location Localização para o holograma
     * @param lines Linhas do holograma
     * @return ID do holograma criado
     */
    public String createOrUpdateHologram(UUID petId, Location location, List<String> lines) {
        if (!plugin.isDecentHologramsEnabled()) return null;
        
        // Verificar se já existe um holograma para este pet
        String hologramId = petHolograms.get(petId);
        
        if (hologramId != null) {
            // Atualizar holograma existente
            Hologram hologram = DHAPI.getHologram(hologramId);
            if (hologram != null) {
                DHAPI.moveHologram(hologramId, location);
                DHAPI.setHologramLines(hologram, lines);
                return hologramId;
            } else {
                // Se o holograma não existe mais, remover referência
                petHolograms.remove(petId);
            }
        }
        
        // Criar novo holograma
        hologramId = "pet_" + petId.toString();
        DHAPI.createHologram(hologramId, location, lines);
        petHolograms.put(petId, hologramId);
        
        return hologramId;
    }
    
    /**
     * Remove um holograma de pet
     * @param petId ID do pet
     */
    public void removeHologram(UUID petId) {
        if (!plugin.isDecentHologramsEnabled()) return;
        
        String hologramId = petHolograms.get(petId);
        if (hologramId != null) {
            DHAPI.removeHologram(hologramId);
            petHolograms.remove(petId);
        }
    }
    
    /**
     * Define a visibilidade de um holograma de pet
     * Ao invés de alterar visibilidade, remove e recria o holograma quando necessário
     * @param petId ID do pet
     * @param visible Se o holograma deve estar visível
     * @param location Localização atual (necessário para recriar)
     * @param lines Linhas do holograma (necessário para recriar)
     */
    public void setHologramVisibility(UUID petId, boolean visible, Location location, List<String> lines) {
        if (!plugin.isDecentHologramsEnabled()) return;
        
        String hologramId = petHolograms.get(petId);
        if (hologramId != null) {
            if (visible) {
                // Verificar se o holograma existe
                Hologram hologram = DHAPI.getHologram(hologramId);
                if (hologram == null) {
                    // Se não existir, criar novamente
                    DHAPI.createHologram(hologramId, location, lines);
                }
            } else {
                // Se não deve ser visível, remover
                DHAPI.removeHologram(hologramId);
                // Mas manter no mapa para sabermos que já existiu
            }
        } else if (visible) {
            // Se não há referência e deve ser visível, criar novo
            hologramId = "pet_" + petId.toString();
            DHAPI.createHologram(hologramId, location, lines);
            petHolograms.put(petId, hologramId);
        }
    }
    
    /**
     * Remove todos os hologramas
     */
    public void removeAllHolograms() {
        if (!plugin.isDecentHologramsEnabled()) return;
        
        for (String hologramId : petHolograms.values()) {
            DHAPI.removeHologram(hologramId);
        }
        petHolograms.clear();
    }
}