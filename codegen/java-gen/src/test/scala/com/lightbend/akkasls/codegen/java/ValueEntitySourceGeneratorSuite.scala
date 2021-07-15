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
class ValueEntitySourceGeneratorSuite extends munit.FunSuite {
  test("ValueEntity source") {

    val service = TestData.simpleEntityService()
    val entity = TestData.valueEntity()

    val packageName = "com.example.service"
    val className = "MyServiceImpl"
    val interfaceClassName = "AbstractMyService"
    val entityType = "my-valueentity-persistence"

    val sourceDoc =
      EntityServiceSourceGenerator.source(
        service,
        entity,
        packageName,
        className,
        interfaceClassName,
        entityType
      )
    assertEquals(
      sourceDoc.layout,
      """/* This code was initialised by Akka Serverless tooling.
        | * As long as this file exists it will not be re-generated.
        | * You are free to make changes to this file.
        | */
        |
        |package com.example.service;
        |
        |import com.akkaserverless.javasdk.valueentity.ValueEntityContext;
        |import com.example.service.persistence.EntityOuterClass;
        |import com.external.Empty;
        |
        |/** A value entity. */
        |public class MyServiceImpl extends AbstractMyService {
        |    @SuppressWarnings("unused")
        |    private final ValueEntityContext entityContext;
        |    
        |    public MyServiceImpl(ValueEntityContext entityContext) {
        |        this.entityContext = entityContext;
        |    }
        |    
        |    @Override
        |    public EntityOuterClass.MyState emptyState() {
        |        return EntityOuterClass.MyState.getDefaultInstance();
        |    }
        |    
        |    @Override
        |    public Effect<Empty> set(EntityOuterClass.MyState currentState, ServiceOuterClass.SetValue command) {
        |        return effects().error("The command handler for `Set` is not implemented, yet");
        |    }
        |    
        |    @Override
        |    public Effect<ServiceOuterClass.MyState> get(EntityOuterClass.MyState currentState, ServiceOuterClass.GetValue command) {
        |        return effects().error("The command handler for `Get` is not implemented, yet");
        |    }
        |}""".stripMargin
    )
  }

  test("Abstract ValueEntity source") {
    val service = TestData.simpleEntityService()
    val entity = TestData.valueEntity()
    val packageName = "com.example.service"
    val className = "MyService"

    val sourceDoc =
      EntityServiceSourceGenerator.interfaceSource(service, entity, packageName, className)
    assertEquals(
      sourceDoc.layout,
      """/* This code is managed by Akka Serverless tooling.
        | * It will be re-generated to reflect any changes to your protobuf definitions.
        | * DO NOT EDIT
        | */
        |
        |package com.example.service;
        |
        |import com.akkaserverless.javasdk.valueentity.ValueEntityBase;
        |import com.example.service.persistence.EntityOuterClass;
        |import com.external.Empty;
        |
        |/** A value entity. */
        |public abstract class AbstractMyService extends ValueEntityBase<EntityOuterClass.MyState> {
        |    
        |    public abstract Effect<Empty> set(EntityOuterClass.MyState currentState, ServiceOuterClass.SetValue command);
        |    
        |    public abstract Effect<ServiceOuterClass.MyState> get(EntityOuterClass.MyState currentState, ServiceOuterClass.GetValue command);
        |}""".stripMargin
    )
  }

  test("ValueEntity generated handler") {
    val service = TestData.simpleEntityService()
    val entity = TestData.valueEntity()
    val packageName = "com.example.service"
    val className = "MyService"

    val sourceDoc =
      ValueEntitySourceGenerator.valueEntityHandler(service, entity, packageName, className)

    val generated = sourceDoc.layout
    val expected =
      """
        |/* This code is managed by Akka Serverless tooling.
        | * It will be re-generated to reflect any changes to your protobuf definitions.
        | * DO NOT EDIT
        | */
        |package com.example.service;
        |
        |import com.akkaserverless.javasdk.impl.AnySupport;
        |import com.akkaserverless.javasdk.impl.EntityExceptions;
        |import com.akkaserverless.javasdk.impl.FailInvoked$;
        |import com.akkaserverless.javasdk.lowlevel.ValueEntityHandler;
        |import com.akkaserverless.javasdk.valueentity.CommandContext;
        |import com.akkaserverless.javasdk.valueentity.ValueEntityBase;
        |import com.example.service.persistence.EntityOuterClass;
        |import com.external.Empty;
        |import scalapb.UnknownFieldSet;
        |
        |/** A value entity handler */
        |public class MyServiceHandler extends ValueEntityHandler {
        |
        |  final MyService entity;
        |  
        |  MyServiceHandler() {
        |    this.entity = new MyService();
        |  }
        |
        |
        |  @Override
        |  public ValueEntityBase.Effect<? extends GeneratedMessageV3> handleCommand(
        |      Any command, Any state, CommandContext<Any> context) throws Throwable {
        |    try {
        |      return invoke(command, state, context);
        |    } catch (Exception e) {
        |      if (e.getClass().isAssignableFrom(FailInvoked$.class)) {
        |        throw e;
        |      } else {
        |        throw e.getCause();
        |      }
        |    }
        |  }
        |}""".stripMargin

    assertEquals(generated, expected)
  }

  test("ValueEntity test source") {
    val service = TestData.simpleEntityService()
    val entity = TestData.valueEntity()

    val packageName = "com.example.service"
    val implClassName = "MyServiceImpl"
    val testClassName = "MyServiceTest"

    val sourceDoc =
      EntityServiceSourceGenerator.testSource(
        service,
        entity,
        packageName,
        implClassName,
        testClassName
      )
    assertEquals(
      sourceDoc.layout,
      """/* This code was initialised by Akka Serverless tooling.
        | * As long as this file exists it will not be re-generated.
        | * You are free to make changes to this file.
        | */
        |
        |package com.example.service;
        |
        |import com.akkaserverless.javasdk.valueentity.CommandContext;
        |import com.example.service.persistence.EntityOuterClass;
        |import com.external.Empty;
        |import org.junit.Test;
        |import org.mockito.*;
        |
        |import static org.junit.Assert.assertThrows;
        |
        |public class MyServiceTest {
        |    private String entityId = "entityId1";
        |    private MyServiceImpl entity;
        |    private CommandContext<EntityOuterClass.MyState> context = Mockito.mock(CommandContext.class);
        |    
        |    @Test
        |    public void setTest() {
        |        entity = new MyServiceImpl(entityId);
        |        
        |        // TODO: write your mock here
        |        // Mockito.when(context.[...]).thenReturn([...]);
        |        
        |        // TODO: set fields in command, and update assertions to verify implementation
        |        // assertEquals([expected],
        |        //    entity.set(ServiceOuterClass.SetValue.newBuilder().build(), context);
        |        // );
        |    }
        |    
        |    @Test
        |    public void getTest() {
        |        entity = new MyServiceImpl(entityId);
        |        
        |        // TODO: write your mock here
        |        // Mockito.when(context.[...]).thenReturn([...]);
        |        
        |        // TODO: set fields in command, and update assertions to verify implementation
        |        // assertEquals([expected],
        |        //    entity.get(ServiceOuterClass.GetValue.newBuilder().build(), context);
        |        // );
        |    }
        |}""".stripMargin
    )
  }

}
