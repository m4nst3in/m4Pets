package me.m4nst3in.m4plugins.gui;

import me.m4nst3in.m4plugins.M4Pets;
import org.bukkit.configuration.ConfigurationSection;
import me.m4nst3in.m4plugins.pets.abstractpets.AbstractPet;
import me.m4nst3in.m4plugins.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MyPetsGUI {
    
    private final M4Pets plugin;
    private final Map<UUID, Map<UUID, Inventory>> petManagementInventories; // playerUUID -> petUUID -> Inventory
    
    public MyPetsGUI(M4Pets plugin) {
        this.plugin = plugin;
        this.petManagementInventories = new HashMap<>();
    }
    
    /**
     * Abre a GUI de lista de pets do jogador
     */
    public void openPetsList(Player player) {
        Collection<AbstractPet> pets = plugin.getPetManager().getPlayerPets(player.getUniqueId());
        
        // Normalize title to match GUIManager expectations (single &a&l)
        String title = TextUtil.color("&5&l✨ &9&lM4Pets &8&l| &a&l🐾 Meus Pets 🐾");
        Inventory inventory = Bukkit.createInventory(null, 54, title);
        
        if (pets.isEmpty()) {
            // Mostrar mensagem de que o jogador não tem pets
            ItemStack noPetsItem = new ItemStack(Material.BARRIER);
            ItemMeta noPetsMeta = noPetsItem.getItemMeta();
            if (noPetsMeta != null) {
                noPetsMeta.setDisplayName(TextUtil.color("&c&l❌ &fVocê não possui pets! &c&l❌"));
                List<String> lore = new ArrayList<>();
                lore.add(TextUtil.color("&7┌─────────────────────────┐"));
                lore.add(TextUtil.color("&7│ &fVisite a loja de pets  &7│"));
                lore.add(TextUtil.color("&7│ &fpara adquirir um pet!  &7│"));
                lore.add(TextUtil.color("&7└─────────────────────────┘"));
                lore.add("");
                lore.add(TextUtil.color("&e&l🛒 &6Clique para ir à loja"));
                noPetsMeta.setLore(lore);
                noPetsItem.setItemMeta(noPetsMeta);
            }
            inventory.setItem(22, noPetsItem);
        } else {
            // Adicionar cada pet à interface
            int slot = 10;
            
            for (AbstractPet pet : pets) {
                Material icon = Material.valueOf(
                    plugin.getConfigManager().getMainConfig().getString(
                        "pets." + pet.getType().getConfigCategory() + "." + pet.getType().name().toLowerCase() + ".icon", 
                        "NAME_TAG"
                    )
                );
                
                ItemStack item = new ItemStack(icon);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(TextUtil.color("&a&l🐾 " + pet.getPetName()));
                    
                    List<String> lore = new ArrayList<>();
                    lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
                    lore.add(TextUtil.color("&7📝 &fTipo: &b" + plugin.getConfigManager().getMainConfig().getString(
                        "pets." + pet.getType().getConfigCategory() + "." + pet.getType().name().toLowerCase() + ".name", 
                        pet.getType().name()
                    )));
                    lore.add(TextUtil.color("&7⭐ &fNível: &e&l" + pet.getLevel() + "&7/&e5"));
                    lore.add(TextUtil.color("&7❤ &fVida: &c" + (int)pet.getHealth() + "&7/&c" + (int)pet.getMaxHealth()));
                    lore.add("");
                    
                    if (pet.isDead()) {
                        lore.add(TextUtil.color("&4&l💀 MORTO 💀"));
                        lore.add(TextUtil.color("&c&oSeu pet precisa ser ressuscitado"));
                    } else {
                        lore.add(TextUtil.color("&2&l💚 VIVO 💚"));
                        lore.add(TextUtil.color("&a&oSeu pet está saudável"));
                    }
                    
                    lore.add("");
                    lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
                    lore.add(TextUtil.color("&e&l👆 &6Clique para gerenciar!"));
                    
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                
                inventory.setItem(slot, item);
                
                // Avançar para o próximo slot
                slot++;
                if (slot % 9 == 8) {
                    slot += 2;
                }
                
                // Limitar a 28 pets por página
                if (slot >= 45) break;
            }
        }
        
        // Adicionar item de voltar
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(TextUtil.color("&c&l◀ &fVoltar ao Menu Principal"));
            List<String> lore = new ArrayList<>();
            lore.add(TextUtil.color("&7Retornar ao menu principal"));
            lore.add("");
            lore.add(TextUtil.color("&e&l👆 &6Clique para voltar"));
            backMeta.setLore(lore);
            backItem.setItemMeta(backMeta);
        }
        inventory.setItem(49, backItem);
        
        // Preencher espaços vazios com painéis de vidro
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
        
        player.openInventory(inventory);
    }
    
    /**
     * Abre a GUI de gerenciamento de um pet específico
     */
    public void openPetManagement(Player player, AbstractPet pet) {
        UUID playerUUID = player.getUniqueId();
        
        // Criar ou recuperar o mapa de inventários para este jogador
        petManagementInventories.putIfAbsent(playerUUID, new HashMap<>());
        Map<UUID, Inventory> playerInventories = petManagementInventories.get(playerUUID);
        
        // Criar um novo inventário para este pet
        String title = TextUtil.color("&5&l✨ &9&lM4Pets &8&l| &a&l🛠 " + pet.getPetName() + " &a&l🛠");
        Inventory inventory = Bukkit.createInventory(null, 36, title);
        
        // Summon/Despawn
        ItemStack summonItem = new ItemStack(pet.isSpawned() ? Material.RED_DYE : Material.LIME_DYE);
        ItemMeta summonMeta = summonItem.getItemMeta();
        if (summonMeta != null) {
            summonMeta.setDisplayName(TextUtil.color(pet.isSpawned() ? "&c&l❌ Remover Pet" : "&a&l✅ Invocar Pet"));
            
            List<String> lore = new ArrayList<>();
            lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            if (pet.isSpawned()) {
                lore.add(TextUtil.color("&7🌍 &fSeu pet está no mundo"));
                lore.add(TextUtil.color("&7📍 &fClique para removê-lo"));
                lore.add("");
                lore.add(TextUtil.color("&c⚠ &fO pet será despawnado"));
            } else {
                lore.add(TextUtil.color("&7👻 &fSeu pet não está no mundo"));
                lore.add(TextUtil.color("&7🎯 &fClique para invocá-lo"));
                lore.add("");
                lore.add(TextUtil.color("&a✨ &fO pet aparecerá ao seu lado"));
            }
            lore.add("");
            lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            lore.add(TextUtil.color("&e&l👆 &6Clique para " + (pet.isSpawned() ? "remover" : "invocar")));
            
            summonMeta.setLore(lore);
            summonItem.setItemMeta(summonMeta);
        }
        inventory.setItem(10, summonItem);
        
        // Renomear pet
        ItemStack renameItem = new ItemStack(Material.NAME_TAG);
        ItemMeta renameMeta = renameItem.getItemMeta();
        if (renameMeta != null) {
            renameMeta.setDisplayName(TextUtil.color("&e&l✏️ Renomear Pet"));
            
            List<String> lore = new ArrayList<>();
            lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            lore.add(TextUtil.color("&7📝 &fNome atual: &b" + pet.getPetName()));
            lore.add("");
            lore.add(TextUtil.color("&7✨ &fDê um novo nome especial"));
            lore.add(TextUtil.color("&7   &fpara seu companheiro!"));
            lore.add("");
            lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            lore.add(TextUtil.color("&e&l👆 &6Clique para renomear"));
            
            renameMeta.setLore(lore);
            renameItem.setItemMeta(renameMeta);
        }
        inventory.setItem(12, renameItem);
        
        // Atualizar Pet (level up)
        ItemStack upgradeItem = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta upgradeMeta = upgradeItem.getItemMeta();
        if (upgradeMeta != null) {
            int maxLevel = plugin.getConfigManager().getMainConfig().getInt("pets.global.max-level", 5);
            boolean maxLevelReached = pet.getLevel() >= maxLevel;
            
            upgradeMeta.setDisplayName(TextUtil.color(maxLevelReached 
                ? "&7&l🏆 Nível Máximo Atingido" 
                : "&b&l⬆️ Melhorar Pet"));
            
            List<String> lore = new ArrayList<>();
            lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            if (maxLevelReached) {
                lore.add(TextUtil.color("&7🏆 &fParabéns! Seu pet alcançou"));
                lore.add(TextUtil.color("&7   &fo nível máximo possível!"));
                lore.add("");
                lore.add(TextUtil.color("&e✨ &6Habilidade especial desbloqueada!"));
                lore.add(TextUtil.color("&a&l🎉 Pet completamente evoluído!"));
            } else {
                double petCost = plugin.getConfigManager().getMainConfig().getDouble(
                    "pets." + pet.getType().getConfigCategory() + "." + pet.getType().name().toLowerCase() + ".cost", 
                    1000
                );
                double upgradeCost = petCost * 0.25 * pet.getLevel();
                
                lore.add(TextUtil.color("&7⭐ &fNível atual: &e&l" + pet.getLevel() + "&7/&e" + maxLevel));
                lore.add(TextUtil.color("&7🔮 &fPróximo nível: &a&l" + (pet.getLevel() + 1)));
                lore.add("");
                lore.add(TextUtil.color("&7💰 &fCusto: &6$" + String.format("%.2f", upgradeCost)));
                lore.add("");
                lore.add(TextUtil.color("&7📈 &fBenefícios do upgrade:"));
                lore.add(TextUtil.color("&a  ❤ &fMais vida"));
                lore.add(TextUtil.color("&a  ⚡ &fMais velocidade"));
                lore.add(TextUtil.color("&a  💪 &fMais força"));
            }
            lore.add("");
            lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            if (!maxLevelReached) {
                lore.add(TextUtil.color("&e&l👆 &6Clique para melhorar!"));
            } else {
                lore.add(TextUtil.color("&7&l🎯 Pet no nível máximo"));
            }
            
            upgradeMeta.setLore(lore);
            upgradeItem.setItemMeta(upgradeMeta);
        }
        inventory.setItem(14, upgradeItem);
        
        // Ressuscitar Pet (se estiver morto)
        ItemStack resurrectItem = new ItemStack(pet.isDead() ? Material.TOTEM_OF_UNDYING : Material.LIGHT_GRAY_DYE);
        ItemMeta resurrectMeta = resurrectItem.getItemMeta();
        if (resurrectMeta != null) {
            resurrectMeta.setDisplayName(TextUtil.color(pet.isDead() 
                ? "&d&lRessuscitar Pet" 
                : "&8&lRessuscitar Pet"));
            
            List<String> lore = new ArrayList<>();
            if (pet.isDead()) {
                double petCost = plugin.getConfigManager().getMainConfig().getDouble(
                    "pets." + pet.getType().getConfigCategory() + "." + pet.getType().name().toLowerCase() + ".cost", 
                    1000
                );
                int resurrectPercent = plugin.getConfigManager().getMainConfig().getInt("economy.resurrect-cost-percent", 25);
                double resurrectCost = petCost * (resurrectPercent / 100.0);
                
                lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
                lore.add(TextUtil.color("&7💀 &cSeu pet está morto!"));
                lore.add(TextUtil.color("&7💰 &fCusto para ressuscitar:"));
                lore.add(TextUtil.color("&7   &e&l" + String.format("%.2f", resurrectCost) + " &7moedas"));
                lore.add("");
                lore.add(TextUtil.color("&7✨ &fReviva seu companheiro"));
                lore.add(TextUtil.color("&7   &fe volte a se aventurar!"));
                lore.add("");
                lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
                lore.add(TextUtil.color("&e&l👆 &6Clique para ressuscitar"));
            } else {
                lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
                lore.add(TextUtil.color("&7💚 &aSeu pet está vivo e"));
                lore.add(TextUtil.color("&7   &asaudável!"));
                lore.add("");
                lore.add(TextUtil.color("&7🎉 &fNão precisa de"));
                lore.add(TextUtil.color("&7   &fressurreição"));
                lore.add("");
                lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            }
            
            resurrectMeta.setLore(lore);
            resurrectItem.setItemMeta(resurrectMeta);
        }
        inventory.setItem(16, resurrectItem);
        
        // Cosméticos
        ItemStack cosmeticsItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta cosmeticsMeta = cosmeticsItem.getItemMeta();
        if (cosmeticsMeta != null) {
            cosmeticsMeta.setDisplayName(TextUtil.color("&d&l💎 &f&lCosméticos"));
            
            List<String> lore = new ArrayList<>();
            lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            lore.add(TextUtil.color("&7✨ &fTorne seu pet único com"));
            lore.add(TextUtil.color("&7   &fcosméticos exclusivos!"));
            lore.add("");
            lore.add(TextUtil.color("&7💎 &fAcessórios especiais"));
            lore.add(TextUtil.color("&7🌟 &fEfeitos visuais"));
            lore.add(TextUtil.color("&7🎭 &fPersonalizações únicas"));
            lore.add("");
            lore.add(TextUtil.color("&7💰 &fAdquira na loja do pet"));
            lore.add("");
            lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            lore.add(TextUtil.color("&e&l👆 &6Clique para ver itens"));
            
            cosmeticsMeta.setLore(lore);
            cosmeticsItem.setItemMeta(cosmeticsMeta);
        }
        inventory.setItem(30, cosmeticsItem);
        
        // Mudança de variante (cor, etc)
        ItemStack variantItem = new ItemStack(Material.LEATHER_HORSE_ARMOR);
        ItemMeta variantMeta = variantItem.getItemMeta();
        if (variantMeta != null) {
            variantMeta.setDisplayName(TextUtil.color("&6&l🎨 &f&lAparência"));
            
            List<String> lore = new ArrayList<>();
            lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            lore.add(TextUtil.color("&7🌈 &fPersonalize a aparência"));
            lore.add(TextUtil.color("&7   &fdo seu querido pet!"));
            lore.add("");
            lore.add(TextUtil.color("&7🎯 &fVariante atual: &b&l" + pet.getVariant()));
            lore.add("");
            lore.add(TextUtil.color("&7✨ &fEscolha entre diversas"));
            lore.add(TextUtil.color("&7   &fcores e estilos únicos"));
            lore.add("");
            lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            lore.add(TextUtil.color("&e&l👆 &6Clique para alterar"));
            
            variantMeta.setLore(lore);
            variantItem.setItemMeta(variantMeta);
        }
        inventory.setItem(32, variantItem);
        
        // Informações do pet
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(TextUtil.color("&b&lInformações do Pet"));
            
            List<String> lore = new ArrayList<>();
            lore.add(TextUtil.color("&7Nome: &f" + pet.getPetName()));
            lore.add(TextUtil.color("&7Tipo: &f" + plugin.getConfigManager().getMainConfig().getString(
                "pets." + pet.getType().getConfigCategory() + "." + pet.getType().name().toLowerCase() + ".name", 
                pet.getType().name()
            )));
            lore.add(TextUtil.color("&7Nível: &e" + pet.getLevel()));
            lore.add(TextUtil.color("&7Vida: &c" + (int)pet.getHealth() + "&7/&c" + (int)pet.getMaxHealth()));
            
            if (pet.getLevel() >= 5) {
                lore.add("");
                lore.add(TextUtil.color("&a&lHabilidade de Nível 5:"));
                lore.add(TextUtil.color("&f" + pet.getLevel5AbilityDescription()));
            }
            
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        inventory.setItem(22, infoItem);
        
        // Item de voltar
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(TextUtil.color("&c&lVoltar"));
            backItem.setItemMeta(backMeta);
        }
        inventory.setItem(27, backItem);
        
        // Preencher espaços vazios com painéis de vidro
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
        
        // Salvar o inventário e abrir para o jogador
        playerInventories.put(pet.getPetId(), inventory);
        player.openInventory(inventory);
    }
    
    /**
     * Processa uma ação na GUI de gerenciamento de pet
     */
    public void handlePetManagementAction(Player player, AbstractPet pet, int slot) {
        switch(slot) {
            case 10: // Summon/Despawn
                if (pet.isSpawned()) {
                    pet.despawn();
                    player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("general.pet-despawned")));
                } else {
                    if (plugin.getPetManager().spawnPet(player, pet)) {
                        // Mensagem já é enviada pelo método spawn do pet
                    }
                }
                // Atualizar a GUI
                openPetManagement(player, pet);
                break;
                
            case 12: // Rename
                player.closeInventory();
                player.sendMessage(plugin.formatMessage("&aDigite o novo nome para o seu pet no chat:"));
                
                // Registrar callback para próxima mensagem no chat
                plugin.getGuiManager().registerRenamePetCallback(player, pet);
                break;
                
            case 14: // Upgrade
                if (pet.upgrade(player)) {
                    // Mensagem já é enviada pelo método upgrade do pet
                    // Atualizar a GUI
                    openPetManagement(player, pet);
                }
                break;
                
            case 16: // Resurrect
                if (pet.isDead() && pet.resurrect(player)) {
                    // Mensagem já é enviada pelo método resurrect do pet
                    // Atualizar a GUI
                    openPetManagement(player, pet);
                }
                break;
                
            case 30: // Cosmetics
                openCosmeticsMenu(player, pet);
                break;
                
            case 32: // Variant
                openVariantMenu(player, pet);
                break;
                
            case 27: // Back
                openPetsList(player);
                break;
        }
    }
    
    /**
     * Abre o menu de cosméticos para o pet
     */
    public void openCosmeticsMenu(Player player, AbstractPet pet) {
        String title = TextUtil.color("&5&l✨ &9&lM4Pets &8&l| &d&l💎 Cosméticos &d&l💎");
        Inventory inventory = Bukkit.createInventory(null, 36, title);
        
        // Obter configuração de cosméticos
        ConfigurationSection cosmeticsConfig = plugin.getConfigManager().getMainConfig().getConfigurationSection("cosmetics.particles");
        if (cosmeticsConfig != null) {
            int slot = 10;
            
            for (String cosmeticKey : cosmeticsConfig.getKeys(false)) {
                String name = cosmeticsConfig.getString(cosmeticKey + ".name", "Cosmético");
                Material icon = Material.valueOf(cosmeticsConfig.getString(cosmeticKey + ".icon", "BARRIER"));
                int cost = cosmeticsConfig.getInt(cosmeticKey + ".cost", 500);
                String particleType = cosmeticsConfig.getString(cosmeticKey + ".particle-type", "HEART");
                
                ItemStack item = new ItemStack(icon);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(TextUtil.color("&d&l💎 " + name));
                    
                    List<String> lore = new ArrayList<>();
                    lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
                    
                    boolean hasCosmetic = particleType.equals(pet.getCosmeticParticle());
                    
                    if (hasCosmetic) {
                        lore.add(TextUtil.color("&a&l✅ &fCosmético Ativado"));
                        lore.add(TextUtil.color("&7✨ &fEste efeito está sendo"));
                        lore.add(TextUtil.color("&7   &faplicado ao seu pet!"));
                        lore.add("");
                        lore.add(TextUtil.color("&c&l❌ &6Clique para desativar"));
                    } else {
                        lore.add(TextUtil.color("&7💰 &fPreço: &e&l$" + cost));
                        lore.add(TextUtil.color("&7🎆 &fEfeito: &b" + particleType));
                        lore.add("");
                        lore.add(TextUtil.color("&7✨ &fAdicione este efeito"));
                        lore.add(TextUtil.color("&7   &fvisual ao seu pet!"));
                        lore.add("");
                        lore.add(TextUtil.color("&a&l💳 &6Clique para comprar"));
                    }
                    lore.add("");
                    lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
                    
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                
                inventory.setItem(slot, item);
                
                // Avançar para o próximo slot
                slot++;
                if (slot % 9 == 8) {
                    slot += 2;
                }
            }
        }
        
        // Item de voltar
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(TextUtil.color("&c&lVoltar"));
            backItem.setItemMeta(backMeta);
        }
        inventory.setItem(27, backItem);
        
        // Preencher espaços vazios com painéis de vidro
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
        
        // Registrar o pet para callback
        plugin.getGuiManager().registerPetForCosmetics(player, pet);
        
        player.openInventory(inventory);
    }
    
    /**
     * Processa a compra de um cosmético
     */
    public void handleCosmeticPurchase(Player player, AbstractPet pet, int slot) {
        // Obter configuração de cosméticos
        ConfigurationSection cosmeticsConfig = plugin.getConfigManager().getMainConfig().getConfigurationSection("cosmetics.particles");
        if (cosmeticsConfig == null) return;
        
        // Mapear slot para key de cosmético
        int currentSlot = 10;
        String selectedCosmeticKey = null;
        
        for (String cosmeticKey : cosmeticsConfig.getKeys(false)) {
            if (currentSlot == slot) {
                selectedCosmeticKey = cosmeticKey;
                break;
            }
            
            currentSlot++;
            if (currentSlot % 9 == 8) {
                currentSlot += 2;
            }
        }
        
        if (selectedCosmeticKey == null) {
            // Se for o botão de voltar
            if (slot == 27) {
                openPetManagement(player, pet);
            }
            return;
        }
        
        String particleType = cosmeticsConfig.getString(selectedCosmeticKey + ".particle-type");
        String cosmeticName = cosmeticsConfig.getString(selectedCosmeticKey + ".name");
        int cost = cosmeticsConfig.getInt(selectedCosmeticKey + ".cost", 500);
        
        // Verificar se o jogador já tem o cosmético
        if (particleType != null && particleType.equals(pet.getCosmeticParticle())) {
            // Desativar o cosmético
            pet.setParticleEffect(null);
            plugin.getPetManager().savePet(pet);
            
            player.sendMessage(plugin.formatMessage("&aCosmético desativado!"));
            openCosmeticsMenu(player, pet);
            return;
        }
        
        // Verificar se o jogador tem dinheiro suficiente
        if (plugin.getEconomy().getBalance(player) < cost) {
            player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("general.not-enough-money")
                    .replace("%price%", String.valueOf(cost))));
            return;
        }
        
        // Cobrar o jogador
        plugin.getEconomy().withdrawPlayer(player, cost);
        
        // Aplicar o cosmético
        if (particleType != null) {
            pet.setParticleEffect(particleType);
            plugin.getPetManager().savePet(pet);
            
            player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("general.cosmetic-purchased")
                    .replace("%cosmetic_name%", cosmeticName)
                    .replace("%pet_name%", pet.getPetName())));
            
            // Atualizar o menu
            openCosmeticsMenu(player, pet);
        }
    }
    
    /**
     * Abre o menu de variantes para o pet
     */
    public void openVariantMenu(Player player, AbstractPet pet) {
        String title = TextUtil.color("&5&l✨ &9&lM4Pets &8&l| &6&l🎨 Aparência &6&l🎨");
        Inventory inventory = Bukkit.createInventory(null, 36, title);
        
        // Obter lista de variantes para este tipo de pet
        ConfigurationSection petConfig = plugin.getConfigManager().getMainConfig().getConfigurationSection(
            "pets." + pet.getType().getConfigCategory() + "." + pet.getType().name().toLowerCase()
        );
        
        if (petConfig != null) {
            List<String> variants = petConfig.getStringList("variants");
            int slot = 10;
            
            for (String variant : variants) {
                Material icon = Material.valueOf(petConfig.getString("icon", "BARRIER"));
                
                ItemStack item = new ItemStack(icon);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(TextUtil.color("&6&l🎨 " + variant));
                    
                    List<String> lore = new ArrayList<>();
                    lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
                    
                    if (variant.equalsIgnoreCase(pet.getVariant())) {
                        lore.add(TextUtil.color("&a&l✅ &fVariante Selecionada"));
                        lore.add(TextUtil.color("&7🎯 &fEsta é a aparência atual"));
                        lore.add(TextUtil.color("&7   &fdo seu pet!"));
                        lore.add("");
                        lore.add(TextUtil.color("&2&l🎪 &aJá equipado"));
                    } else {
                        lore.add(TextUtil.color("&7🌈 &fAparência: &b" + variant));
                        lore.add(TextUtil.color("&7✨ &fMude o visual do seu pet"));
                        lore.add(TextUtil.color("&7   &fpara esta variante!"));
                        lore.add("");
                        lore.add(TextUtil.color("&e&l👆 &6Clique para selecionar"));
                    }
                    lore.add("");
                    lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
                    
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                
                inventory.setItem(slot, item);
                
                // Avançar para o próximo slot
                slot++;
                if (slot % 9 == 8) {
                    slot += 2;
                }
            }
        }
        
        // Item de voltar
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(TextUtil.color("&c&lVoltar"));
            backItem.setItemMeta(backMeta);
        }
        inventory.setItem(27, backItem);
        
        // Preencher espaços vazios com painéis de vidro
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
        
        // Registrar o pet para callback
        plugin.getGuiManager().registerPetForVariant(player, pet);
        
        player.openInventory(inventory);
    }
    
    /**
     * Processa a seleção de uma variante
     */
    public void handleVariantSelection(Player player, AbstractPet pet, int slot) {
        // Obter lista de variantes para este tipo de pet
        ConfigurationSection petConfig = plugin.getConfigManager().getMainConfig().getConfigurationSection(
            "pets." + pet.getType().getConfigCategory() + "." + pet.getType().name().toLowerCase()
        );
        
        if (petConfig != null) {
            List<String> variants = petConfig.getStringList("variants");
            int currentSlot = 10;
            
            for (String variant : variants) {
                if (currentSlot == slot) {
                    // Aplicar a variante selecionada
                    pet.setVariant(variant);
                    plugin.getPetManager().savePet(pet);
                    
                    player.sendMessage(plugin.formatMessage("&aAparência do pet alterada para: &e" + variant));
                    openPetManagement(player, pet);
                    return;
                }
                
                currentSlot++;
                if (currentSlot % 9 == 8) {
                    currentSlot += 2;
                }
            }
        }
        
        // Se for o botão de voltar
        if (slot == 27) {
            openPetManagement(player, pet);
        }
    }
}