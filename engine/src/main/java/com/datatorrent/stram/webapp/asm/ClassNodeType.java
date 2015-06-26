/**
 * Copyright (C) 2015 DataTorrent, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datatorrent.stram.webapp.asm;


import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.tree.ClassNode;

/**
 * A special org.objectweb.asm.tree.ClassNode implementation which parse the type signature as well
 *
 * @see ClassNode
 * @since 2.1
 */
public class ClassNodeType extends ClassNode
{
  
  ClassSignatureVisitor csv = new ClassSignatureVisitor();
  
  @SuppressWarnings("unchecked")
  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
  {
    MethodNode mn = new MethodNode(access, name, desc, signature, exceptions);
    mn.typeVariableSignatureNode = csv;
    methods.add(mn);
    return mn;
  }
  
  
  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
  {
    // parse the signature first so Type variable can be captured from the signature
    if(signature!=null){
      SignatureReader sr = new SignatureReader(signature);
      sr.accept(csv);
    }
    super.visit(version, access, name, signature, superName, interfaces);
  }
  

}
