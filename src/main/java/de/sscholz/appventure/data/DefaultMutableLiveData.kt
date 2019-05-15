package de.sscholz.appventure.data

import androidx.lifecycle.MutableLiveData

class DefaultMutableLiveData<T>(val defaultValue: T) : MutableLiveData<T>() {

    override fun getValue(): T = super.getValue() ?: defaultValue

    init {
        super.setValue(defaultValue)
    }
}