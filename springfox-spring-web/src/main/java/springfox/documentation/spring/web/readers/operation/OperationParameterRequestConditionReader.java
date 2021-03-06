/*
 *
 *  Copyright 2015 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package springfox.documentation.spring.web.readers.operation;

import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.condition.NameValueExpression;
import org.springframework.web.servlet.mvc.condition.ParamsRequestCondition;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.AllowableListValues;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;

import java.util.List;

import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;

@Component
public class OperationParameterRequestConditionReader implements OperationBuilderPlugin {

  private final TypeResolver resolver;

  @Autowired
  public OperationParameterRequestConditionReader(TypeResolver resolver) {
    this.resolver = resolver;
  }

  @Override
  public void apply(OperationContext context) {
    ParamsRequestCondition paramsCondition = context.getRequestMappingInfo().getParamsCondition();
    List<Parameter> parameters = newArrayList();
    for (NameValueExpression<String> expression : paramsCondition.getExpressions()) {
      if (skipParameter(parameters, expression)) {
        continue;
      }
      
      Parameter parameter = new ParameterBuilder()
              .name(expression.getName())
              .description(null)
              .defaultValue(expression.getValue())
              .required(true)
              .allowMultiple(false)
              .type(resolver.resolve(String.class))
              .modelRef(new ModelRef("string"))
              .allowableValues(new AllowableListValues(newArrayList(expression.getValue()), "string"))
              .parameterType("query")
              .parameterAccess("")
              .build();
      parameters.add(parameter);
    }
    context.operationBuilder().parameters(parameters);
  }

  private boolean skipParameter(List<Parameter> parameters, NameValueExpression<String> expression) {
    return expression.isNegated() || parameterHandled(parameters, expression);
  }

  private boolean parameterHandled(List<Parameter> parameters, NameValueExpression<String> expression) {
    return any(parameters, withName(expression.getName()));
  }

  @Override
  public boolean supports(DocumentationType delimiter) {
    return true;
  }

  private Predicate<? super Parameter> withName(final String name) {
    return new Predicate<Parameter>() {
      @Override
      public boolean apply(Parameter input) {
        return Objects.equal(input.getName(), name);
      }
    };
  }
}
