package de.mrjulsen.dragnsounds.commands;

import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.api.ServerApi;
import de.mrjulsen.dragnsounds.commands.arguments.SoundSourceArgument;
import de.mrjulsen.dragnsounds.commands.arguments.SoundChannelsArgument;
import de.mrjulsen.dragnsounds.commands.arguments.SoundFileArgument;
import de.mrjulsen.dragnsounds.commands.arguments.SoundLocationArgument;
import de.mrjulsen.dragnsounds.config.CommonConfig;
import de.mrjulsen.dragnsounds.core.ServerSoundManager;
import de.mrjulsen.dragnsounds.core.data.ESoundType;
import de.mrjulsen.dragnsounds.core.data.PlaybackConfig;
import de.mrjulsen.dragnsounds.core.ext.CustomSoundInstance;
import de.mrjulsen.dragnsounds.core.ext.CustomSoundSource;
import de.mrjulsen.dragnsounds.core.ffmpeg.AudioSettings;
import de.mrjulsen.dragnsounds.core.ffmpeg.EChannels;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.dragnsounds.net.stc.SoundUploadCommandPacket;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.commands.arguments.AngleArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CustomSoundCommand {

    private static final String CMD_NAME = "sound";
    
    private static final String SUB_PLAY = "play";
    private static final String SUB_STOP = "stop";
    private static final String SUB_UPLOAD = "upload";
    private static final String SUB_DELETE = "delete";
    private static final String SUB_SHOW_FOLDER = "openFolder";
    private static final String SUB_CLEAN_UP = "cleanUp";
    private static final String SUB_HELP = "help";
    private static final String SUB_MODIFY = "modify";
    private static final String SUB_MODIFY_VOL = "volume";
    private static final String SUB_MODIFY_PITCH = "pitch";
    private static final String SUB_MODIFY_ATTENUATION_DISTANCE = "attenuationDistance";
    private static final String SUB_MODIFY_DOPPLER = "doppler";
    private static final String SUB_MODIFY_CONE = "cone";
    private static final String SUB_MODIFY_POSITION = "pos";
    private static final String SUB_MODIFY_PAUSE = "pause";
    private static final String SUB_MODIFY_RESUME = "resume";
    private static final String SUB_MODIFY_SEEK = "seek"; 

    private static final String ARG_SOUND_FILE = "soundFile";
    private static final String ARG_PLAYERS = "targets";
    private static final String ARG_PLAYER = "target";
    private static final String ARG_SOURCE = "source";
    private static final String ARG_VOLUME = "volume";
    private static final String ARG_PITCH = "pitch";
    private static final String ARG_POSITION = "position";
    private static final String ARG_ATTENUATION_DISTANCE = "attenuationDistance";
    private static final String ARG_TICKS_OFFSET = "ticksOffset";
    private static final String ARG_SHOW_LABEL = "showLabel";
    private static final String ARG_LOCATION = "location";
    private static final String ARG_DISPLAY_NAME = "displayName";
    private static final String ARG_CHANNELS = "channels";
    private static final String ARG_BIT_RATE = "bitRate";
    private static final String ARG_SAMPLING_RATE = "samplingRate";
    private static final String ARG_QUALITY = "quality";
    private static final String ARG_SHOW_PROGRESS = "showProgress";
    private static final String ARG_DIRECTION = "direction";
    private static final String ARG_VELOCITY = "velocity";
    private static final String ARG_ANGLE_A = "angleA";
    private static final String ARG_ANGLE_B = "AngleB";
    private static final String ARG_OUTER_GAIN = "outerGain";
    private static final String ARG_DOPPLER = "dopplerFactor";
    
    @SuppressWarnings("all")
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandSelection selection) {        
        
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(CMD_NAME)
            .requires(x -> x.hasPermission(CommonConfig.USE_SOUND_COMMAND_PERMISSION.get()))
            .then(Commands.literal(SUB_PLAY)
                .then(Commands.argument(ARG_SOUND_FILE, SoundFileArgument.location())
                    .executes(x -> playSound(x.getSource(), fileArg(x) ))
                    .then(Commands.argument(ARG_PLAYERS, EntityArgument.players())
                        .executes(x -> playSound(x.getSource(), fileArg(x), playersArg(x)))
                        .then(Commands.argument(ARG_SOURCE, SoundSourceArgument.soundSource())
                            .executes(x -> playSound(x.getSource(), fileArg(x), playersArg(x), sourceArg(x)))
                            .then(Commands.argument(ARG_VOLUME, FloatArgumentType.floatArg(CustomSoundInstance.VOLUME_MIN, CustomSoundInstance.VOLUME_MAX))
                                .executes(x -> playSound(x.getSource(), fileArg(x), playersArg(x), sourceArg(x), volumeArg(x)))
                                .then(Commands.argument(ARG_PITCH, FloatArgumentType.floatArg(CustomSoundInstance.PITCH_MIN, CustomSoundInstance.PITCH_MAX))
                                    .executes(x -> playSound(x.getSource(), fileArg(x), playersArg(x), sourceArg(x), volumeArg(x), pitchArg(x)))
                                    .then(Commands.argument(ARG_POSITION, Vec3Argument.vec3())
                                        .executes(x -> playSound(x.getSource(), fileArg(x), playersArg(x), sourceArg(x), volumeArg(x), pitchArg(x), posArg(x)))
                                        .then(Commands.argument(ARG_ATTENUATION_DISTANCE, IntegerArgumentType.integer(0))
                                            .executes(x -> playSound(x.getSource(), fileArg(x), playersArg(x), sourceArg(x), volumeArg(x), pitchArg(x), posArg(x), attenuationArg(x)))
                                            .then(Commands.argument(ARG_TICKS_OFFSET, IntegerArgumentType.integer(0))
                                                .executes(x -> playSound(x.getSource(), fileArg(x), playersArg(x), sourceArg(x), volumeArg(x), pitchArg(x), posArg(x), attenuationArg(x), ticksArg(x)))
                                                .then(Commands.argument(ARG_SHOW_LABEL, BoolArgumentType.bool())
                                                    .executes(x -> playSound(x.getSource(), fileArg(x), playersArg(x), sourceArg(x), volumeArg(x), pitchArg(x), posArg(x), attenuationArg(x), ticksArg(x), showLabelArg(x)))
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ).then(Commands.literal(SUB_STOP)
                .executes(x -> stopSound(x.getSource()))
                .then(Commands.argument(ARG_PLAYERS, EntityArgument.players())
                    .executes(x -> stopSound(x.getSource(), playersArg(x)))
                    .then(Commands.argument(ARG_SOUND_FILE, SoundFileArgument.location())
                        .executes(x -> stopSound(x.getSource(), playersArg(x), fileArg(x)))
                    )
                )
            ).then(Commands.literal(SUB_MODIFY)
                .then(Commands.argument(ARG_PLAYER, EntityArgument.player())
                    .then(Commands.argument(ARG_SOUND_FILE, SoundFileArgument.location())
                        .then(Commands.literal(SUB_MODIFY_VOL)
                            .then(Commands.argument(ARG_VOLUME, FloatArgumentType.floatArg(CustomSoundInstance.VOLUME_MIN, CustomSoundInstance.VOLUME_MAX))
                                .executes(x -> volume(x.getSource(), playerArg(x), fileArg(x), volumeArg(x)))
                            )
                        ).then(Commands.literal(SUB_MODIFY_PITCH)
                            .then(Commands.argument(ARG_PITCH, FloatArgumentType.floatArg(CustomSoundInstance.PITCH_MIN, CustomSoundInstance.PITCH_MAX))
                                .executes(x -> pitch(x.getSource(), playerArg(x), fileArg(x), pitchArg(x)))
                            )
                        ).then(Commands.literal(SUB_MODIFY_ATTENUATION_DISTANCE)
                            .then(Commands.argument(ARG_ATTENUATION_DISTANCE, IntegerArgumentType.integer(0))
                                .executes(x -> attenuationDistance(x.getSource(), playerArg(x), fileArg(x), attenuationArg(x)))
                            )
                        ).then(Commands.literal(SUB_MODIFY_CONE)
                            .then(Commands.argument(ARG_DIRECTION, RotationArgument.rotation())
                                .then(Commands.argument(ARG_ANGLE_A, AngleArgument.angle())
                                    .then(Commands.argument(ARG_ANGLE_B, AngleArgument.angle())
                                        .executes(x -> cone(x.getSource(), playerArg(x), fileArg(x), directionArg(x), angleAArg(x), angleBArg(x), 1.0f))
                                        .then(Commands.argument(ARG_OUTER_GAIN, FloatArgumentType.floatArg(CustomSoundInstance.VOLUME_MIN, CustomSoundInstance.VOLUME_MAX))
                                            .executes(x -> cone(x.getSource(), playerArg(x), fileArg(x), directionArg(x), angleAArg(x), angleBArg(x), outerGainArg(x)))
                                        )
                                    )
                                )
                            )
                        ).then(Commands.literal(SUB_MODIFY_DOPPLER)
                            .then(Commands.argument(ARG_VELOCITY, Vec3Argument.vec3())
                                .then(Commands.argument(ARG_DOPPLER, FloatArgumentType.floatArg(0))
                                    .executes(x -> doppler(x.getSource(), playerArg(x), fileArg(x), velocityArg(x), dopplerArg(x)))
                                )
                            )
                        ).then(Commands.literal(SUB_MODIFY_SEEK)
                            .then(Commands.argument(ARG_TICKS_OFFSET, IntegerArgumentType.integer(0))
                                .executes(x -> seek(x.getSource(), playerArg(x), fileArg(x), ticksArg(x)))
                            )
                        ).then(Commands.literal(SUB_MODIFY_PAUSE)
                            .executes(x -> setPaused(x.getSource(), playerArg(x), fileArg(x), true))
                        ).then(Commands.literal(SUB_MODIFY_RESUME)
                            .executes(x -> setPaused(x.getSource(), playerArg(x), fileArg(x), false))
                        ).then(Commands.literal(SUB_MODIFY_POSITION)
                            .then(Commands.argument(ARG_POSITION, Vec3Argument.vec3())
                                .executes(x -> pos(x.getSource(), playerArg(x), fileArg(x), posArg(x)))
                            )
                        )
                    )
                )
            ).then(Commands.literal(SUB_UPLOAD).requires(x -> x.hasPermission(CommonConfig.MANAGE_SOUND_COMMAND_PERMISSION.get()))
                .then(Commands.argument(ARG_LOCATION, SoundLocationArgument.location())
                    .then(Commands.argument(ARG_DISPLAY_NAME, StringArgumentType.string())
                        .executes(x -> uploadSound(x.getSource(), locationArg(x), displayNameArg(x)))
                        .then(Commands.argument(ARG_PLAYER, EntityArgument.player())
                            .executes(x -> uploadSound(x.getSource(), locationArg(x), displayNameArg(x), playerArg(x)))
                            .then(Commands.argument(ARG_CHANNELS, SoundChannelsArgument.channels())
                                .then(Commands.argument(ARG_BIT_RATE, IntegerArgumentType.integer(1))
                                    .then(Commands.argument(ARG_SAMPLING_RATE, IntegerArgumentType.integer(1))
                                        .then(Commands.argument(ARG_QUALITY, IntegerArgumentType.integer(1, 10))
                                            .executes(x -> uploadSound(x.getSource(), locationArg(x), displayNameArg(x), playerArg(x), new AudioSettings(channelsArg(x), bitRateArg(x), samplingRateArg(x), qualityArg(x))))
                                            .then(Commands.argument(ARG_SHOW_PROGRESS, BoolArgumentType.bool())
                                                .executes(x -> uploadSound(x.getSource(), locationArg(x), displayNameArg(x), playerArg(x), new AudioSettings(channelsArg(x), bitRateArg(x), samplingRateArg(x), qualityArg(x)), showProgress(x), null))
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            
            ).then(Commands.literal(SUB_DELETE).requires(x -> x.hasPermission(CommonConfig.MANAGE_SOUND_COMMAND_PERMISSION.get()))
                .then(Commands.argument(ARG_SOUND_FILE, SoundFileArgument.location())
                    .executes(x -> deleteSound(x.getSource(), fileArg(x)))
                )
            ).then(Commands.literal(SUB_CLEAN_UP).requires(x -> x.hasPermission(CommonConfig.MANAGE_SOUND_COMMAND_PERMISSION.get()))
                .executes(x -> {
                    ServerSoundManager.cleanUp(x.getSource().getLevel(), true);
                    x.getSource().sendSuccess(() -> TextUtils.translate("gui." + DragNSounds.MOD_ID + ".commands.sound.cleanup"), false);        
                    return 1;
                })
            ).then(Commands.literal(SUB_HELP)
                .executes(x -> {
                    Util.getPlatform().openUri(DragNSounds.DOCUMENTATION_UTL);
                    return 1;
                })
            )
        ;

        if (selection == CommandSelection.INTEGRATED) {
            builder = builder.then(Commands.literal(SUB_SHOW_FOLDER).requires(x -> x.hasPermission(CommonConfig.MANAGE_SOUND_COMMAND_PERMISSION.get()))
                .executes(x -> {
                    Util.getPlatform().openFile(SoundLocation.getModDirectory(x.getSource().getLevel()).toFile());
                    return 1;
                })
            );
        }

        dispatcher.register(builder);
    }

    

    private static int playSound(CommandSourceStack cmd, SoundFile file, ServerPlayer[] players, SoundSource source, float volume, float pitch, Vec3 pos, int attenuationDistance, int ticksOffset, boolean showLabel) {
        int id = (int)ServerApi.playSound(file, new PlaybackConfig(pos == null ? ESoundType.UI : ESoundType.WORLD, source.getName(), volume, pitch, pos, attenuationDistance, false, ticksOffset, showLabel), players, (player, i, status) -> {});
        cmd.sendSuccess(() -> TextUtils.translate("gui." + DragNSounds.MOD_ID + ".commands.sound.play", file.getDisplayName(), players.length), false);
        return id;
    }

    private static int playSound(CommandSourceStack cmd, SoundFile file, ServerPlayer[] players, SoundSource source, float volume, float pitch, Vec3 pos, int attenuationDistance, int ticksOffset) {
        return playSound(cmd, file, players, source, volume, pitch, pos, attenuationDistance, ticksOffset, false);
    }

    private static int playSound(CommandSourceStack cmd, SoundFile file, ServerPlayer[] players, SoundSource source, float volume, float pitch, Vec3 pos, int attenuationDistance) {
        return playSound(cmd, file, players, source, volume, pitch, pos, attenuationDistance, 0);
    }

    private static int playSound(CommandSourceStack cmd, SoundFile file, ServerPlayer[] players, SoundSource source, float volume, float pitch, Vec3 pos) {
        return playSound(cmd, file, players, source, volume, pitch, pos, CustomSoundInstance.ATTENUATION_DISTANCE_DEFAULT);
    }

    private static int playSound(CommandSourceStack cmd, SoundFile file, ServerPlayer[] players, SoundSource source, float volume, float pitch) {
        return playSound(cmd, file, players, source, volume, pitch, null);
    }

    private static int playSound(CommandSourceStack cmd, SoundFile file, ServerPlayer[] players, SoundSource source, float volume) {
        return playSound(cmd, file, players, source, volume, CustomSoundInstance.PITCH_DEFAULT);
    }

    private static int playSound(CommandSourceStack cmd, SoundFile file, ServerPlayer[] players, SoundSource source) {
        return playSound(cmd, file, players, source, CustomSoundInstance.VOLUME_DEFAULT);
    }

    private static int playSound(CommandSourceStack cmd, SoundFile file, ServerPlayer[] players) {
        return playSound(cmd, file, players, CustomSoundSource.getSoundSourceByName(CustomSoundSource.CUSTOM.getName()));
    }

    private static int playSound(CommandSourceStack cmd, SoundFile file) throws CommandSyntaxException {
        return playSound(cmd, file, new ServerPlayer[] {cmd.getPlayerOrException()});
    }

    
    private static SoundFile fileArg(CommandContext<CommandSourceStack> stack) {
        return stack.getArgument(ARG_SOUND_FILE, SoundFile.class);
    }
    
    private static ServerPlayer[] playersArg(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        return EntityArgument.getPlayers(stack, ARG_PLAYERS).toArray(ServerPlayer[]::new);
    }
    
    private static ServerPlayer playerArg(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        return EntityArgument.getPlayer(stack, ARG_PLAYER);
    }

    private static SoundSource sourceArg(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        return stack.getArgument(ARG_SOURCE, SoundSource.class);
    }

    private static float volumeArg(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        return FloatArgumentType.getFloat(stack, ARG_VOLUME);
    }

    private static float pitchArg(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        return FloatArgumentType.getFloat(stack, ARG_PITCH);
    }

    private static Vec3 posArg(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        return Vec3Argument.getVec3(stack, ARG_POSITION);
    }

    private static int attenuationArg(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        return IntegerArgumentType.getInteger(stack, ARG_ATTENUATION_DISTANCE);
    }

    private static int ticksArg(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        return IntegerArgumentType.getInteger(stack, ARG_TICKS_OFFSET);
    }

    private static boolean showLabelArg(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        return BoolArgumentType.getBool(stack, ARG_SHOW_LABEL);
    }

    private static Vec3 directionArg(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        Vec2 vec = RotationArgument.getRotation(stack, ARG_DIRECTION).getRotation(stack.getSource());
        return new Vec3(vec.x, vec.y, 0);
    }

    private static Vec3 velocityArg(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        return Vec3Argument.getVec3(stack, ARG_VELOCITY);
    }

    private static float angleAArg(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        return AngleArgument.getAngle(stack, ARG_ANGLE_A);
    }
    
    private static float angleBArg(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        return AngleArgument.getAngle(stack, ARG_ANGLE_B);
    }
    
    private static float outerGainArg(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        return FloatArgumentType.getFloat(stack, ARG_OUTER_GAIN);
    }    
    
    private static float dopplerArg(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        return FloatArgumentType.getFloat(stack, ARG_DOPPLER);
    }



    private static int uploadSound(CommandSourceStack cmd, SoundLocation location, String displayName, ServerPlayer player, AudioSettings settings, boolean showProgressScreen, CompoundTag nbt) throws CommandSyntaxException {
        DragNSounds.net().sendToPlayer(player, new SoundUploadCommandPacket(new SoundFile.Builder(location, displayName, Map.of()), settings, showProgressScreen));
        cmd.sendSuccess(() -> TextUtils.translate("gui." + DragNSounds.MOD_ID + ".commands.sound.upload_started"), false);
        return 1;
    }

    private static int uploadSound(CommandSourceStack cmd, SoundLocation location, String displayName, ServerPlayer player, AudioSettings settings) throws CommandSyntaxException {
        return uploadSound(cmd, location, displayName, player, settings, true, null);
    }

    private static int uploadSound(CommandSourceStack cmd, SoundLocation location, String displayName, ServerPlayer player) throws CommandSyntaxException {
        return uploadSound(cmd, location, displayName, player, null);
    }
    
    private static int uploadSound(CommandSourceStack cmd, SoundLocation location, String displayName) throws CommandSyntaxException {
        return uploadSound(cmd, location, displayName, cmd.getPlayerOrException());
    }

    private static SoundLocation locationArg(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        return SoundLocationArgument.getFile(stack, ARG_LOCATION);
    }
    
    private static String displayNameArg(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        return StringArgumentType.getString(stack, ARG_DISPLAY_NAME);
    }
    
    private static EChannels channelsArg(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        return stack.getArgument(ARG_CHANNELS, EChannels.class);
    }
    
    private static int bitRateArg(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        return IntegerArgumentType.getInteger(stack, ARG_BIT_RATE);
    }
    
    private static int samplingRateArg(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        return IntegerArgumentType.getInteger(stack, ARG_SAMPLING_RATE);
    }
    
    private static byte qualityArg(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        return (byte)IntegerArgumentType.getInteger(stack, ARG_QUALITY);
    }
    
    private static boolean showProgress(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        return BoolArgumentType.getBool(stack, ARG_SHOW_PROGRESS);
    }




    private static int stopSound(CommandSourceStack cmd, ServerPlayer[] players, SoundFile file) throws CommandSyntaxException {
        ServerApi.stopAllSoundInstances(file, players);
        cmd.sendSuccess(() -> TextUtils.translate("gui." + DragNSounds.MOD_ID + ".commands.sound.stopped"), false);
        return 1;
    }

    private static int stopSound(CommandSourceStack cmd, ServerPlayer[] players) throws CommandSyntaxException {
        ServerApi.stopAllCustomSounds(players);
        cmd.sendSuccess(() -> TextUtils.translate("gui." + DragNSounds.MOD_ID + ".commands.sound.stop_all"), false);
        return 1;
    }

    private static int stopSound(CommandSourceStack cmd) throws CommandSyntaxException {
        return stopSound(cmd, new ServerPlayer[] {cmd.getPlayerOrException()});
    }





    private static int deleteSound(CommandSourceStack cmd, SoundFile file) throws CommandSyntaxException {
        ServerApi.deleteSound(file);
        cmd.sendSuccess(() -> TextUtils.translate("gui." + DragNSounds.MOD_ID + ".commands.sound.deleted"), false);
        return 1;
    }




    private static int volume(CommandSourceStack cmd, ServerPlayer player, SoundFile file, float volume) throws CommandSyntaxException {
        ServerApi.setVolumeAndPitchAllInstances(file, volume, -1, -1, new ServerPlayer[] {player});
        cmd.sendSuccess(() -> TextUtils.translate("gui." + DragNSounds.MOD_ID + ".commands.sound.modified"), false);
        return 1;
    }

    private static int pitch(CommandSourceStack cmd, ServerPlayer player, SoundFile file, float pitch) throws CommandSyntaxException {
        ServerApi.setVolumeAndPitchAllInstances(file, -1, pitch, -1, new ServerPlayer[] {player});
        cmd.sendSuccess(() -> TextUtils.translate("gui." + DragNSounds.MOD_ID + ".commands.sound.modified"), false);
        return 1;
    }

    private static int attenuationDistance(CommandSourceStack cmd, ServerPlayer player, SoundFile file, int attenuationDistance) throws CommandSyntaxException {
        ServerApi.setVolumeAndPitchAllInstances(file, -1, -1, attenuationDistance, new ServerPlayer[] {player});
        cmd.sendSuccess(() -> TextUtils.translate("gui." + DragNSounds.MOD_ID + ".commands.sound.modified"), false);
        return 1;
    }

    private static int pos(CommandSourceStack cmd, ServerPlayer player, SoundFile file, Vec3 pos) throws CommandSyntaxException {
        ServerApi.setPositionAllInstances(file, pos, new ServerPlayer[] {player});
        cmd.sendSuccess(() -> TextUtils.translate("gui." + DragNSounds.MOD_ID + ".commands.sound.modified"), false);
        return 1;
    }

    private static int cone(CommandSourceStack cmd, ServerPlayer player, SoundFile file, Vec3 direction, float angleA, float angleB, float outerGain) throws CommandSyntaxException {
        ServerApi.setConeAllInstances(file, direction, angleA, angleB, outerGain, new ServerPlayer[] {player});
        cmd.sendSuccess(() -> TextUtils.translate("gui." + DragNSounds.MOD_ID + ".commands.sound.modified"), false);
        return 1;
    }    

    private static int doppler(CommandSourceStack cmd, ServerPlayer player, SoundFile file, Vec3 velocity, float doppler) throws CommandSyntaxException {
        ServerApi.setDopplerAllInstances(file, doppler, velocity, new ServerPlayer[] {player});
        cmd.sendSuccess(() -> TextUtils.translate("gui." + DragNSounds.MOD_ID + ".commands.sound.modified"), false);
        return 1;
    }     

    private static int seek(CommandSourceStack cmd, ServerPlayer player, SoundFile file, int ticks) throws CommandSyntaxException {
        ServerApi.seekAllInstances(file, ticks, new ServerPlayer[] {player});
        cmd.sendSuccess(() -> TextUtils.translate("gui." + DragNSounds.MOD_ID + ".commands.sound.modified"), false);
        return 1;
    }     

    private static int setPaused(CommandSourceStack cmd, ServerPlayer player, SoundFile file, boolean paused) throws CommandSyntaxException {
        ServerApi.setSoundPausedAllInstances(file, paused, new ServerPlayer[] {player});
        cmd.sendSuccess(() -> TextUtils.translate("gui." + DragNSounds.MOD_ID + ".commands.sound.modified"), false);
        return 1;
    }
}