package com.dzichkovskii.mqttsrm.interfaces

interface UIUpdaterInterface{
    fun update(message: String, topic: String)
}