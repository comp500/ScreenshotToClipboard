// See HeadlessFix for a description of what this does
function initializeCoreMod() {
    return {
        'coremodone': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.Minecraft'
            },
            'transformer': function (classNode) {
            	var asmHandler = "link/infra/screenshotclipboard/HeadlessFix";

                var Opcodes = Java.type("org.objectweb.asm.Opcodes");

                var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
                var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");

                var api = Java.type('net.minecraftforge.coremod.api.ASMAPI');

                var methods = classNode.methods;
                // run() method
                var targetMethod = api.mapMethod("func_99999_d");

                for (m in methods)
                {
                    var method = methods[m];
                    if (method.name === targetMethod)
                    {
                        var code = method.instructions;
                        var instr = code.toArray();
                        for (t in instr)
                        {
                            var instruction = instr[t];
                            code.insertBefore(instruction, new MethodInsnNode(Opcodes.INVOKESTATIC, asmHandler, "run", "()V", false));
                            break;
                        }
                        break;
                    }
                }

                return classNode;
            }
        }
    }
}