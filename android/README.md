# Setting up the Android App

- Clone https://github.com/SDP15/goodb0i.git
- Run git checkout -b [branch_name] origin/[branch_name]  (branch_name) is currently app_socket
- Open /android/App in Android Studio. It should import and build by itself

# Application structure

## Root packages

The app is separated into data and view packages. 

### Data

`data` is responsible for loading storing and processing data. 
It functions as separately from Android as possible (there should be few/none `import android.*` lines). NB. `androidx` is separate from Android, and can be tested outside of the emulator.

### View

`view` contains the views and ViewModels, as well as any adapters and view related utilities. 

## Dependency injection 

Our app uses [Koin](https://github.com/InsertKoinIO/koin) for dependency injection.
- A dependency is a required object for some dependent. 
- An example in our app is `ProductLoader` 

```kotlin
interface ProductLoader {

    suspend fun loadProduct(id: String): Result<Product>
t
    suspend fun loadCategory(category: String): Result<List<Product>>

    suspend fun loadProductsForShelfRack(shelfId: Int): Result<List<Product>>

    suspend fun search(query: String): Result<List<Product>>

    suspend fun loadAll(): Result<List<Product>>

}
```

- This interface defines the methods we need for loading various subsets of products
- The dependent which requires a `ProductLoader` does not need to know how the `ProductLoader` works, only that it implements the interface
- The two components (dependent and dependency) are decoupled such that we could re-implement the `ProductLoader` without having to change the dependent(s)
- In our case we have the `RetrofitProductLoader`, which uses the `Retrofit` HTTP client to load products from the server. We also have the `TestDataProductLoader` which loads hardcoded data. 
- The dependent doesn't need to know whether it is loading test data or actual data from the server (In fact, `TestDataProductLoader.DelegateProductLoader` lets us swap out the data source while the app is running)

**How does injection work?**
- The possible dependencies are defined in `App`
- Each of the lines `single<type> { }` define a dependency of a particular type, and a function which creates an instance of this type
- When a dependent requests a dependency it does so through Kotlin property delegation, e.g. `private val someDepedencyOfType: type by inject()`
- This means that the property is not instantiated until we access it
- When it is accessed the Koin `inject` function looks through the available dependencies and finds the `single` we defined earlier
- `single` refers to the dependency being a singleton. If we haven't access it before, Koin creates it with the creation function we defined earlier, otherwise, it returns the previously created instance 
- If we want to create a new instance for each injection site,  we can write `factory<type> { }` instead of `single`
- We can also write 'get()' to immediately resolve the dependency (this might be useful if the dependency has set-up work to do, which can then happen before we access it)  

**Why is this useful?**
- Moves 'biolerplate' code out of the actual application logic (no huge 'onCreate' methods setting up all the dependencies)
- Decreases coupling between classes and their dependencies. Classes shouldn't depend on particular implementations of their dependencies
- Externalisation of configuration
  - The particular implementations are not managed by the dependents, making them simpler to change from a single point 
  - This makes the dependents much easier to test using stubs or mock objects 

**How to add a new dependency?**
- Define an interface for the dependency you are creating
- Provide an implementation of this interface
- Define a `single` or `factory` component for the dependency interface which instantiates the implementation. `single<interface> { Implementation() }`
- If the implementation has dependencies itself, these can be resolved at the constructor.
  - Suppose we have `class InterfaceAImplementation(val dependency1: Interface1, val context: Context) : InterfaceA`,
  - and that we already have a component method for `Interface1` (`single<Interface1>1 { Interface1Implementation() }`) 
  - We can inject the dependencies as follows: `single<InterfaceA> { InterfaceAImplementation(get(), androidApplication())  }`
  - Koin will resolve the 'Interface1' dependency as it would any other, and the `Context` dependency through the application level context that was used to start Koin

**More information**
 The [Wikipedia article on depenceny inject](https://en.wikipedia.org/wiki/Dependency_injection), and [Koin reference documentation](https://insert-koin.io/docs/1.0/documentation/reference/index.html) are quite good.

## Fragments, ViewModels, and LiveData 

`ViewModels` are Android's current first-party solution to maintaining UI state 'in a lifecycle conscious way'. That is to say that ViewModels are scoped to the creation and destruction of a `LifeCycle`.

`androidx.fragment.app.Fragment` and `androidx.fragment.app.FragmentActivity` implement the `LifecycleOwner` interface to provide a `LifeCycle` instance which allows listening for lifecycle events (`onCreate`, `onDestroy`, etc)

Lifecycle aware components such as `LiveData` can then dispatch data only when the lifecycle component is actually able to receive it.

The `ViewModel` for each `Fragment` exists between the `onCreate` and `onDestroy` of the parent `Fragment` (or those of an `Activity` if `ViewModel` is scoped to the `Activity`.
The [Fragment source](https://android.googlesource.com/platform/frameworks/support/+/20d92619d2d4cb50eaf48fe62da1dc691cdfd09b/fragment/src/main/java/androidx/fragment/app/Fragment.java#1709) shows that the `ViewModel` is
cleared if the `Fragment` is being destroyed for a reason other than a configuration change. NB. The implementation in the Android source has changed since changed on the dev branch, but the behaviour is the same.

![Activity ViewModel Lifecycle](https://developer.android.com/images/topic/libraries/architecture/viewmodel-lifecycle.png)

**Our use of ViewModels**

All of our `ViewModel` classes extend our `BaseViewModel` which has an abstract `bind` function. It is the responsibility of our `Fragments` to call this method as soon as possible, notifying the `ViewModel` that the `Fragment` is visible to the user.
The `ViewModels` expose `LiveData` to the `Fragment` and have methods corresponding to possible user input. 

A simple example is `SavedListsViewModel` and `SavedListsFragment`. 

```kotlin
class SavedListsViewModel : BaseViewModel<Any>() {

    private val listStore: ShoppingListStore by inject()

    val lists: LiveData<List<ShoppingList>> = listStore.loadLists()

    override fun bind() {

    }

    fun open(list: ShoppingList) {
        transitions.postValue(SavedListsFragmentDirections.actionViewShoppingListToListConfirmationFragment(list))
    }

}
```

As shown above, the `SavedListsViewModel` requests a `ShoppingListStore` via `Koin` and delegates the `LiveData<List<ShoppingList>>' to whichever implementation of `ShoppingListStore` Koin provides it.

The `SavedListsFragment` observes this data and posts it to an adapter. 

```kotlin
    override fun onResume() {
        super.onResume()
        vm.bind()
        val adapter = SavedListsAdapter(vm::open)
        saved_lists_viewswitcher.switchOnEmpty(adapter)
        lists_recycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        lists_recycler.adapter = adapter
        vm.lists.observe(this, Observer {
            adapter.setItems(it)
        })
        vm.transitions.observe(this, Observer {
            findNavController().navigate(it)
        })
    }
``` 

The method reference `vm::open` can be seen being passed to the adapter. This method is called from the adapter whenever a user clicks on an item in the list.

### Use of Koin for injection of ViewModels

Koin can inject Android `ViewModels` in the same way that it injects other dependencies. This is done through `viewModel<type> { }` and `by viewModel()` rather than `inject`.

For the most part, our `ViewModels` are scoped to the `Fragment` that they work with. The only exception is `ListViewModel` which exists at the scope of `ListPagingFragment`, and is used in its child fragments (SearchFragment and ShoppingListFragment). 

This is very simple to do with Koin. The `sharedViewModel` function injects a `ViewModel` across multiple dependents. The key to sharing `ListViewModel` is the `from` parameter for `sharedViewModel()`. 
This parameter is a `ViewModelStoreOwnerDefinition`, which is a typealias for a function that returns a `ViewModelStoreOwner`. The default implementation uses the parent `Activity, but we can swap this out for the parent `Fragment`, thereby scoping the `ViewModel` to the parent `Fragment`. 


### Navigation component and navigating between `Fragments` 

The application has a single `Activity` which loads a root layout and manages back presses. 

The `activity_main` layout file contains a fragment with `android:name="androidx.navigation.fragment.NavHostFragment"` and `app:navGraph="@navigation/nav_graph"`.
This `Fragment` loads the navigation graph and opens the start component from the graph (WelcomeFragment). 

The actions in the `nav_graph` XML define transitions between the different fragments.
An action has an id and a destination:
```XML
        <action
            android:id="@+id/action_list_creation_fragment_to_list_confirmation_fragment"
            app:destination="@id/list_confirmation_fragment" />
```
`Fragments` can also require arguments. 
```XML
        <argument
            android:name="shopping_list"
            android:defaultValue="@null"
            app:argType="com.sdp15.goodb0i.data.store.lists.ShoppingList"
            app:nullable="true" />
```
In this case we require a `ShoppingList` instance (which implements 'Parcelable' for sending via a 'Bundle'). 
The `ShoppingList` is nullable (and is null by default), as the `list_creation_fragment` that this argument belongs to may be opened to create a new list, or edit an existing one. 

#### Using the actions
- After writing the XML (or using the design tool) to add an action, build the project (CTRL+F9) and the Androidx navigation plugin will build the required classes. 
- For each `Fragment` with outgoing actions, a class `[FragmentName]Directions` will be build with methods for each action
- These methods take any required arguments, and return a `NavDirections` instance (a class with an action id and argument `Bundle`)
- This `NavDirections` should then be posted to the `transitions` `LiveData` defined in `BaseViewModel` 
- The `Fragment` observes `transitions` and calls `findNavController().navigate` with the `NavDirections` instance 

For example, in `ListViewModel` we want to confirm a `ShoppingList` after creating it:
```kotlin
                transitions.postValue(
                    ListPagingFragmentDirections.actionListCreationFragmentToListConfirmationFragment(
                        created
                    )
                )
```
and in `ListPagingFragment` we observe `transitions` 
```kotlin
        vm.transitions.observe(this, Observer {
            if (baseActivity.fragmentHistory.first == R.id.list_confirmation_fragment && baseActivity.fragmentHistory[1] != R.id.list_creation_fragment) {
                findNavController().navigateUp()
            } else {
                findNavController().navigate(it)
            }
        })
```

In this example there is a case where we want to navigate back to an existing instance of a `Fragment`, rather than creating a new one.

**Retreiving arguments**

Also generated are `[FragmentName]Args` classes which manage reading the registered arguments to and from a `Bundle`. 

The extension function `navArgs<T>` returns a `NavArgsLazy` instance used to access the `Args` instance. 

```kotlin
val args = navArgs<ListPagingFragmentArgs>()
val list = args.value.shoppingList
```



## Existing data and view classes and their uses (as of 2019/02/27)

### Base classes and utilities

- `BaseFragment` Defines `baseActivity` which is `getActivity()` cast to `MainActivity`, and `onBackPressed` which is used by `MainActivity` to allow `Fragments` to override back presses
- `BaseViewModel` defines `bind`, the `transitions` `LiveData` (as `SingleLiveData`), and `actions` (another `SingleLiveData`)

- `SingleLiveData` extends `CountedLiveData` and removes the data values after it has been observed once. This is so that a `NavDirections` instance isn't re-observed when we return to a `Fragment`
- `ListDiff` represents a change in a generic `List` 
  - It is a sealed class which stores the current state of the `List` as well as the change that was made
  - `All` represents all contents being changed
  - `Add` represents a single item being added
  - `Remove` represents a single item being removed
  - `Update` represents a single item being changed
  - `Move` represents an item moving from one index to another
  - `ListDiff<T>` implements `List<T>` by delegating to the `items` `List<T>`, so it can be used as a `List<T>` itself
- `Utils` contains some utility extension functions:
  - `EditText.watchText` takes a function which takes a `String` and invokes it whenever a `TextWatcher` fires `onTextChanged`. This is cleaner than the `TextWatcher` boilerplate
  - `ViewSwitcher.switchOnEmpty` takes a `RecyclerViewAdapter` and switches the `ViewSwitcher` view whenever the `RecyclerView` changes from being empty to having some contents (and v.v)
  - `MutableLive<T>.move` moves an element from an index to another index, and returns whether or not the element was actually moved (to != from)


#### `App`
- Starts `Timber` with a `CapturingDebugTree` 
  -  `Timber` is [a logging framework](https://github.com/JakeWharton/timber)
  -  `CapturingDebugTree` saves the log messages to a `StringBuilder` and can display them in a simple dialog
-  Initialises `Firebase` for the `MLKit` barcode scanner
-  Starts `Koin` with a `Logger` that redirects messages to `Timber` so that they can be shown in the in-app log
-  Defines the injectable dependencies and `ViewModels`

#### `MainActivity`

- Instantiates the root `Fragment`
- Hides the `ActionBar`
- Stores a history of the ids of each `Fragment`
  - This is a `CircularIntArray` in which the first element is always the *previous* `Fragment`
- Hides the keyboard when `Fragments` are switched
- Delegates back presses to the currently visible `Fragment` 
- Listens for the volume down key in order to show the debug menu



### `WelcomeFragment` and `WelcomeViewModel`

Nothing to see here. Listens for clicks on 3 buttons and opens the corresponding `Fragment`

### `CodeFragment` and `CodeViewModel` 

- Listens for input on the code input
- Checks whether it is valid (7 digits)
- If so, it launches a coroutine toattempt to load the list
  - If this is successful, it navigates to `ListConfirmationFragment`
  - Otherwise: There's currently no error handling, it just ignores the result

### `SavedListsFragment`, `SavedListsViewModel`, and `SavedListsAdapter` 

- `SavedListsFragment` creates the adapter and listens for items posted from the `ViewModel`
- `SavedListsViewModel` delegates the `LiveData<List<ShoppingList>>` to an implementation of `ShoppingListStore`
- `SavedListsAdapter` 
  - Binds list data to viewholders
  - Handles deleting lists from device via `ShoppingListStore`
  - Handles passing clicks to `SavedListsViewModel`

### `ListPagingFragment`, `SearchFragment`, `ShoppingListFragment`, `ListViewModel`, and `ProductAdapter` 

#### `ListPagingFragment`
- Hosts the `ViewPager` holding `SearchFragment` and `ShoppingListFragment`
- Instantiates the `ListViewModel`
- Handles clicks on the search bar menu
- Handles closing the keyboard and changing the menu icon when the `ViewHolder` changes position
- Handles navigation and back presses

#### `ProductAdapter` 
- Displays either `Products` in a shopping list, or in search results
- Handles incrementing and decrementing quantities of items on button press
- Handles rearranging items in `ShoppingListFragment`

#### `ListViewModel`
- Manages loading of `Products` for a query
  - This is done by a `Job` which is cancelled if a new query arrives before it is finished
- Manages saving or updating `ShoppingLists` on the server
- Manages dispatching changes in item quantity to both child `Fragments`

#### `SearchFragment`
- Creates the adapter for search results and observes results to pass to it
- Uses `switchOnEmpty` to display an empty result

#### `ShoppingListFragment`
- Creates the adapter for the entire list
- Adds an `ItemTouchHelper` to notify the `ViewModel` when items are dragged and dropped to new positions
- Dispatches save button presses to the `ViewModel`

### `ListConfirmationFragment` and `ListConfirmationViewModel`

#### `ListConfirmationFragment`
- Passes the `ShoppingList` to the `ViewModel`
- Manages navigation to other fragments

#### `ListConfirmationViewModel`
- Posts information about the shopping list to the `Fragment`

### `ShopConnectionFragment` and `ShopConnectionViewModel`

- Initiates the connection to the server through `ShoppingSessionManager`
- Listens for `ShoppingSessionState` updates and navigates to `NavigatingToFragment` if the connection and trolley negotiation is successful

### `NavigatingToFragment` and `NavigatingToViewModel`
- Listens for updates to the `ShoppingSessionState` and will later display updates to the current location
- Navigates to the `ItemConfirmationFragment` once the next shelf rack is reached

### `ProductFragment` and `ProductViewModel`
- Display the product that has been reached
- Navigate to the `ScannerFragment`

### `ScannerFragment` and `ScannerViewModel`

#### `ScannerFragment`
- Set up the camera view with the `Fragment` lifecycle and add a `FrameProcessor` to pass frames to the `ViewModel`

#### `ScannerViewModel`
- Receives frames and if no current processing is running, sends the frame to a `BarcodeReader` implementation which attempts to find barcodes`
- Posts the read value to the `ShoppingSessionManager` to check if the scanned barcode exists

### `ItemConfirmationFragment` and `ItemConfirmationViewModel`
- Will allow confirmation of a product
- Will also listen for messages that the product has been confirmed via the Trolley


### `BarcodeReader` and `MLKitScanner`
- Defines an interface for reading, a reading format, and a callback for finding or not finding a barcode
- `MLKitScanner` implements this using the `FirebaseVision` barcode detector

### Models

- `Product` is a product in the shop
- `ListItem` is a product with a quantity
- `ShoppingLis` is a list of `ListItems` with a code and timestamp
- The `Parcelize` and `Entity` annotations on the models are for `Bundle` packaging and `Room` storage respectively. `@SerializedName` provides names for `GSON` to deserialize the classes from JSON 


### `ListManager`, `ProductLoader` and their `Retrofit` implementations

These interfaces are both fairly self explanatory. The `Loader` loads things, and the `Manager` creates, loads, and updates things. 

The `Retrofit` HTTP client defines the endpoints we are using with annotated methods in an interface. 

```kotlin
interface KTORProductAPI {

    @GET("/products/{id}")
    fun getProductAsync(@Path("id") id: String): Deferred<Response<Product>>

    @GET("/products")
    fun getAllAsync(): Deferred<Response<List<Product>>>

    @GET("/products/search/{query}")
    fun searchAsync(@Path("query") query: String): Deferred<Response<List<Product>>>

    @GET("/shelves/{id}")
    fun getProductsForShelfRackAsync(@Path("id") id: Int): Deferred<Response<List<Product>>>

}
```


The first method above is a call which takes a path parameter id, and returns a `Deferred<Response<Product>>`. 

- `Deferred` is a basic implementation of a `Future`, that is, a value which has not yet been computed
- `Reponse<T>` is a `Retrofit` class representing a HTTP response 

The `RetrofitProductLoader` uses these to implement the `ProductLoader` interface. It does this through the `awaitCatching` extension function defined in `Extensions.kt` 

```kotlin
suspend fun <T, U> Deferred<T>.awaitCatching(success: (T) -> U, failure: (Throwable) -> U): U {
    return try {
        success(await())
    } catch (t: Throwable) {
        failure(t)
    }
}
```

`await` awaits the completion of the `Deferred` value. This can throw a `CancellationException`, or other exceptions. 
`awaitCatching` takes two parameters, a `success` function taking the type of the `Deferred` and returning some other type `U`, and a `failure` function which takes a `Throwable` and also returns type `U`. 

In our use, this lets us define a clean immplementation of `ProductLoader` with methods such as 

```kotlin
    override suspend fun loadProduct(id: String): Result<Product> = api.getProductAsync(id).awaitCatching(
        success = { it.toResult() },
        failure = { Result.Failure(Exception(it.message)) }
    )
```

This makes use of `Result<T>` and another extension function, `Response<T>.toResult` . 

A `Result` is a generic sealed class which is either an instance of `Result.Success<T>` or `Result.Failure<T>`, the first of which contains an instance of `T`, and the second of which contains the `Exception` that caused the failure. 

`toResult` is a simple converter from a `Retrofit` `Response` to a `Response` 

```kotlin
fun <T : Any> Response<T>.toResult(log: Boolean = BuildConfig.DEBUG): Result<T> {
    val body = body()
    return if (isSuccessful && body != null) {
        if (log) Timber.i("Successful response. Body: $body")
        Result.Success(body)
    } else {
        if (log) Timber.e("Failed response. Errorbody ${errorBody()}")
        Result.Failure(APIError(this))
    }
}
```

### `ShoppingListStore`, `RoomDB`, `ListDAO`, `RoomShoppingListStore` and `ListItemTypeConverter` 

`ShoppingListStore` is an interface for storing lists on the device, with `storeList`, `loadLists`, and `deleteList` functions. 

`ListDAO` is a `Room ` `Dao` (Data access object) interface defining the queries we want to make on the `ShoppingList` table (recall that `ShoppingList` is annotated with `@Entity`). 
- `@Query(SELECT * FROM ShoppingList)` is a simple query to load all lists
- `@Insert(onConflict = OnConflictStrategy.REPLACE)` is an insert method that will replace with the newer value on conflict, meaning that we can also use this method for update
- `@Delete` is self explanatory


`ListItemTypeConverter` converts `ListItems` to strings, as `SQL`, and therefore `Room` doesn't work well with dumping deeply nested objects without relations (what we are doing instead of setting up proper relations). It uses `GSON` to convert to and from JSON strings, as we have already defined the mapping with `@SerializedName` annotations.


`RoomDB` defines a `Room` database with only the `ShoppingList` entity, and defines an abstract method to access a `ListDAO` from the `RoomDB`.

`RoomShoppingListStore` takes the `RoomDB` instance and uses its methods to implement `ShoppingListStore`.


## Connection to server

These classes are in `data/navigation` but they are important/complex enough to warrant another section. 


### `Message`

This is a nested `sealed` class which defines the types and structure of message to and from the server. 

N.B A `sealed` class is just an `abstract` class, the `sealed` type hierarchy is just a feature of the compiler, which can do things like recognise a `when` (`switch`) expression as being exhaustive when it has branches for all possible subtypes

The classes are mostly documented with comments, and their names should be pretty self explanatory. 

They are converted to and from strings in `Transformer`.
Each message is represented with a two character code, followed by a delimiter (&), and then any message body. 
It's been a bit of a pain keeping these in sync between the different code bases, so I will probably define them somewhere else at some point. 

### `Route` 

A `RoutePoint` represents one of the points in the route around the sop
- `Start` is a constant fixed start point. We always start here regardless of the route
- `End` is similar for the end. This is probably the tills
- `Pass` is marker point that we expect to pass through 
- `TurnLeft`, `TurnRight`, and `TurnCenter` are parts of the route which represent the direction we turn at the next junction
- `Stop` is a shelf that we stop at

The expected process is that when the trolley reaches a marker, it searches for the next stop point after the last position it registered. This means that it can miss a `Pass` point and then ignore it later when it reaches a shelf. 

The `Route` is just a wrapper around a `List` of `RoutePoints` which is built from a string. 


The route string is a comma separated list with each element being one of `start`, `end`, `left`, `right`, `center`, `stop%id`, or `pass%id`, where `%` is used as a delimeter, and `id` is the id of the point to pass through or stop at. 

### `ShoppingSessionManager` 

This interface defines the state and functions of the session manager. This is a class which exists across the scope of all the `Fragments` in `view/navigation`. 

It provides `LiveData` for the current product, most recently scanned product, and `ShoppingSessionState`. 

(The incoming messages are also accessible, but there probably isn't a use for this. The `ViewModels` shouldn't really be accessing this as that's the whole point of the `ShoppingSessionManager`)

### `SocketHandler`

This is a generic class with two type parameters `IN` and `OUT`. These are the types of the messages coming in and out, and are converted to and from strings with a `SocketMessageTransformer`. (This is implemented by `Message.Transformer`). 

The `SocketHandler` manages opening a `WebSocket` with `OKHTTP`, sending message, emitting received messages as the `OUT` type, and providing a `LiveData` for the socket state. 


### `SessionManager` 

This is the implementation of `ShoppingSessionManager`. It's not finished. 

It tracks
- The app id provided by the server
- The route
- The index within the route
- The `ShoppingList` that we are collecting products for
- The scanned product
- The current state of the session 
- The state of the `WebSocket` through a `SocketHandler`

It manages all incoming and outgoing messages as well as actions coming from the `Fragments`. 

Currently it's ~200 lines, which is fairly acceptable. But it could quite easily become overly complicated, which would make it a pain to work with.


## Testing

There is currently only a proper test for `ListViewModel`. 

This tests all of the of the functionality of the `ViewModel`. 

Some of the `ViewModels` are so simple that they aren't worth testing (There are a couple without any conditionals to test), however the rest still need testing. 

## Integrated testing 

Every time a commit is made to a branch containing the `.travis.yml` file, an automated build of the Android app will be run, and the unit tests will be run. The green tick/red cross showing up beside commits is whether the commit built successfully. 

You can click on these to see the build log.

## 