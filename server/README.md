# Setting up the server

- Clone https://github.com/SDP15/goodb0i.git
- Run git checkout -b [branch_name] origin/[branch_name]  (branch_name) is currently app_socket
- Open /server/GoodB0iServer in IntelliJ
- Click the "Import Gradle project" link in the bottom right
- In the import window 
  - Tick "use auto import"
  - Tick "use default gradle wrapper"
  - For the Gradle JVM option, select whichever version you have installed (Not the autofilled one)

# Server structure

The server is built using [KTOR](https://ktor.io/) and JetBrains [Exposed](https://github.com/JetBrains/Exposed).

KTOR starts from an `Application` class. Requests (HTTP, HTTP/2, or WebSocket) are converted to `ApplicationCall` instances and run through a pipeline owned by the `Application`. 

Most of the notes on KTOR come from [their docs](https://ktor.io/servers/application.html).

The pipeline is made up of interceptors which handle routing, compression, etc. 

## `Features` 

A feature is a singleton that is installed and configured for a pipeline. They are added to an `Application` with the `install` function. 

## Modules

A module is a function receiving the `Application` class which configures the server pipeline by
- Installing features
- Registering routes
- Handling requests, etc

## Features that we install

### DefaultHeaders 
 
 Adds default headers to requests:
 - KTOR package name and version
 - Application package name and version

### CallLogging

Exactly what it says

### WebSockets

Enables support for WebSockets

### ContentNegotiation 

This `Feature` allows the registration of converters based on the `Content-Type` and `Accept` headers. 

We install `GSON` with our own `TypeAdapters` to convert incoming JSON and outgoing models to JSON. 

### Compression 

gzip compression is enabled

### Routing 

The `Routing` feature is used to build a routing tree. 
We install 4 routes, `products`, `shelves`, `lists`, and `sockets` which form the following routing tree: 

```json
/products {
    / 
    /search/{query}
    /{id}
}
/shelves {
    /{id}
}
/lists {
    /new
    /update/{code}
    /load/{code}
}
# Route.sockets installs the sockets at the root of the tree
/trolley
/app
/ping

```

## Our package structure 

The server is split into `controller`, `repository`, and `service` packages. 

### `controller` 

The `controller` package refers to the files which control the routing of requests. 

Note that there aren't any classes here. Routes are defined as extension functions on the `Route` class, meaning that they receive the `Route` type. 

An extension `Route.someRoute(a: A, b: B) {}` will be resolved statically something like `someRoute(this: Route, a: A, b: B)`. 

The files `ListResource`, `ProductResource`, and `ShelfResources` each declare the routes and endpoints listed above, while `SocketResource` declares the websockets. 

The declared `Route` extension functions are installed in `Main` under `install(Routing) { ... }`.

### `repository`

The `repository` package contains all of our database models and entities, as well as some utility code to provider test data. 

We are using JetBrains `Exposed`, which gives us a typesafe DSL for SQL. The implementation is explained later. 

### `service` 

The `service` package contains services which sit between the repository and controllers, and manage loading and editing data, as well as maintaining the state of each of the shopping sessions that is happening. 

#### Database services 

`ListService`, `ProduceService`, and `ShelfService` provide methods for querying or updating the database. 

At some point in the future these should be extracted out to interfaces for testing.

#### Shopping and routing

At this point these components **will** change, so there's not much point explaining them in detail.

## Database structure 

### Tables and models 

`Exposed` defines models as `objects` (singletons) extending the `Table` class (which in turn extends `ColumnSet` and contains thousandsof lines of code that we don't have to care about). 

Our table classes extend either `UUIDTable` or `IntIdTable` which both extend `IdTable`, a class representing a table indexed by some `Compareable` type. 

For example, the `Products` table extends `UUIDTable` 
```kotlin
object Products : UUIDTable() {
    val name = varchar("name", 255)
    val averageSellingUnitWeight = double("averageSellingUnitWeight")
    val contentsMeasureType = varchar("ContentsMeasureType", 20)
    val contentsQuantity = double("contentsQuantity")
    val unitOfSale = integer("UnitOfSale")
    val unitQuantity = varchar("UnitQuantity", 20)
    val department = varchar("department", 50)
    val description = text("description")
    val price = double("price")
    val superDepartment = varchar("superDepartment", 50)
    val unitPrice = double("unitPrice")
}
```
and defines various fields with functions corresponding to SQL data types. These functions are all part of `Table` and call `registerColumn` with a name and a type, which creates a `Column` instance and adds it to the `Tables` internal columns list.

#### Hikari and JDBC 

When we create the database we first create an instance of `HikariDataSource` with a configuration to create an in-memory database with the H2 database engine. We also set a maximum pool size. This is the number of connections that may be opened with the database. (We *really* don't have to care about this)

#### Setting up the database

In `DatabaseFactory` we call `Database.connect` with the `HikariDataSource` instance. 

We then run a `transaction` and create each of the tables that we have defined. 

```kotlin
Database.connect(hikari())
transaction {
    create(Products)
    create(Shelves)
    create(ShelfRacks)
    create(ShoppingLists)
    create(ListContentsTable)
}
```

#### Models 

Alongside each of the table models, we define an entity class representing a single entity in the table. 

For `Products`, the `Product` entity looks like this 

```kotlin
class Product(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Product>(Products)

    var name by Products.name
    var averageSellingUnitWeight by Products.averageSellingUnitWeight
    var contentsMeasureType by Products.contentsMeasureType
    var contentsQuantity by Products.contentsQuantity
    var unitOfSale by Products.unitOfSale
    var unitQuantity by Products.unitQuantity
    var department by Products.department
    var superDepartment by Products.superDepartment
    var description by Products.description
    var price by Products.price
    var unitPrice by Products.unitPrice
}
```

The class is constructed with an `EntityID<UUID>` which is passed through to `UUIDEntity`, which in turn extends `Entity<UUID>` which stores the `EntityID`. 

We define a `companion object` extending `UUIDEntityclass` with the type of `Product`, and a reference to the `Products` table. 

This companion object extending `EntityClass` with a reference to the `Products` table gives us static methods to access and modify the table.

#### Relations 

Alongside `Products`, we have the `ShoppingLists`, `ListEntries`, `ListContentsTable` `ShelfRacks`, and `Shelves` tables.

The `ShoppingLists` table has two fields, code, and time, and a UUID index. 

`ListEntries` stores the entries in lists. It has a reference to a product, a quantity, and a value representing the index of the entry when the user created the list (as the entries may not be ordered when we retreive them).

`ListContentsTable` is used to map lists to their entries. It is a junction table with a refence to a `ShoppingList` and a reference to a `ListEntry`. It is index by both of these values. 

The entity class for `ListEntries` is defined as follows 
```kotlin
class ListEntry(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ListEntry>(ListEntries)
    var index by ListEntries.index // The index of the item, to be preserved
    var product by Product referencedOn ListEntries.product
    var quantity by ListEntries.quantity
}
```

The `by Product referencedOn ListEntries.product` uses the reference define in `ListEntries` to look up the `Product` instance and provide a reference to it. 

Similarly, `ShoppingList` provides a reference to a list of `ListEntry`s using the `via` function to perform an inner join

```kotlin
class ShoppingList(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ShoppingList>(ShoppingLists)

    var code by ShoppingLists.code
    var time by ShoppingLists.time
    var products by ListEntry via ListContentsTable

}
```

Aside from the structures to store products and lists, we also have `ShelfRacks` and `Shelves`. 

A `ShelfRack` has an id, capacity, and an info string. 
A `Shelf` has a `Product`, `quantity`, `position`, and a reference to a `ShelfRack`. 

A `ShelfRack` references the shelves it contains with `referrersOn`.

## Path finding

### Graph

A graph is a generic class with a type used to index nodes. It is a collection of `Vertex` instances, which each have a `Node` (containing and ID) and a list of outgoing edges. (I don't like using `Node` and `Vertex` together. Nodes themselves should probably be the generic part, with some function to provide a `Comparable` id).

`Graph` defines builder functions which let us construct a graph nicely like this: 

```kotlin
        val graph = graph<String> {
            val cost = 5
            ("a" to "b" cost 1 to "c" cost 2 to "d" cost 5) // a-1->b-2->c-5>d
            "TEST" toFrom listOf("TEST2" cost 5, "TEST3" cost 6) // TEST<-5->TEST2 TEST<-6->TEST3 
            "ENTRANCE" to "FRUITS" cost cost
            "FRUITS" to "VEGETABLES" cost cost
            "VEGETABLES" toFrom "VEG_CORNER" cost 2 * cost
            "VEG_CORNER" toFrom "DAIRY_CORNER" cost 2 * cost
            "DAIRY_CORNER" to "DAIRY" cost cost
            "DAIRY" to listOf("BAKERY" cost cost, "SEAFOOD" cost 2 * cost)
            "BAKERY" to listOf("MEAT", "BOTTOM_CORNER") costs listOf(cost, 2 * cost)
            "BOTTOM_CORNER" to "TILLS" cost 2 * cost
            "BOTTOM_CORNER" to "MEAT" cost cost
            "MEAT" to "SEAFOOD" cost cost
            "MEAT" to "BAKERY" cost 2 * cost
            "SEAFOOD" to "SEAFOOD_CORNER" cost cost
            "SEAFOOD" to "DAIRY" cost 2 * cost
            "SEAFOOD_CORNER" to "FOOD_CUPBOARD_CORNER" cost cost
            "FOOD_CUPBOARD_CORNER" to "FOOD_CUPBOARD" cost cost
            "FOOD_CUPBOARD" to "SWEETS" cost cost
            "SWEETS" to "TILLS" cost cost

        }
```

`to` defines an edge from LHS to RHS, while `toFrom` is bidirectional. The RHS can take a list of nodes followed by a single cost, or a list of nodes with individual costs.

### Dijkstra's

The algorithm is as follows

```python
def find_path(start, waypoints, end, graph):
    remaining = waypoints
    current = start
    path = []
    while(waypoints.isNotEmpty()):
       # Distances from current to all other points in graph
       distances = dijkstras(from=current, graph)
       closest = remaining.minBy(distances)
       waypoints.remove(closest)
       # Add the subpath using the distances from dijkstra's
       path.add(current to closet)
       remaining.remove(current)
       current = closest
       # Final leg of the route
       if(remaining.isEmpty() and current != end):
         remaining.add(end)
    return path
```

As Dijkstra's computes the shortest path from a node to **all** other nodes, `RouteFinder` will eventually be rewritten to compute all distances at startup.