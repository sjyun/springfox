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

package springfox.documentation.spring.web.readers
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import springfox.documentation.service.Operation
import springfox.documentation.builders.OperationBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.spi.service.contexts.AuthorizationContext
import springfox.documentation.spi.service.contexts.RequestMappingContext
import springfox.documentation.spring.web.mixins.AuthSupport
import springfox.documentation.spring.web.mixins.RequestMappingSupport
import springfox.documentation.spring.web.mixins.ServicePluginsSupport
import springfox.documentation.spring.web.plugins.DocumentationContextSpec
import springfox.documentation.spring.web.plugins.DocumentationPluginsManager
import springfox.documentation.spring.web.readers.operation.ApiOperationReader
import springfox.documentation.spring.web.readers.operation.DefaultOperationBuilder

import static org.springframework.web.bind.annotation.RequestMethod.*

@Mixin([RequestMappingSupport, AuthSupport, ServicePluginsSupport])
class ApiOperationReaderSpec extends DocumentationContextSpec {
  ApiOperationReader sut

  def setup() {
    AuthorizationContext authorizationContext = AuthorizationContext.builder()
            .withAuthorizations(defaultAuth())
            .forPaths(PathSelectors.regex(".*"))
            .build()
    plugin.authorizationContext(authorizationContext)
    sut = new ApiOperationReader(customWebPlugins([],[],[new DefaultOperationBuilder()]))
  }

  def "Should generate default operation on handler method without swagger annotations"() {

    given:
      RequestMappingInfo requestMappingInfo = requestMappingInfo("/doesNotMatterForThisTest",
              [
                      patternsRequestCondition      : patternsRequestCondition('/doesNotMatterForThisTest', '/somePath/{businessId:\\d+}'),
                      requestMethodsRequestCondition: requestMethodsRequestCondition(PATCH, POST)
              ]
      )

      HandlerMethod handlerMethod = dummyHandlerMethod()

      RequestMappingContext context = new RequestMappingContext(context(),
              requestMappingInfo,
              handlerMethod)
    when:
      def operations = sut.read(context)

    then:
      Operation apiOperation = operations[0]
      apiOperation.getMethod() == PATCH.toString()
      apiOperation.getSummary() == handlerMethod.method.name
      apiOperation.getNotes() == handlerMethod.method.name
      apiOperation.getNickname() == handlerMethod.method.name
      apiOperation.getPosition() == 0
      apiOperation.getAuthorizations().size() == 0

      def secondApiOperation = operations[1]
      secondApiOperation.position == 1
  }


  def "Should ignore operations that are marked as hidden"() {

    given:
      RequestMappingInfo requestMappingInfo = requestMappingInfo("/doesNotMatterForThisTest",
              [
                      patternsRequestCondition      : patternsRequestCondition('/doesNotMatterForThisTest', '/somePath/{businessId:\\d+}'),
                      requestMethodsRequestCondition: requestMethodsRequestCondition(PATCH, POST)
              ]
      )

      HandlerMethod handlerMethod = dummyHandlerMethod("methodThatIsHidden")
      RequestMappingContext context = new RequestMappingContext(context(), requestMappingInfo, handlerMethod)

    when:
      def mock = Mock(DocumentationPluginsManager)
      mock.operation(_) >> new OperationBuilder().hidden(true).build()
      def operations = new ApiOperationReader(mock).read(context)

    then:
      0 == operations.size()
  }
}
