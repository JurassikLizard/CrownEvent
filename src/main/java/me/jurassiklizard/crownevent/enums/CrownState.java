package me.jurassiklizard.crownevent.enums;

public enum CrownState {
    // ALL CAUSES SHOULD BE EXTECUTED IN switch(crownState) -> CrownManager#setCrownState(CrownState crownState)

    DESPAWNED,  /* {
        DONE?
        Inactive Event
        Causes{
            Command not being run
            Stop command being run
            Read as inactive (state:2/isRunning:false) from config
        }
        Effects{
            Nothing happens in the plugin
        }
    }*/
    PODIUM, /* {
        DONE
        Podium mode
        Causes{
            Causes crown to be created, if it does not exist
            Teleports crown to random spot within radius
            Should never be active outside of radius
        }
        Effects{
            Announcement about new location of crown.
        }
    }*/
    SEMI_TELEPORT, /* {
        DONE
        Doesn't go into podium mode after 5 minutes
        Causes{
            Player disconnects - Runs command from "player-drop-crown"
        }
        Effects{
            Standard crown spawn at location (glowing, along with standard event subscribers)
        }
    }*/
    CONTROLLED, /* {
        DONE
        In player inventory
        Causes{
            Player picked-up crown - Runs command from "player-pickup-crown"
            Player dying that is controlling crown - Runs command from "player-drop-crown"
        }
        Effects{
            Crown is moved to player inventory, commands are run based on what happened.
            Any 5 minute timers are cancelled
        }
    }*/
    TELEPORT, /* {
        DONE
        5-minute timer activated
        Causes{
            Player dropped crown - Runs command from "player-drop-crown"
            Server starts active (state:1/isRunning:true) and location from 1-minute cache is stored
        }
        Effects{
            5 minute timer starts, if CONTROLLED state isn't entered, then state is set to Podium.
        }
    }*/
    DAMAGE, /* {
        DONE
        () -> Podium Mode
        Causes{
            Crown taking any damage (Lava, Fire, Void, etc.)
        }
        Effects{
            State is immediately set to Podium, along with cancellation of any damage taken.
        }
    }*/

    // TODO: Every minute store current location of crown or crown holder, set to null if crown is not spawned
    // TODO: On server startup, create a new crown at stored location
}
