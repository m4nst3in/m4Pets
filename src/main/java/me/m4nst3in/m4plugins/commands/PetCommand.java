package me.m4nst3in.m4plugins.commands;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.abstractpets.AbstractPet;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PetCommand implements CommandExecutor, TabCompleter {
    
    private final M4Pets plugin;
    
    public PetCommand(M4Pets plugin) {
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
            // Abrir o menu principal
            plugin.getGuiManager().openMainMenu(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "help":
                sendHelpMessage(player);
                break;
                
            case "reload":
                if (!player.hasPermission("m4pets.admin")) {
                    player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("general.no-permission")));
                    return true;
                }
                
                plugin.getConfigManager().reloadConfigurations();
                player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("general.reload-config")));
                break;
                
            case "list":
                showPetsList(player);
                break;
                
            case "summon":
                if (args.length < 2) {
                    player.sendMessage(plugin.formatMessage("&cUso correto: /pets summon <nome do pet>"));
                    return true;
                }
                
                String petName = args[1];
                AbstractPet pet = plugin.getPetManager().getPlayerPetByName(player.getUniqueId(), petName);
                
                if (pet == null) {
                    player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("general.pet-not-found")));
                    return true;
                }
                
                plugin.getPetManager().spawnPet(player, pet);
                break;
                
            default:
                sendHelpMessage(player);
                break;
        }
        
        return true;
    }
    
    /**
     * Envia a mensagem de ajuda para o jogador
     */
    private void sendHelpMessage(Player player) {
        player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("help.header")));
        player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("help.pets")));
        
        if (player.hasPermission("m4pets.admin")) {
            player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("help.reload")));
        }
        
        player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("help.summon")));
        player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("help.list")));
        player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("help.help")));
    }
    
    /**
     * Mostra uma lista dos pets do jogador no chat
     */
    private void showPetsList(Player player) {
        Collection<AbstractPet> pets = plugin.getPetManager().getPlayerPets(player.getUniqueId());
        
        if (pets.isEmpty()) {
            player.sendMessage(plugin.formatMessage("&cVocê não possui nenhum pet."));
            return;
        }
        
        player.sendMessage(plugin.formatMessage("&9&l=== &bSeus Pets &9&l==="));
        
        for (AbstractPet pet : pets) {
            String status = pet.isDead() ? "&c&lMORTO" : "&a&lVIVO";
            player.sendMessage(plugin.formatMessage("&e" + pet.getPetName() + " &7- &fNível " + pet.getLevel() + " &7- " + status));
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("help");
            completions.add("list");
            completions.add("summon");
            
            if (sender.hasPermission("m4pets.admin")) {
                completions.add("reload");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("summon") && sender instanceof Player) {
            Player player = (Player) sender;
            Collection<AbstractPet> pets = plugin.getPetManager().getPlayerPets(player.getUniqueId());
            
            for (AbstractPet pet : pets) {
                completions.add(pet.getPetName());
            }
        }
        
        return completions;
    }
}