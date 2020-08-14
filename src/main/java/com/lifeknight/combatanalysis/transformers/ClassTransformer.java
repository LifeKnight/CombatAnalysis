package com.lifeknight.combatanalysis.transformers;

import com.lifeknight.combatanalysis.utilities.Miscellaneous;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.util.Arrays;

import static com.lifeknight.combatanalysis.mod.Core.MOD_NAME;
import static org.objectweb.asm.Opcodes.*;

public class ClassTransformer implements IClassTransformer {

    private static final String[] classesBeingTransformed = {
            "net.minecraft.entity.EntityLivingBase",
            "net.minecraft.client.entity.EntityPlayerSP",
            "net.minecraft.client.entity.EntityOtherPlayerMP"
    };

    @Override
    public byte[] transform(String name, String transformedName, byte[] classBeingTransformed) {
        boolean isObfuscated = !name.equals(transformedName);
        int index = Arrays.asList(classesBeingTransformed).indexOf(transformedName);
        return index != -1 ? transform(index, classBeingTransformed, isObfuscated) : classBeingTransformed;
    }

    private static byte[] transform(int index, byte[] classBeingTransformed, boolean isObfuscated) {
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(classBeingTransformed);
            classReader.accept(classNode, 0);

            if (index == 0) {
                transformEntityLivingBase(classNode, isObfuscated);
            } else if (index == 1) {
                transformEntityPlayerSP(classNode, isObfuscated);
            } else {
                transformEntityOtherPlayerMP(classNode, isObfuscated);
            }

            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

            classNode.accept(classWriter);
            return classWriter.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return classBeingTransformed;
    }

    private static void transformEntityLivingBase(ClassNode entityLivingBase, boolean isObfuscated) {
        final String handleStatusUpdateName = isObfuscated ? "a" : "handleStatusUpdate";
        final String handleStatusUpdateDescription = "(B)V";

        for (MethodNode method : entityLivingBase.methods) {
            if (method.name.equals(handleStatusUpdateName) && method.desc.equals(handleStatusUpdateDescription)) {
                AbstractInsnNode targetNode = null;

                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (instruction.getOpcode() == ALOAD) {
                        targetNode = instruction.getPrevious();
                        break;
                    }
                }

                if (targetNode == null) {
                    Miscellaneous.logError("An error occurred while trying to insert method instruction into EntityLivingBase.");
                } else {
                    InsnList insnList = new InsnList();
                    insnList.add(new VarInsnNode(ALOAD, 0));
                    insnList.add(new MethodInsnNode(INVOKESTATIC, "com/lifeknight/combatanalysis/mod/Core", "onLivingHurt", "(Lnet/minecraft/entity/EntityLivingBase;)V", false));
                    method.instructions.insert(targetNode, insnList);
                    Miscellaneous.info("Successfully inserted instructions into EntityLivingBase.");
                }
                break;
            }
        }
    }

    private static void transformEntityPlayerSP(ClassNode entityPlayerSP, boolean isObfuscated) {
        final String attackEntityFromName = isObfuscated ? "a" : "attackEntityFrom";
        final String attackEntityFromDescription = isObfuscated ? "(Low;F)Z" : "(Lnet/minecraft/util/DamageSource;F)Z";

        for (MethodNode method : entityPlayerSP.methods) {
            if (method.name.equals(attackEntityFromName) && method.desc.equals(attackEntityFromDescription)) {
                AbstractInsnNode targetNode = null;

                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (instruction.getOpcode() == ICONST_0) {
                        targetNode = instruction.getPrevious();
                        break;
                    }
                }

                if (targetNode == null) {
                    Miscellaneous.logError("An error occurred while trying to insert method instructions into EntityPlayerSP.");
                } else {
                    InsnList insnList = new InsnList();
                    insnList.add(new VarInsnNode(ALOAD, 0));
                    insnList.add(new VarInsnNode(ALOAD, 1));
                    insnList.add(new MethodInsnNode(INVOKESTATIC, "com/lifeknight/combatanalysis/mod/Core", "onAttackEntityPlayerSPFrom", "(Lnet/minecraft/util/DamageSource;)V", false));
                    method.instructions.insert(targetNode, insnList);
                    Miscellaneous.info("Successfully inserted instructions into EntityPlayerSP.");
                }
                break;
            }
        }
    }

    private static void transformEntityOtherPlayerMP(ClassNode entityOtherPlayerMP, boolean isObfuscated) {
        final String attackEntityFromName = isObfuscated ? "a" : "attackEntityFrom";
        final String attackEntityFromDescription = isObfuscated ? "(Low;F)Z" : "(Lnet/minecraft/util/DamageSource;F)Z";

        for (MethodNode method : entityOtherPlayerMP.methods) {
            if (method.name.equals(attackEntityFromName) && method.desc.equals(attackEntityFromDescription)) {
                AbstractInsnNode targetNode = null;

                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (instruction.getOpcode() == ICONST_1) {
                        targetNode = instruction.getPrevious();
                        break;
                    }
                }

                if (targetNode == null) {
                    Miscellaneous.logError("An error occurred while trying to insert method instructions into EntityOtherPlayerMP.");
                } else {
                    InsnList insnList = new InsnList();
                    insnList.add(new VarInsnNode(ALOAD, 0));
                    insnList.add(new VarInsnNode(ALOAD, 1));
                    insnList.add(new MethodInsnNode(INVOKESTATIC, "com/lifeknight/combatanalysis/mod/Core", "onAttackEntityOtherPlayerMPFrom", "(Lnet/minecraft/client/entity/EntityOtherPlayerMP;Lnet/minecraft/util/DamageSource;)V", false));
                    method.instructions.insert(targetNode, insnList);
                    Miscellaneous.info("Successfully inserted instructions into EntityOtherPlayerMP.");
                }
                break;
            }
        }
    }
}