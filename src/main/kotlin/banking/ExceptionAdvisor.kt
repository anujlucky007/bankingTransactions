package banking

import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import javax.inject.Singleton

@Produces
@Singleton
@Requires(classes = [DuplicateException::class, ExceptionHandler::class])
class OutOfStockExceptionHandler : ExceptionHandler<DuplicateException, HttpResponse<*>> {

    override fun handle(request: HttpRequest<*>, exception: DuplicateException): HttpResponse<*> {
        return HttpResponse.badRequest(Error(exception.errorMessage, exception.errorCode))
    }

}

@Produces
@Singleton
@Requires(classes = [NotExistsException::class, ExceptionHandler::class])
class NotExistsExceptionHandler : ExceptionHandler<NotExistsException, HttpResponse<*>> {

    override fun handle(request: HttpRequest<*>, exception: NotExistsException): HttpResponse<*> {
        return HttpResponse.notFound(Error(exception.errorMessage, exception.errorCode))
    }

}

@Produces
@Singleton
@Requires(classes = [ValidationException::class, ExceptionHandler::class])
class ExceptionHandler : ExceptionHandler<ValidationException, HttpResponse<*>> {

    override fun handle(request: HttpRequest<*>, exception: ValidationException): HttpResponse<*> {
        return HttpResponse.badRequest(Error(exception.errorMessage, exception.errorCode))
    }

}

@Produces
@Singleton
@Requires(classes = [GenericException::class, ExceptionHandler::class])
class GenericExceptionHandler : ExceptionHandler<GenericException, HttpResponse<*>> {

    override fun handle(request: HttpRequest<*>, exception: GenericException): HttpResponse<*> {
        print(exception)
        return HttpResponse.badRequest(Error(exception.errorMessage, exception.errorCode))
    }

}

@Produces
@Singleton
@Requires(classes = [java.lang.Exception::class, ExceptionHandler::class])
class ExceptionNewHandler : ExceptionHandler<java.lang.Exception, HttpResponse<*>> {

    override fun handle(request: HttpRequest<*>, exception: Exception): HttpResponse<*> {
        print(exception)
        return HttpResponse.badRequest("Bad Request")
    }

}

data class  Error(val errorMessage : String,val errorCode:String)

class DuplicateException(val errorMessage : String,val errorCode:String) : RuntimeException()
class NotExistsException(val errorMessage : String,val errorCode:String) : RuntimeException()
class ValidationException(val errorMessage : String,val errorCode:String) : RuntimeException()
class GenericException(val errorMessage : String,val errorCode:String) : Exception(errorMessage)