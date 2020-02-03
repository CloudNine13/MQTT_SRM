/**
 * @author Igor Dzichkovskii
 */
package com.dzichkovskii.mqttsrm.interfaces

/**
 * This interface is to update EditView with incoming messages
 * @see com.dzichkovskii.mqttsrm.fragments.SubscribeFragment.update
 */
interface UIUpdaterInterface{
    fun update(message: String, topic: String)
}