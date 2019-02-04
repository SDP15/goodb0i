package com.sdp15.goodb0i.data.scanner

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

data class BarcodeReading(val fvb: List<FirebaseVisionBarcode>)