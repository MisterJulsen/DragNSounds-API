package de.mrjulsen.dragnsounds.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.ServerInstanceManager;
import de.mrjulsen.dragnsounds.net.stc.PrintDebugPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;

public class StatusCommand {

    private static final String CMD_NAME = DragNSounds.MOD_ID;
    
    private static final String SUB_STATUS = "status";
    
    @SuppressWarnings("all")
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandSelection selection) {        
        
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(CMD_NAME)
            .then(Commands.literal(SUB_STATUS)
                .executes(x -> status(x.getSource()))
            );

        dispatcher.register(builder);
    }


    private static int status(CommandSourceStack cmd) throws CommandSyntaxException {
        cmd.sendSuccess(() -> ServerInstanceManager.debugComponent(), false);
        DragNSounds.net().sendToPlayer(cmd.getPlayerOrException(), new PrintDebugPacket());
        return 1;
    }
}