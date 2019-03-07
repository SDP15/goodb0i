# App <-> Server <-> Trolley message format

All messages are single WebSocket text frames
All messages through sockets follow the format [CamelCaseCode][Delimeter][Message body].
Most messages don't have a body, but they **must** still have the delimeter.
The delimeter is currently '&'.

## Messages from App to Server
 
- `Reconnect` Sends ID string of previous session to server in order to restore a session after connection loss. Body is the ID string
- `PlanRoute` Requests that the server plan a route for a shopping list. Body is the code of the shopping list
- `ProductScanned` The app has scanned a product. Body is the ID of the scanned product. **The server currently ignores the ID**
- `ProductAccepted` The previously scanned product has been accepted from the app. No body
- `ProductRejected` The previously scanned product has been rejected from the app. No body
- `RequestHelp` The user has requested staff assistance from the app. No body **Currently does nothing**
- `Stop` The app has requested that the trolley stop. No body. **There may not be a use for this**
- `RouteReceived` Confirmation message that the app has received and parsed a route sent from the server. No body

## Messages from Server to App

- `ID` Assigning a unique ID string to the app shopping session. Body is the ID string. This message is sent directly in `AppManager::joinApp`, not from `Session`.
- `TrolleyAssigned` A trolley has been assigned to the shopping session. No body
- `UserReady` The trolley voice system has confirmed that the user is ready to start moving. No body. This is the prompt for the app to start navigation
- `RouteCalculated` A route has been calculated. Body is the route string. This message is also sent to the trolley
- `ReachedPoint` The trolley has reached a point in the store. Body is the point ID.
- `TrolleyAcceptedProduct` The trolley has accepted the current product. No body
- `TrolleyRejectedProduct` The trolley has rejected the current product. No body 

## Messages from Server to Trolley

- `AppAcceptedProduct` The app has accepted the current product. No body **This is forwarded to the trolley**
- `AppRejectedProduct` The app has rejected the current product. No body **This is forwarded to the trolley**
- `AppScannedProduct` The app has scanned a product. No body. **This should contain the ID of the scanned product, but currently doesn't** **This is forwarded to the trolley**
- `Assigned` The trolley has been assigned to a user session
- `RouteCalculated` The server has calculated a route for a list. Body is the route string

## Messages from Trolley to Server

- `ReachedPoint` The trolley has reached a point in the store. Body is the ID of the point **This is forwarded to the app**
- `AcceptedProduct` The trolley has accepted the current product. No body **This is forwarded to the app**
- `RejectedProduct` The trolley has rejected the current product. No body **This is forwarded to the app**
- `UserReady` The user is ready to depart. No body **This is forwarded to the app**
- `ReceivedRoute` The route string has been received and parsed. No body

