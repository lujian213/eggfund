package io.github.lujian213.eggfund.controller

import spock.lang.Specification
import graphql.execution.ExecutionStepInfo
import graphql.execution.ResultPath
import graphql.language.Field
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLOutputType
import org.springframework.graphql.execution.ErrorType
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class CustomExceptionResolverSpec extends Specification {

    def "ResolveToSingleError"() {
        given:
        def resolver = new CustomExceptionResolver()
        def env = Mock(DataFetchingEnvironment) {
            getExecutionStepInfo() >> {
                ExecutionStepInfo.Builder builder = ExecutionStepInfo.newExecutionStepInfo();
                builder.type = Mock(GraphQLOutputType)
                builder.path = ResultPath.parse("/somepath")
                return builder.build()
            }
            getField() >> new Field("abc")
        }
        when:
        def result = resolver.resolveToSingleError(new ResponseStatusException(HttpStatus.BAD_REQUEST, "some reason"), env)
        then:
        result.errorType == ErrorType.INTERNAL_ERROR
        when:
        result = resolver.resolveToSingleError(new ResponseStatusException(HttpStatus.NOT_FOUND, "some reason"), env)
        then:
        result.errorType == ErrorType.NOT_FOUND
        when:
        result = resolver.resolveToSingleError(new IOException("some reason"), env)
        then:
        result == null
    }

}