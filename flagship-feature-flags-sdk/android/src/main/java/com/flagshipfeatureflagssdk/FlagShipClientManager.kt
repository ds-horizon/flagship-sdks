package com.flagshipfeatureflagssdk

import dev.openfeature.kotlin.sdk.Client

const val KEY_NULL = "KEY_IS_NULL"
const val CLIENT_NULL = "CLIENT_IS_NULL"

object FlagShipClientManager {
  var openFeatureClient: Client? = null
}
