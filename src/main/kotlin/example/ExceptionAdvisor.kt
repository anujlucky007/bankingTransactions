package example

import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import javax.inject.Singleton

@Produces
@Singleton
@Requires(classes = [Exception::class, ExceptionHandler::class])
class OutOfStockExceptionHandler : ExceptionHandler<DuplicateException, HttpResponse<*>> {

    override fun handle(request: HttpRequest<*>, exception: DuplicateException): HttpResponse<*> {
        return HttpResponse.badRequest(Error(exception.errorMessage,exception.errorCode))
    }

}

@Produces
@Singleton
@Requires(classes = [ValidationException::class, ExceptionHandler::class])
class ExceptionHandler : ExceptionHandler<ValidationException, HttpResponse<*>> {

    override fun handle(request: HttpRequest<*>, exception: ValidationException): HttpResponse<*> {
        return HttpResponse.serverError(exception.message)
    }

}

data class  Error(val errorMessage : String,val errorCode:String)

class DuplicateException(val errorMessage : String,val errorCode:String) : RuntimeException()
class ValidationException(val errorMessage : String,val errorCode:String) : RuntimeException()