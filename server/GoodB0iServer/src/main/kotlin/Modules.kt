import org.koin.dsl.module.module
import org.koin.experimental.builder.single
import service.ListService
import service.ProductService
import service.ShelfService

val ServiceModule = module {
    single<ListService>()
    single<ProductService>()
    single<ShelfService>()
}