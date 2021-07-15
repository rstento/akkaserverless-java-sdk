/*
 * Copyright 2021 Lightbend Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lightbend.akkasls.codegen.java

import com.lightbend.akkasls.codegen.ModelBuilder
import org.bitbucket.inkytonik.kiama.output.PrettyPrinterTypes.Document

object ValueEntitySourceGenerator {
  import SourceGenerator._

  private[codegen] def valueEntitySource(
      service: ModelBuilder.EntityService,
      entity: ModelBuilder.ValueEntity,
      packageName: String,
      className: String,
      interfaceClassName: String
  ): Document = {
    val messageTypes = service.commands.toSeq
        .flatMap(command => Seq(command.inputType, command.outputType)) ++ Seq(entity.state.fqn)

    val imports = (messageTypes
      .filterNot(_.parent.javaPackage == packageName)
      .map(typeImport) ++
    Seq(
      "com.akkaserverless.javasdk.valueentity.ValueEntityContext"
    )).distinct.sorted

    pretty(
      initialisedCodeComment <> line <> line <>
      "package" <+> packageName <> semi <> line <>
      line <>
      ssep(
        imports.map(pkg => "import" <+> pkg <> semi),
        line
      ) <> line <>
      line <>
      "/** A value entity. */" <> line <>
      `class`("public", s"$className extends $interfaceClassName") {

        """@SuppressWarnings("unused")""" <> line <>
        "private final ValueEntityContext entityContext;" <> line <>
        line <>
        constructor(
          "public",
          className,
          List("ValueEntityContext" <+> "entityContext")
        ) {
          "this.entityContext = entityContext;"
        } <> line <>
        line <>
        "@Override" <>
        line <>
        method(
          "public",
          qualifiedType(entity.state.fqn),
          "emptyState",
          Nil,
          emptyDoc
        )(
          "return" <+> qualifiedType(entity.state.fqn) <> ".getDefaultInstance();"
        ) <>
        line <>
        line <>
        ssep(
          service.commands.toSeq.map { command =>
            "@Override" <>
            line <>
            method(
              "public",
              "Effect" <> angles(qualifiedType(command.outputType)),
              lowerFirst(command.fqn.name),
              List(
                qualifiedType(entity.state.fqn) <+> "currentState",
                qualifiedType(command.inputType) <+> "command"
              ),
              emptyDoc
            ) {
              "return effects().error" <> parens(
                "\"The command handler for `" + command.fqn.name + "` is not implemented, yet\""
              ) <> semi
            }
          },
          line <> line
        )
      }
    )
  }

  private[codegen] def valueEntityHandler(service: ModelBuilder.EntityService,
                                          entity: ModelBuilder.ValueEntity,
                                          packageName: String,
                                          className: String): Document = {

    val imports = generateImports(
      service.commands,
      Some(entity.state),
      packageName,
      otherImports = Seq(
        "com.akkaserverless.javasdk.impl.AnySupport",
        "com.akkaserverless.javasdk.impl.EntityExceptions",
        "com.akkaserverless.javasdk.impl.valueentity.AdaptedCommandContextWithState",
        "com.akkaserverless.javasdk.lowlevel.ValueEntityHandler",
        "com.akkaserverless.javasdk.valueentity.CommandContext",
        "com.akkaserverless.javasdk.valueentity.ValueEntityBase",
        "com.akkaserverless.javasdk.valueentity.ValueEntityContext",
        "com.google.protobuf.Descriptors",
        "scalapb.UnknownFieldSet"
      )
    )

    val serviceApiOuterClass = service.fqn.parent.javaOuterClassname

    def makeCase(cmd: ModelBuilder.Command) = {
      val methodName = cmd.fqn.name
      val inputType = s"$serviceApiOuterClass.${cmd.inputType.name}"
      // Watch-out, this is special as an indentation sensitive
      s"""
        case "$methodName":
          return entity.${lowerFirst(methodName)}(
              parsedState,
              ${inputType}.parseFrom(command.getValue));
"""
    }

    val outerClassAndState = s"${entity.state.fqn.parent.javaOuterClassname}.${entity.state.fqn.name}"
    pretty(
      managedCodeCommentString <> line <> // fixme: why this can't be in string interpolation?
      s"""|package $packageName;
          |
          |$imports
          |
          |/** A value entity handler */
          |public class ${className}Handler extends ValueEntityHandler {
          |
          |  public static final Descriptors.ServiceDescriptor serviceDescriptor =
          |      ${service.fqn.parent.javaOuterClassname}.getDescriptor().findServiceByName("${service.fqn.name}");
          |  public static final String entityType = "${entity.entityType}";
          |
          |  private final ${className}Impl entity;
          |  
          |  ${className}Handler(ValueEntityContext entityContext) {
          |    this.entity = new ${className}Impl(entityContext);
          |  }
          |
          |  @Override
          |  public ValueEntityBase.Effect<? extends GeneratedMessageV3> handleCommand(
          |      Any command, Any state, CommandContext<Any> context) throws Throwable {
          |      
          |    $outerClassAndState parsedState =
          |      $outerClassAndState.parseFrom(state.getValue());
          |
          |    CommandContext<$outerClassAndState> adaptedContext =
          |        new AdaptedCommandContextWithState(context, parsedState);
          |
          |    entity.setCommandContext(Optional.of(adaptedContext));
          |    
          |    try {
          |      switch (context.commandName()) {
          |        ${service.commands.map(makeCase).mkString("")}
          |        default:
          |          throw new EntityExceptions.EntityException(
          |              context.entityId(),
          |              context.commandId(),
          |              context.commandName(),
          |              "No command handler found for command ["
          |                  + context.commandName()
          |                  + "] on "
          |                  + entity.getClass().toString());
          |      }
          |    } finally {
          |      entity.setCommandContext(Optional.empty;
          |    }
          |  }
          |  
          |  @Override
          |  public com.google.protobuf.any.Any emptyState() {
          |    return com.google.protobuf.any.Any.apply(
          |        AnySupport.DefaultTypeUrlPrefix()
          |          + "/"
          |          + ${outerClassAndState}.getDescriptor().getFullName(),
          |        entity.emptyState().toByteString(),
          |        UnknownFieldSet.empty());
          |  }
          |}""".stripMargin
    )

  }

  private[codegen] def abstractValueEntity(
      service: ModelBuilder.EntityService,
      entity: ModelBuilder.ValueEntity,
      packageName: String,
      className: String
  ): Document = {
    val messageTypes = service.commands.toSeq
        .flatMap(command => Seq(command.inputType, command.outputType)) ++ Seq(entity.state.fqn)

    val imports = (messageTypes
      .filterNot(_.parent.javaPackage == packageName)
      .map(typeImport) ++
    Seq(
      "com.akkaserverless.javasdk.valueentity.ValueEntityBase"
    )).distinct.sorted

    pretty(
      managedCodeComment <> line <> line <>
      "package" <+> packageName <> semi <> line <>
      line <>
      ssep(
        imports.map(pkg => "import" <+> pkg <> semi),
        line
      ) <> line <>
      line <>
      "/** A value entity. */"
      <> line <>
      `class`("public abstract", "Abstract" + className, "ValueEntityBase<" + qualifiedType(entity.state.fqn) + ">") {
        line <>
        ssep(
          service.commands.toSeq.map { command =>
            abstractMethod(
              "public",
              "Effect" <> angles(qualifiedType(command.outputType)),
              lowerFirst(command.fqn.name),
              List(
                qualifiedType(entity.state.fqn) <+> "currentState",
                qualifiedType(command.inputType) <+> "command"
              )
            ) <> semi
          },
          line <> line
        )
      }
    )
  }
}
