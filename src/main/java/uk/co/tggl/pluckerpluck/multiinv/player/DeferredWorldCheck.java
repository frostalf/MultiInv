package uk.co.tggl.pluckerpluck.multiinv.player;

import org.bukkit.entity.Player;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.listener.MIPlayerListener;

public class DeferredWorldCheck implements Runnable {
    
    Player player;
    MIPlayerListener listener;
    
    public DeferredWorldCheck(Player player, MIPlayerListener listener) {
        this.player = player;
        this.listener = listener;
    }
    
    @Override
    public void run() {
        // Seems banned players generate an exception... make sure they are actually logged in...
        if(player != null && player.isOnline()) {
            // Let's see if the player is in a world that doesn't exist anymore...
        	if(MIYamlFiles.saveonquit) {
                String currentworld = MIPlayerListener.getGroup(player.getWorld());
            	if(!player.hasPermission("multiinv.enderchestexempt")) {
                    // Load the enderchest inventory for this world from file.
                    listener.loadEnderchestState(player, currentworld);
                }
                if(!player.hasPermission("multiinv.exempt")) {
                    // Load the inventory for this world from file.
                    listener.loadPlayerState(player, currentworld);
                }
            }else if(MIYamlFiles.logoutworld.containsKey(player.getUniqueId().toString()) && MIYamlFiles.logoutworld.get(player.getUniqueId().toString()) != null) {
                String logoutworld = MIYamlFiles.logoutworld.get(player.getUniqueId().toString());
                String currentworld = MIPlayerListener.getGroup(player.getWorld());
                MultiInv.log.debug(player.getName() + " has logged in in world " + currentworld + ". Logout world was: " + logoutworld);
                //Inventory is saved on quit, no need to save on load.
                if(!currentworld.equals(logoutworld)) {
                    if(!player.hasPermission("multiinv.enderchestexempt")) {
                        // If they aren't in the same world they logged out of let's save their current inventory
                        listener.saveEnderchestState(player, logoutworld);
                        // and switch them to the correct inventory for this world.
                        listener.loadEnderchestState(player, currentworld);
                    }
                    if(!player.hasPermission("multiinv.exempt")) {
                        // If they aren't in the same world they logged out of let's save their current inventory
                        listener.savePlayerState(player, logoutworld);
                        // and switch them to the correct inventory for this world.
                        listener.loadPlayerState(player, currentworld);
                    }
                    MIYamlFiles.savePlayerLogoutWorld(player.getUniqueId(), currentworld);
                }
            }
        	//Let's execute any stored inventory edits.
        	MIPlayerGiveCache cache = listener.removePlayerGiveCache(player.getUniqueId());
        	if(cache != null) {
        		cache.ExecuteStoredInventoryChanges();
        	}
        }
    }
}
