package com.flagship.sdk.core.contracts

interface IStore<T> {
    suspend fun current(namespace: String = "default"): T?

    suspend fun replace(value: T): Long
}
