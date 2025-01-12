package com.exner.tools.activitytimercompanion.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exner.tools.activitytimercompanion.data.AllDataHolder
import com.exner.tools.activitytimercompanion.state.TVConnectionStateHolder
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.text.Charsets.UTF_8

enum class ProcessStateConstants {
    IDLE,
    DATA_RECEIVED,
    DONE,
    CANCELLED,
    ERROR
}

/****
 * Here's the flow:
 *
 * "IDLE"
 * - show "Start discovery" button
 * - the button has two callbacks
 *   - success -> DISCOVERY_STARTED
 *   - fail -> ERROR
 * - the discovery method also has a callback
 *   - called when partner found -> PARTNER_FOUND
 *
 * PARTNER_FOUND
 * - authenticate
 *   - If OK -> AUTHENTICATION_OK
 *   - else -> AUTHENTICATION_DENIED (back to DISCOVERY_STARTED)
 *
 * AUTHENTICATION_OK
 * - connect (requestConnection) gets passed a callback, and has a failure callback, too
 *   - callback -> CONNECTION_ESTABLISHED
 *   - fail -> CONNECTION_DENIED
 *
 */

data class ProcessState(
    val currentState: ProcessStateConstants = ProcessStateConstants.IDLE,
    val message: String = ""
)

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    val tvConnectionStateHolder: TVConnectionStateHolder
) : ViewModel() {

    private val _processStateFlow = MutableStateFlow(ProcessState())
    val processStateFlow: StateFlow<ProcessState> = _processStateFlow.asStateFlow()

    private lateinit var connectionsClient: ConnectionsClient

    private val moshi = Moshi.Builder().build()
    val adapter: JsonAdapter<AllDataHolder> = moshi.adapter(AllDataHolder::class.java)

    private val eventChannel = Channel<UIEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Log.d("CVMPC", "Payload received ${payload.id}")
            if (payload.type == Payload.Type.BYTES) {
                val payloadJson: String = String(payload.asBytes()!!, UTF_8)
                val testPL = adapter.fromJson(payloadJson)
                viewModelScope.launch() {
                    eventChannel.send(
                        UIEvent.transitionState(
                            ProcessStateConstants.DATA_RECEIVED,
                            "Data received"
                        )
                    )
                }
                Log.d("CVMPC", "received $testPL")
            } else {
                Log.d("CVMPC", "Payload received from $endpointId but wrong type: $payload")
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            Log.d("CVMPTU", "Payload Transfer Update: ${update.status}")
            when (update.status) {
                PayloadTransferUpdate.Status.CANCELED -> {
                    Log.d("CVMPTU", "Transfer cancelled")
                }

                PayloadTransferUpdate.Status.FAILURE -> {
                    Log.d("CVMPTU", "Transfer failed")
                }

                PayloadTransferUpdate.Status.IN_PROGRESS -> {
                    Log.d("CVMPTU", "Transfer in progress")
                }

                PayloadTransferUpdate.Status.SUCCESS -> {
                    Log.d("CVMPTU", "Transfer successful")
                }
            }
        }
    }

    fun provideConnectionsClient(connectionsClient: ConnectionsClient) {
        this.connectionsClient = connectionsClient
    }

    override fun onCleared() {
        connectionsClient.stopDiscovery()
        connectionsClient.stopAllEndpoints()
        super.onCleared()
    }

    fun triggerTransitionToNewState(
        newState: ProcessStateConstants,
        message: String = "OK"
    ) {
        // all the logic should be here
        // DO NOT CALL RECURSIVELY!
        // ONLY CALL FROM View!
        when (newState) {
            ProcessStateConstants.IDLE -> {
            }

            ProcessStateConstants.CANCELLED -> {
                Log.d("SNDVM", "Cancelled. Disconnecting everything...")
                connectionsClient.stopAllEndpoints()
                connectionsClient.stopDiscovery()
                _processStateFlow.value = ProcessState(newState, "Cancelled")
            }

            ProcessStateConstants.DATA_RECEIVED -> {
                Log.d("SNDVM", "Data received, now moving on...")
                _processStateFlow.value =
                    ProcessState(ProcessStateConstants.DATA_RECEIVED, "Data received.")
            }

//            ProcessStateConstants.SENDING -> {
//                Log.d("SNDVM", "Will try and send $message...")
//                viewModelScope.launch {
//                    val process = repository.loadProcessByUuid(message)
//                    if (process != null) {
//                        establishedConnections.forEach{ connection ->
//                            Log.d("SNDVM", "Sending payload to endpoint ${connection.value.endpointId}...")
//                            connectionsClient.sendPayload(
//                                connection.value.endpointId,
//                                process.toPayload()
//                            )
//                            Log.d("SNDVM", "Payload presumably sent.")
//                        }
//                        _processStateFlow.value = ProcessState(ProcessStateConstants.CONNECTION_ESTABLISHED, "OK")
//                    }
//                }
//            }

            ProcessStateConstants.DONE -> {
                Log.d("SNDVM", "Done. Disconnecting everything...")
                connectionsClient.stopAllEndpoints()
                connectionsClient.stopDiscovery()
                _processStateFlow.value = ProcessState(newState, "Done")
            }

            ProcessStateConstants.ERROR -> {
                Log.d("SNDVM", "Error!")
            }
        }
    }

}