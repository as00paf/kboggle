package data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class TestMessage() {
    @Serializable
    @SerialName("test_join")
    data class TestJoinMessage(val name: String) : TestMessage()

    @Serializable
    @SerialName("test_sync")
    data class TestSyncMessage(val data: Data) : TestMessage()
}