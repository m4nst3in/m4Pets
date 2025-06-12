package me.m4nst3in.m4plugins.commands;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.abstractpets.AbstractPet;
import me.m4nst3in.m4plugins.pets.abstractpets.WarriorPet;
import me.m4nst3in.m4plugins.pets.warriors.SkeletonPet;
import me.m4nst3in.m4plugins.pets.warriors.VindicatorPet;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Comando para controlar pets guerreiros
 * /warrior <subcmd> [args...]
 */
public class WarriorCommand implements CommandExecutor, TabCompleter {
    
    private final M4Pets plugin;
    
    public WarriorCommand(M4Pets plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.formatMessage("&cEste comando só pode ser usado por jogadores."));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "help":
                sendHelpMessage(player);
                break;
                
            case "target":
                handleTargetCommand(player, args);
                break;
                
            case "ai":
                handleAICommand(player, args);
                break;
                
            case "mode":
                handleModeCommand(player, args);
                break;
                
            case "ability":
                handleAbilityCommand(player, args);
                break;
                
            case "status":
                handleStatusCommand(player, args);
                break;
                
            case "cleartarget":
                handleClearTargetCommand(player, args);
                break;
                
            default:
                sendHelpMessage(player);
                break;
        }
        
        return true;
    }
    
    /**
     * Comando para definir alvo do pet guerreiro
     */
    private void handleTargetCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.formatMessage("&cUso: /warrior target <jogador|mob>"));
            return;
        }
        
        String targetName = args[1];
        
        WarriorPet warriorPet = getActiveWarriorPet(player);
        if (warriorPet == null) {
            player.sendMessage(plugin.formatMessage("&cVocê não tem nenhum pet guerreiro ativo."));
            return;
        }
        
        // Verificar se é um jogador
        Player targetPlayer = Bukkit.getPlayer(targetName);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            if (targetPlayer.getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage(plugin.formatMessage("&cSeu pet não pode atacar você mesmo!"));
                return;
            }
            
            double distance = player.getLocation().distance(targetPlayer.getLocation());
            if (distance > warriorPet.getAttackRadius()) {
                player.sendMessage(plugin.formatMessage("&cO jogador está muito longe! Distância máxima: " + 
                    (int) warriorPet.getAttackRadius() + " blocos."));
                return;
            }
            
            warriorPet.setTargetPlayer(targetPlayer.getName());
            player.sendMessage(plugin.formatMessage("&a" + warriorPet.getPetName() + " &eagora atacará &c" + targetPlayer.getName() + "&e!"));
            return;
        }
        
        // Verificar se é um tipo de mob válido
        try {
            EntityType entityType = EntityType.valueOf(targetName.toUpperCase());
            
            // Verificar se há mobs desse tipo na área
            boolean foundMobs = false;
            for (Entity entity : player.getNearbyEntities(warriorPet.getAttackRadius(), 
                                                          warriorPet.getAttackRadius(), 
                                                          warriorPet.getAttackRadius())) {
                if (entity.getType() == entityType && entity instanceof LivingEntity) {
                    foundMobs = true;
                    break;
                }
            }
            
            if (!foundMobs) {
                player.sendMessage(plugin.formatMessage("&cNenhum " + targetName + " encontrado na área."));
                return;
            }
            
            // Definir o tipo de mob como alvo
            warriorPet.setTargetMobType(entityType);
            player.sendMessage(plugin.formatMessage("&a" + warriorPet.getPetName() + " &eagora atacará &c" + 
                targetName.toLowerCase() + "s &ena área!"));
            
        } catch (IllegalArgumentException e) {
            player.sendMessage(plugin.formatMessage("&cTipo de mob inválido: " + targetName));
        }
    }
    
    /**
     * Comando para controlar IA do pet
     */
    private void handleAICommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.formatMessage("&cUso: /warrior ai <on|off>"));
            return;
        }
        
        String aiState = args[1].toLowerCase();
        
        WarriorPet warriorPet = getActiveWarriorPet(player);
        if (warriorPet == null) {
            player.sendMessage(plugin.formatMessage("&cVocê não tem nenhum pet guerreiro ativo."));
            return;
        }
        
        boolean enable = aiState.equals("on") || aiState.equals("ativar");
        warriorPet.setAIEnabled(enable);
        
        String status = enable ? "&aativada" : "&cdesativada";
        player.sendMessage(plugin.formatMessage("&eIA do pet &a" + warriorPet.getPetName() + " &efoi " + status + "&e!"));
    }
    
    /**
     * Comando para alterar modo de combate (apenas esqueleto)
     */
    private void handleModeCommand(Player player, String[] args) {
        WarriorPet warriorPet = getActiveWarriorPet(player);
        
        if (warriorPet == null) {
            player.sendMessage(plugin.formatMessage("&cVocê não tem nenhum pet guerreiro ativo."));
            return;
        }
        
        if (!(warriorPet instanceof SkeletonPet)) {
            player.sendMessage(plugin.formatMessage("&cApenas pets esqueletos podem trocar de modo de combate."));
            return;
        }
        
        SkeletonPet skeletonPet = (SkeletonPet) warriorPet;
        skeletonPet.toggleCombatMode();
    }
    
    /**
     * Comando para ativar habilidade especial
     */
    private void handleAbilityCommand(Player player, String[] args) {
        WarriorPet warriorPet = getActiveWarriorPet(player);
        
        if (warriorPet == null) {
            player.sendMessage(plugin.formatMessage("&cVocê não tem nenhum pet guerreiro ativo."));
            return;
        }
        
        if (!warriorPet.hasLevel5Ability()) {
            player.sendMessage(plugin.formatMessage("&cSeu pet precisa estar no nível 5 para usar habilidades especiais."));
            return;
        }
        
        // Ativar habilidade específica baseada no tipo
        if (warriorPet instanceof VindicatorPet) {
            ((VindicatorPet) warriorPet).activateBerserkerFury();
        } else {
            // Para outros pets, ativar habilidade padrão
            warriorPet.useLevel5Ability();
        }
        
        player.sendMessage(plugin.formatMessage("&a" + warriorPet.getPetName() + " &eusou sua habilidade especial!"));
    }
    
    /**
     * Comando para ver status do pet
     */
    private void handleStatusCommand(Player player, String[] args) {
        WarriorPet warriorPet = getActiveWarriorPet(player);
        
        if (warriorPet == null) {
            player.sendMessage(plugin.formatMessage("&cVocê não tem nenhum pet guerreiro ativo."));
            return;
        }
        
        player.sendMessage(plugin.formatMessage("&9&l=== &bStatus de " + warriorPet.getPetName() + " &9&l==="));
        player.sendMessage(plugin.formatMessage("&eNível: &f" + warriorPet.getLevel()));
        player.sendMessage(plugin.formatMessage("&eVida: &c" + (int)warriorPet.getHealth() + "&f/&c" + (int)warriorPet.getMaxHealth()));
        player.sendMessage(plugin.formatMessage("&eDano de Ataque: &f" + warriorPet.getAttackDamage()));
        player.sendMessage(plugin.formatMessage("&eVelocidade de Ataque: &f" + warriorPet.getAttackSpeed()));
        player.sendMessage(plugin.formatMessage("&eIA: " + (warriorPet.isAIEnabled() ? "&aAtivada" : "&cDesativada")));
        
        if (warriorPet.getTargetPlayerName() != null) {
            player.sendMessage(plugin.formatMessage("&eAlvo: &c" + warriorPet.getTargetPlayerName()));
        } else if (warriorPet.getTargetMobType() != null) {
            player.sendMessage(plugin.formatMessage("&eAlvo: &c" + warriorPet.getTargetMobType().name().toLowerCase() + "s"));
        }
        
        if (warriorPet instanceof SkeletonPet) {
            SkeletonPet skeleton = (SkeletonPet) warriorPet;
            String mode = skeleton.isRangedMode() ? "Arqueiro" : "Guerreiro";
            player.sendMessage(plugin.formatMessage("&eModo de Combate: &f" + mode));
        }
        
        if (warriorPet instanceof VindicatorPet) {
            VindicatorPet vindicator = (VindicatorPet) warriorPet;
            long chargeTime = vindicator.getChargeTimeRemaining();
            if (chargeTime > 0) {
                player.sendMessage(plugin.formatMessage("&eInvestida disponível em: &f" + (chargeTime / 1000) + "s"));
            } else {
                player.sendMessage(plugin.formatMessage("&eInvestida: &aDisponível"));
            }
        }
        
        if (warriorPet.hasLevel5Ability()) {
            player.sendMessage(plugin.formatMessage("&eHabilidade Especial: &a" + warriorPet.getLevel5AbilityDescription()));
        }
    }
    
    /**
     * Comando para limpar alvo
     */
    private void handleClearTargetCommand(Player player, String[] args) {
        WarriorPet warriorPet = getActiveWarriorPet(player);
        
        if (warriorPet == null) {
            player.sendMessage(plugin.formatMessage("&cVocê não tem nenhum pet guerreiro ativo."));
            return;
        }
        
        warriorPet.setTargetPlayer(null);
        warriorPet.setTargetMobType(null);
        if (warriorPet.getEntity() instanceof Mob) {
            ((Mob) warriorPet.getEntity()).setTarget(null);
        }
        
        player.sendMessage(plugin.formatMessage("&a" + warriorPet.getPetName() + " &enão tem mais alvos específicos."));
    }
    
    /**
     * Obtém o pet guerreiro ativo do jogador (automaticamente)
     */
    private WarriorPet getActiveWarriorPet(Player player) {
        Collection<AbstractPet> pets = plugin.getPetManager().getPlayerPets(player.getUniqueId());
        
        for (AbstractPet pet : pets) {
            if (pet instanceof WarriorPet && pet.isSpawned()) {
                return (WarriorPet) pet;
            }
        }
        
        return null;
    }
    
    /**
     * Envia mensagem de ajuda
     */
    private void sendHelpMessage(Player player) {
        player.sendMessage(plugin.formatMessage("&9&l=== &bComandos de Pets Guerreiros &9&l==="));
        player.sendMessage(plugin.formatMessage("&e/warrior target <jogador|mob> &f- Define alvo"));
        player.sendMessage(plugin.formatMessage("&e/warrior ai <on|off> &f- Controla IA"));
        player.sendMessage(plugin.formatMessage("&e/warrior mode &f- Troca modo (só esqueleto)"));
        player.sendMessage(plugin.formatMessage("&e/warrior ability &f- Usa habilidade especial"));
        player.sendMessage(plugin.formatMessage("&e/warrior status &f- Mostra status"));
        player.sendMessage(plugin.formatMessage("&e/warrior cleartarget &f- Remove alvo"));
        player.sendMessage(plugin.formatMessage("&e/warrior help &f- Mostra esta ajuda"));
        player.sendMessage(plugin.formatMessage("&7Nota: Os comandos funcionam com o pet guerreiro ativo"));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!(sender instanceof Player)) return completions;
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("target", "ai", "mode", "ability", "status", "cleartarget", "help"));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("target")) {
                // Sugerir jogadores online
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList()));
                
                // Sugerir tipos de mobs comuns
                completions.addAll(Arrays.asList("ZOMBIE", "SKELETON", "CREEPER", "SPIDER", "ENDERMAN", 
                    "WITCH", "PILLAGER", "VINDICATOR", "EVOKER"));
                
            } else if (subCommand.equals("ai")) {
                completions.addAll(Arrays.asList("on", "off", "ativar", "desativar"));
            }
        }
        
        return completions;
    }
}
