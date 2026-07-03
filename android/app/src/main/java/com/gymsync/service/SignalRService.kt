package com.gymsync.service

import com.gymsync.BuildConfig
import com.gymsync.data.local.TokenManager
import com.microsoft.signalr.*
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalRService @Inject constructor(
    private val tokenManager: TokenManager
) {
    private var hubConnection: HubConnection? = null

    private val _partnerStatus = Channel<PartnerStatusEvent>(Channel.BUFFERED)
    val partnerStatus: Flow<PartnerStatusEvent> = _partnerStatus.receiveAsFlow()

    private val _setCompleted = Channel<SetCompletedEvent>(Channel.BUFFERED)
    val setCompleted: Flow<SetCompletedEvent> = _setCompleted.receiveAsFlow()

    private val _restTimerUpdate = Channel<RestTimerEvent>(Channel.BUFFERED)
    val restTimerUpdate: Flow<RestTimerEvent> = _restTimerUpdate.receiveAsFlow()

    private val _newMessage = Channel<NewMessageEvent>(Channel.BUFFERED)
    val newMessage: Flow<NewMessageEvent> = _newMessage.receiveAsFlow()

    private val _messageDelivered = Channel<MessageStatusEvent>(Channel.BUFFERED)
    val messageDelivered: Flow<MessageStatusEvent> = _messageDelivered.receiveAsFlow()

    private val _messageRead = Channel<MessageStatusEvent>(Channel.BUFFERED)
    val messageRead: Flow<MessageStatusEvent> = _messageRead.receiveAsFlow()

    private val _partnerTyping = Channel<TypingEvent>(Channel.BUFFERED)
    val partnerTyping: Flow<TypingEvent> = _partnerTyping.receiveAsFlow()

    private val _motivation = Channel<MotivationEvent>(Channel.BUFFERED)
    val motivation: Flow<MotivationEvent> = _motivation.receiveAsFlow()

    private val _petBringsWater = Channel<PetWaterEvent>(Channel.BUFFERED)
    val petBringsWater: Flow<PetWaterEvent> = _petBringsWater.receiveAsFlow()

    private val _petResting = Channel<PetRestingEvent>(Channel.BUFFERED)
    val petResting: Flow<PetRestingEvent> = _petResting.receiveAsFlow()

    private val _petWaterDrank = Channel<PetWaterEvent>(Channel.BUFFERED)
    val petWaterDrank: Flow<PetWaterEvent> = _petWaterDrank.receiveAsFlow()

    private var isConnected = false

    fun connect() {
        if (isConnected) return

        val token = tokenManager.accessToken ?: return

        try {
            hubConnection = HubConnectionBuilder.create(BuildConfig.SIGNALR_URL)
                .withAccessTokenProvider(Single.just(token))
                .withServerTimeout(TimeUnit.SECONDS.toMillis(30))
                .build()

            hubConnection?.on("PartnerStatusChanged",
                Action2<String, String> { userId, status ->
                    _partnerStatus.trySend(PartnerStatusEvent(userId, status))
                }, String::class.java, String::class.java)

            hubConnection?.on("SetCompleted",
                Action4<String, String, Int, Boolean> { userId, exerciseName, setNumber, isPr ->
                    _setCompleted.trySend(SetCompletedEvent(userId, exerciseName, setNumber, isPr))
                }, String::class.java, String::class.java, Int::class.java, Boolean::class.java)

            hubConnection?.on("RestTimerUpdate",
                Action3<String, Int, Boolean> { userId, remaining, isRunning ->
                    _restTimerUpdate.trySend(RestTimerEvent(userId, remaining, isRunning))
                }, String::class.java, Int::class.java, Boolean::class.java)

            hubConnection?.on("NewMessage",
                Action3<String, String, String> { senderId, messageId, preview ->
                    _newMessage.trySend(NewMessageEvent(senderId, messageId, preview))
                }, String::class.java, String::class.java, String::class.java)

            hubConnection?.on("MessageDelivered",
                Action1<String> { messageId ->
                    _messageDelivered.trySend(MessageStatusEvent(messageId))
                }, String::class.java)

            hubConnection?.on("MessageRead",
                Action1<String> { messageId ->
                    _messageRead.trySend(MessageStatusEvent(messageId))
                }, String::class.java)

            hubConnection?.on("PartnerTyping",
                Action2<String, Boolean> { userId, isTyping ->
                    _partnerTyping.trySend(TypingEvent(userId, isTyping))
                }, String::class.java, Boolean::class.java)

            hubConnection?.on("MotivationReceived",
                Action3<String, String, String> { senderId, type, message ->
                    _motivation.trySend(MotivationEvent(senderId, type, message))
                }, String::class.java, String::class.java, String::class.java)

            hubConnection?.on("PetBringsWater",
                Action3<String, String, String> { partnerId, petName, petType ->
                    _petBringsWater.trySend(PetWaterEvent(partnerId, petName, petType))
                }, String::class.java, String::class.java, String::class.java)

            hubConnection?.on("PetResting",
                Action4<String, String, String, Boolean> { partnerId, petName, petType, isResting ->
                    _petResting.trySend(PetRestingEvent(partnerId, petName, petType, isResting))
                }, String::class.java, String::class.java, String::class.java, Boolean::class.java)

            hubConnection?.on("PetWaterDrank",
                Action3<String, String, String> { partnerId, petName, petType ->
                    _petWaterDrank.trySend(PetWaterEvent(partnerId, petName, petType))
                }, String::class.java, String::class.java, String::class.java)

            hubConnection?.start()?.blockingAwait()
            isConnected = true
        } catch (e: Exception) {
            isConnected = false
        }
    }

    fun disconnect() {
        try {
            hubConnection?.stop()
        } catch (_: Exception) { }
        hubConnection = null
        isConnected = false
    }

    fun setStatus(status: String) {
        hubConnection?.send("SetStatus", status)
    }

    fun joinWorkoutRoom(workoutId: String) {
        hubConnection?.send("JoinWorkoutRoom", workoutId)
    }

    fun leaveWorkoutRoom(workoutId: String) {
        hubConnection?.send("LeaveWorkoutRoom", workoutId)
    }

    fun notifySetCompleted(workoutId: String, exerciseName: String, setNumber: Int, isPr: Boolean) {
        hubConnection?.send("NotifySetCompleted", workoutId, exerciseName, setNumber, isPr)
    }

    fun notifyRestTimerUpdate(workoutId: String, remainingSeconds: Int, isRunning: Boolean) {
        hubConnection?.send("NotifyRestTimerUpdate", workoutId, remainingSeconds, isRunning)
    }

    fun sendMotivation(receiverId: String, type: String, message: String?) {
        hubConnection?.send("SendMotivation", receiverId, type, message)
    }

    fun markDelivered(messageId: String) {
        hubConnection?.send("MarkDelivered", messageId)
    }

    fun markRead(messageId: String) {
        hubConnection?.send("MarkRead", messageId)
    }

    fun setTyping(isTyping: Boolean) {
        hubConnection?.send("SetTyping", isTyping)
    }

    fun notifyPetBringsWater(workoutId: String, petName: String, petType: String) {
        hubConnection?.send("PetBringsWater", workoutId, petName, petType)
    }

    fun notifyPetResting(workoutId: String, petName: String, petType: String, isResting: Boolean) {
        hubConnection?.send("PetResting", workoutId, petName, petType, isResting)
    }

    fun notifyPetWaterDrank(workoutId: String, petName: String, petType: String) {
        hubConnection?.send("PetWaterDrank", workoutId, petName, petType)
    }
}

data class PetWaterEvent(val partnerId: String, val petName: String, val petType: String)
data class PetRestingEvent(val partnerId: String, val petName: String, val petType: String, val isResting: Boolean)

data class PartnerStatusEvent(val userId: String, val status: String)
data class SetCompletedEvent(val userId: String, val exerciseName: String, val setNumber: Int, val isPr: Boolean)
data class RestTimerEvent(val userId: String, val remainingSeconds: Int, val isRunning: Boolean)
data class NewMessageEvent(val senderId: String, val messageId: String, val preview: String)
data class MessageStatusEvent(val messageId: String)
data class TypingEvent(val userId: String, val isTyping: Boolean)
data class MotivationEvent(val senderId: String, val type: String, val message: String?)
